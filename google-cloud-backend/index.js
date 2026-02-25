// to run: gcloud run deploy llm --source . --region GOOGLE_CLOUD_PROJECT_REGION --allow-unauthenticated

const { GoogleGenAI } = require("@google/genai");
const { ChatVertexAI } = require("@langchain/google-vertexai");
const express = require("express"); // to handle request/responses
const multer = require("multer"); // for handling multipart/form-data (images): https://www.npmjs.com/package/multer

const { initializeApp } = require("firebase-admin/app"); // firebase admin sdk
const { getAuth } = require("firebase-admin/auth"); // auth method from firebase
const { getFirestore, FieldValue } = require("firebase-admin/firestore"); // firestore

// for structuring LangChain outputs:
const { z } = require("zod");

// LangChain:
const { createAgent, toolStrategy } = require("langchain");

// like an enum but in javascript (enums only supported in typescript)
// https://masteringjs.io/tutorials/fundamentals/enum
// these are the same as present in the Kotlin app
const LlmRequestType = {
  RECIPE: "RECIPE",
  RECIPE_FROM_IMAGE_AND_DISH: "RECIPE_FROM_IMAGE_AND_DISH",
  DISHES_FROM_INGREDIENTS: "DISHES_FROM_INGREDIENTS",
  DISHES_FROM_IMAGE: "DISHES_FROM_IMAGE",
  DISHES_FROM_INGREDIENTS_IMAGE: "DISHES_FROM_INGREDIENTS_IMAGE",
  RECIPE_QUERY: "RECIPE_QUERY",
  RECIPE_ALTERATION: "RECIPE_ALTERATION",
};

// For storing image in memory with 2mb limit:
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 2 * 1024 * 1024 }, // filesize is in bytes
});

const GOOGLE_CLOUD_PROJECT = "GOOGLE_CLOUD_PROJECT_NAME";
const GOOGLE_CLOUD_LOCATION = "GOOGLE_CLOUD_PROJECT_REGION";

const app = express();
app.use(express.json());

initializeApp(); // admin sdk initialised, uses application default credentials
const db = getFirestore();

// Units:
// these will be switched dynamically using user preferences
const metricUnits =
  "ML, L, G, KG, CLOVE, HEAD, BUNCH, SPRIG, BULB, STALK, STICK, PIECE, LEAF, DASH, DRIZZLE, PINCH, SPLASH, DROP, HANDFUL, SLICE, LOAF, ROLL, SHEET, BLOCK, BAR. use UNIT if the ingredient has no unit (e.g. carrot has no unit)";
const imperialUnits =
  "TSP, TBSP, FL_OZ, CUP, PINT, QUART, GALLON, OZ, LB, CLOVE, HEAD, BUNCH, SPRIG, BULB, STALK, STICK, PIECE, LEAF, DASH, DRIZZLE, PINCH, SPLASH, DROP, HANDFUL, SLICE, LOAF, ROLL, SHEET, BLOCK, BAR. use UNIT if the ingredient has no unit (e.g. carrot has no unit)";

// Schemas:
// ingredients put together:
const IngredientSchemaZod = z.object({
  ingredientSingular: z.string().describe(""),
  ingredientPlural: z.string().describe(""),
  ingredientAmount: z.string().describe(""),
  ingredientAmountUnit: z.string().describe(""),
});

// recipe generation:
const RecipeSchemaZod = z.object({
  title: z.string().describe("title of the recipe"),

  ingredients: z.array(
    z.object({
      recipeSection: z
        .string()
        .describe(
          "A heading for this list of ingredients, for example 'For the ...' e.g. 'For the filling'",
        ),
      ingredientsSingular: z
        .array(z.string())
        .describe(
          "A list of ingredients for this recipeSection, in their singular form. Do not specify details such as size; simply give the ingredient " +
            "(type should be specified only if necessary, e.g. brown sugar vs granulated sugar). " +
            "Do not include the amounts here; these will go in ingredientAmounts and ingredientAmountsUnits, respectively. " +
            "Simply return as comma-separated list using , character e.g. a, b, c." +
            "ENSURE that the length of ingredientsSingular = the length of corresponding ingredientAmounts, ingredientsPlural and ingredientAmountsUnits",
        ),
      ingredientsPlural: z
        .array(z.string())
        .describe(
          "A list of ingredients for this recipeSection, in their English correct plural form, as would appear in a recipe. For example, egg -> eggs, flour -> flour " +
            "Do not include the amounts here; these will go in ingredientAmounts and ingredientAmountsUnits, respectively. " +
            "Simply return as comma-separated list using , character e.g. a, b, c",
        ),
      ingredientAmounts: z
        .array(z.string())
        .describe(
          "A list of the amounts of each ingredient to use, given only as integers or floats. " +
            "It must correspond with the ingredient in the ingredients list and the units given in the ingredientAmountsUnits list. " +
            "Simply return as comma-separated list using , character e.g. a, b, c",
        ),
      ingredientAmountsUnits: z
        .array(z.string())
        .describe(
          "A list of the ingredient units to use. Must correspond with the ingredient in the ingredients list and ingredientAmounts list. " +
            "Simply return as comma-separated list using , character e.g. a, b, c " +
            "Ensure that the unit makes sense, for example only use an approximate unit (such as splash, sprinkle, dash, etc) if the corresponding amount itself is a whole number - otherwise use a unit reflective of the precision (e.g. tsp, tbsp, ml)" +
            "Return only values from the following list, exactly as they are in the list (as they will be mapped using a predefined enum): " +
            metricUnits +
            " Only use UNIT if the ingredient has no measurement; it is a piece itself (e.g. egg, whole fruit, whole vegetable). In particular, if it is a spice, baking agent or herb, do not use UNIT.",
        ),
    }),
  ),
  servings: z
    .string()
    .describe(
      "The number of servings (not pieces) made of this meal using the amount of ingredients in the ingredientAmounts and ingredientAmountsUnits list. " +
        "Given as an integer.",
    ),
  mealType: z.string().describe(
    "The type of meal that this corresponds to (breakfast, lunch, dinner, snack)", // need to expand this to include the user's custom meal types
  ),
  method: z
    .string()
    .describe(
      "A numbered list of the recipe's method, in the form \\nX. for all X to ensure each step is on its own line " +
        "For step(s) involving heating the oven, ensure you specify Celsius and Farenheit and different oven options (Fan, Gas mark etc) " +
        "When writing the units e.g. ML, ensure they correspond with those you wrote in ingredientAmountsUnits, but write them in the human-readable form (e.g. ML -> ml) ",
      "For the final step, include a nice, upbeat happy step such as 'Enjoy your homemade ...'",
    ),
});

/**
 * Receives the request send from the Nourish app and builds the response accordingly.
 * 1. checks if the prompt is present in the request
 * 2. authenticates the user using the Bearer token sent in the request. Extracts the
 *    corresponding uid of that user if authentication is successful.
 * 3. rate limits the user by incrementing their daily token count by one
 *    (need to change this later to support different types of requests e.g. images)
 * 4. if any of the previous steps failed, it will return with an error. if this
 *    point is reached, the user is authorised to make the LLM query. Hence, the LLM
 *    query is now made at this step, and the result returned.
 * @param req - The request handled by express, to be dealt with
 * @param res - The response handled by express, to be built up
 * @param requestType - the LlmRequestType, defining the type of request (generate recipe, dishes from image, etc).
 *                      thus, will be handled accordingly.
 * @returns The response, containing its result and a 500 error with detail if
 *          an error occurred.
 */
async function generate(req, res, requestType) {
  try {
    // Extract and validate the prompt
    const prompt = req.body?.prompt;
    const errorMessage = await validateRequest(req, prompt, requestType);
    if (errorMessage != "") {
      return res.status(400).json({ error: errorMessage });
    }

    // Perform the LLM request and retrieve the response, different request depending on the request type
    let result;
    switch (requestType) {
      // Generate a recipe
      case LlmRequestType.RECIPE: {
        result = await generateRecipe(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt,
        );
        break;
      }

      case LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH: {
        result = await generateRecipeFromDishImage(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          req,
        );
        break;
      }

      // Generate dishes from ingredients
      case LlmRequestType.DISHES_FROM_INGREDIENTS: {
        result = await generateDishesFromIngredients(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt,
        );
        break;
      }

      // Generate dishes from image of ingredients, e.g. of contents of fridge
      case LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE: {
        result = await generateDishesFromIngredientsImage(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          req,
        );
        break;
      }

      // Generate dishes from an image
      case LlmRequestType.DISHES_FROM_IMAGE: {
        result = await generateDishesFromImage(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          req,
        );
        break;
      }
    }

    if (result == null) {
      console.log("Error: LLM request failed; no result.");
      return res.status(400).json({ error: "LLM request failed; no result." });
    }

    res.json(result);
    return res;
  } catch (e) {
    console.error(e);
    // throw 500 error because error happened on the server side (Internal Server Error)
    return res.status(500).json({ error: e.message });
  }
}

/**
 * Authenticates a user's Bearer token sent in the request.
 * @param req - The request containing the Bearer token. This token is then verified
 *              using the Firebase Authentication .verifyIdToken() method. This
 *              verifies the signature, checks it isn't expired and finds the corresponding uid.
 * @returns the uid of the authenticated user, and null if authentication failed.
 */
async function authenticateUser(req) {
  const auth = req.get("Authorization"); // authorisation header using express

  // No authorization Bearer token:
  if (auth == null) return null;

  // invalid Bearer token format:
  if (!auth.startsWith("Bearer ")) return null;

  const bearer = auth.slice(7); // slice the "Bearer " out to get only the token
  try {
    const decodedToken = await getAuth().verifyIdToken(bearer);
    return decodedToken.uid; // Authentication success
  } catch (e) {
    // If any error occurred, the authentication failed:
    return null;
  }
}

/**
 * Rate limits the user by finding their current day limit, stored in the Firestore
 * document: /users/{uid}/token-usage/{date}/{amount}.
 * If {amount} > 100, the user has reached their daily limit and thus cannot make any
 * more LLM requests.
 * A new rate limit is created each day, under the current date.
 * TODO This will need to be adapted in the future to accommodate for different levels of
 * recipe generation, e.g. with images which will use more tokens.
 * @param uid - The uid of the user to rate limit.
 * @returns true if the user has not exceeded their daily limit, false otherwise.
 */
async function rateLimitUser(uid) {
  const userRef = db.collection("users").doc(uid); // reference to user doc

  try {
    const userDoc = await userRef.get(); // user doc (/users/{uid})
    if (userDoc.exists) {
      const dateToday = new Date().toJSON().slice(0, 10);

      // Check that token limit for today has not been exceeded:
      const tokenAmountDoc = await userRef
        .collection("token-usage")
        .doc(dateToday)
        .get();

      if (!tokenAmountDoc.exists) {
        // User has not made any requests today, permit and increment.
        await updateUserTokenCount(userRef, dateToday, 1);
        return true;
      }
      if (tokenAmountDoc.get("amount") >= 100) {
        // User has reached their daily token limit. Do not permit or increment
        return false;
      }
      // Otherwise, user is below their daily token limit, permit and increment.
      await updateUserTokenCount(userRef, dateToday, 1);
      return true;
    }
    return false;
  } catch (error) {
    console.error("Rate limit error:" + error);
  }
}

/**
 * Auxiliary function for @see rateLimitUser(uid)
 * Updates the token amount used for a day of the user, stored in the Firestore.
 * @param userRef - the user reference in Firestore to update.
 * @param date - the date to update, usually set to today in format DD-MM-YYYY
 * @param incrementAmount - the amount to increment by the token amount used by.
 */
async function updateUserTokenCount(userRef, date, incrementAmount) {
  const inc = FieldValue.increment(incrementAmount);
  await userRef
    .collection("token-usage")
    .doc(date)
    .set({ amount: inc }, { merge: true });
}

// ERROR VALIDATION:

// returns 400 errors (Bad Request) to signify error on the client's input;
// invalid syntax, malformed data, invalid Bearer token
async function validateRequest(req, prompt, requestType) {
  // only do this check on things which require a prompt.
  // so not dishes from image.
  if (!prompt && requestType == LlmRequestType.RECIPE) {
    console.error("Error: missing or malformed prompt.");
    return "Missing or malformed prompt.";
  }

  // Authenticate the user using the Bearer token in the request
  const requesterUid = await authenticateUser(req);
  if (requesterUid == null) {
    console.error("Error: invalid or non-existent bearer token");
    return "Invalid or non-existent Bearer token";
  }

  // Rate limit the user by incrementing their daily token count, ensuring
  // LLM request cannot be performed if the user has exceeded their rate.
  if (!(await rateLimitUser(requesterUid))) {
    console.error("Error: token limit reached. come back tomorrow");
    return "Token limit reached. Come back tomorrow";
  }

  // Otherwise, validated
  return "";
}

// LLM CALLS USING LangChain:

/**
 * Makes the LLM request using Google Vertex AI to call the LLM and retrieve the
 * response.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param prompt - The prompt to send to the LLM, combined with the system prompt
 *                 and user written prompt from the Nourish app.
 * @returns The LLM response.
 */
async function generateRecipe(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt,
) {
  // these will be switched dynamically using user preferences
  const metricUnits =
    "ML, L, G, KG, CLOVE, HEAD, BUNCH, SPRIG, BULB, STALK, STICK, PIECE, LEAF, DASH, DRIZZLE, PINCH, SPLASH, DROP, HANDFUL, SLICE, LOAF, ROLL, SHEET, BLOCK, BAR. use UNIT if the ingredient has no unit (e.g. carrot has no unit)";
  const imperialUnits =
    "TSP, TBSP, FL_OZ, CUP, PINT, QUART, GALLON, OZ, LB, CLOVE, HEAD, BUNCH, SPRIG, BULB, STALK, STICK, PIECE, LEAF, DASH, DRIZZLE, PINCH, SPLASH, DROP, HANDFUL, SLICE, LOAF, ROLL, SHEET, BLOCK, BAR. use UNIT if the ingredient has no unit (e.g. carrot has no unit), not something else like None - only UNIT.";

  // tool strategy for formatting output:
  //https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeSchemaZod),
  });

  console.log("Generating recipe for: " + prompt + ":");

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not too much (keep below 250 words).",
      },
      {
        role: "user",
        content: "Create a recipe for the following dish: " + prompt,
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.
  0;
  console.log(JSON.stringify(result.structuredResponse));
  return result.structuredResponse;
}

async function generateDishesFromIngredients(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt,
) {
  // tool strategy for formatting output:
  //https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
  const RecipeListSchemaZod = z.object({
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe is best suited to cook from the ingredients.",
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe is best suited to cook from the ingredients.",
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe is best suited to cook from the ingredients.",
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe is best suited to cook from the ingredients.",
      ),
  });

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeListSchemaZod),
  });

  console.log("Generating dishes for ingredients: " + prompt + ":");

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, with the best dish you believe can be cooked from only these ingredients in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is satisfied and do not include more JSON." +
          "Write the name of each dish concisely; in less than 20 words per dish, but include detail if necessary e.g. flavour, type, characteristics.",
      },
      {
        role: "user",
        content:
          "Return 4 names of dishes which you believe can be cooked at home using only these ingredients (with the exception of small amounts of salt, pepper and oil): " +
          prompt,
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.

  console.log(result.structuredResponse);
  return result.structuredResponse;
}

// to handle rthe multipart data, multer is used:
// https://medium.com/@muhebollah.diu/understanding-multipart-form-data-the-ultimate-guide-for-beginners-fd039c04553d
async function generateRecipeFromDishImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  const base64encoded = file.buffer.toString("base64"); // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const dataUrl = `data:${filetype};base64,${base64encoded}`; // required format for gemini 2.0 flash

  // User can supply further detail if they wish, but this is optional:
  const dish = JSON.parse(req.body?.detail).prompt;

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeSchemaZod),
  });

  console.log("Generating recipe for dish using image:");

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not too much (keep below 250 words).",
      },
      {
        role: "user",
        content: [
          {
            type: "text",
            text:
              "Generate a recipe for the following dish, creating the recipe as close to the image provided as possible: " +
              dish,
          },
          { type: "image_url", image_url: { url: dataUrl } },
        ],
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.

  console.log(JSON.stringify(result.structuredResponse));
  return result.structuredResponse;
}

async function generateDishesFromIngredientsImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  const base64encoded = file.buffer.toString("base64"); // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const dataUrl = `data:${filetype};base64,${base64encoded}`; // required format for gemini 2.0 flash

  // User can supply further detail if they wish, but this is optional:
  const userDetail = JSON.parse(req.body?.detail).prompt;
  let userPrompt =
    "This is a picture featuring many different cooking ingredients. Return 4 names of dishes which you believe can be cooked using only these ingredients (with the exception of small amounts of salt, pepper and oil).";
  if (userDetail != "") {
    userPrompt =
      "This is a picture featuring many different cooking ingredients. Return 4 names of dishes which you believe can be cooked using only these ingredients (with the exception of small amounts of salt, pepper and oil). Further detail to assist classification: " +
      userDetail;
  }

  // tool strategy for formatting output:
  //https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
  const RecipeListSchemaZod = z.object({
    ingredientsIdentified: z
      .string()
      .describe("The ingredients you can see which are present in the image."),
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe can be cooked with ingredients present in the image.",
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe can be cooked with ingredients present in the image.",
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe can be cooked with ingredients present in the image.",
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe can be cooked with ingredients present in the image.",
      ),
  });

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeListSchemaZod),
  });

  console.log("Identifying ingredients and potential dishes:");

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, first with the ingredients that you can see present in the image in the ingredients field, and then " +
          "the dish you believe is most likely able to be cooked with these ingredients in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is satisfied and do not include more JSON." +
          "Write the name of each dish concisely; in less than 20 words per dish, but include detail if necessary e.g. flavour, type, characteristics.",
      },
      {
        role: "user",
        content: [
          {
            type: "text",
            text: userPrompt,
          },
          { type: "image_url", image_url: { url: dataUrl } },
        ],
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.

  console.log(result.structuredResponse);
  return result.structuredResponse;
}

// to handle rthe multipart data, multer is used:
// https://medium.com/@muhebollah.diu/understanding-multipart-form-data-the-ultimate-guide-for-beginners-fd039c04553d
async function generateDishesFromImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  const base64encoded = file.buffer.toString("base64"); // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const dataUrl = `data:${filetype};base64,${base64encoded}`; // required format for gemini 2.0 flash

  // User can supply further detail if they wish, but this is optional:
  const userDetail = JSON.parse(req.body?.detail).prompt;
  let userPrompt =
    "Return 4 names of dishes which you believe are in the image.";
  if (userDetail != "") {
    userPrompt =
      "Return 4 names of dishes which you believe are in the image. Further detail to assist classification: " +
      userDetail;
  }

  // tool strategy for formatting output:
  //https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
  const RecipeListSchemaZod = z.object({
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe is present in the image.",
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe is present in the image.",
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe is present in the image.",
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe is present in the image.",
      ),
  });

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeListSchemaZod),
  });

  console.log("Identifying dishes in the image:");

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, with the dish you believe is most likely depicted in the image in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is satisfied and do not include more JSON." +
          "Write the name of each dish concisely; in less than 20 words per dish, but include detail if necessary e.g. flavour, type, characteristics.",
      },
      {
        role: "user",
        content: [
          {
            type: "text",
            text: userPrompt,
          },
          { type: "image_url", image_url: { url: dataUrl } },
        ],
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.

  console.log(result.structuredResponse);
  return result.structuredResponse;
}

// To run the POST endpoint:
app.post("/recipe", async (req, res) => {
  await generate(req, res, LlmRequestType.RECIPE);
});

app.post("/dishes-from-ingredients", async (req, res) => {
  await generate(req, res, LlmRequestType.DISHES_FROM_INGREDIENTS);
});

app.post("/dishes-from-image", upload.single("image"), async (req, res) => {
  await generate(req, res, LlmRequestType.DISHES_FROM_IMAGE);
});

app.post(
  "/dishes-from-ingredients-image",
  upload.single("image"),
  async (req, res) => {
    await generate(req, res, LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE);
  },
);

app.post(
  "/recipe-from-dish-image",
  upload.single("image"),
  async (req, res) => {
    await generate(req, res, LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH);
  },
);

const port = process.env.PORT || 8080;
app.listen(port, () => console.log("Nourish endpoints are now live."));

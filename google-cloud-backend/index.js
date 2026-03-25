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

// for actions
const LlmActionType = {
  NUTRITIONAL_VALUES: "NUTRITIONAL_VALUES",
  ALTERATION: "ALTERATION",
  QUESTION: "QUESTION",
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

// Units: given as metric, something to address in the future would be also adding support for imperial which the software has the capacity to do but was not implemented for simplicity.
const metricUnits =
  "TSP, TBSP, ML, G, CLOVE, HEAD, BUNCH, SPRIG, BULB, STALK, STICK, PIECE, LEAF, CM, DASH, DRIZZLE, PINCH, SPLASH, DROP, HANDFUL, SLICE, LOAF, ROLL, SHEET, BLOCK, BAR, UNIT, PINT";
const ingredientUnitList = metricUnits.split(",").map((v) => v.trim());
// Schemas:

// for nutrition:
const NutritionSchemaZod = z.object({
  servings: z
    .number()
    .int()
    .describe(
      "The number of servings must be 1. The provided recipe amounts and ingredients are given for 1 serving, so ensure the nutrition you provide is for 1 serving.",
    ),
  calories: z
    .number()
    .int()
    .describe(
      "The estimated number of calories, in kcal, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  fat: z
    .number()
    .describe(
      "The estimated amount of fat, in grams, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  saturates: z
    .number()
    .describe(
      "The estimated amount of saturated fat ('of which saturates' so fill this in after the fat field), in grams, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  carbohydrate: z
    .number()
    .describe(
      "The estimated amount of carbohydrates, in grams, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  sugars: z
    .number()
    .describe(
      "The estimated amount of sugars ('of which sugars' so fill this in after the carbohydrate field), in grams, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  protein: z
    .number()
    .describe(
      "The estimated amount of protein, in grams, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  salt: z
    .number()
    .describe(
      "The estimated amount of salt, in grams, in this dish for 1 serving. Calculate this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
});

// for question:
const QuestionAnswerSchemaZod = z.object({
  answer: z
    .string()
    .describe(
      "The answer to the user's question about the provided recipe. Keep it clear and concise, but ensure to go into as much detail as necessary to answer the question sufficiently. Answer in British English.",
    ),
});

// for alteration: simply uses the existing recipe schema.

// ingredients put together:
// order is attempted to ensure that the result makes the most logical sense; e.g. LLM does not first fill in the amount as 1 (to use cups) but then realises cup is not an option.
const IngredientSchemaZod = z.object({
  ingredientSingular: z
    .string()
    .describe(
      "Fill this in first. An ingredient required for this recipeSection, in its singular form. Do not specify details such as size; simply give the ingredient " +
        "(type should be specified only if necessary, e.g. brown sugar vs granulated sugar). Also be careful if adding baking soda; ensure it is not too much and use baking powder instead if appropriate. " +
        "If the ingredient is 'garlic', only write 'garlic', not 'garlic clove' as this will be covered by the ingredientAmountUnit (CLOVE). Apply this rule to other ingredients where you see fit. " +
        "Do not include the amount or unit here; this will go in ingredientAmount and ingredientAmountsUnit, respectively. " +
        "Do not include temperature characteristics of the ingredient, write them exactly as how they would be bought in a shop / shopping list. Include any temperature details in the method instead (e.g. if there is milk and it should be warmed, the ingredient should only be 'milk' and outline the warming step in the method.)",
    ),
  ingredientPlural: z
    .string()
    .describe(
      "Fill this in second. The ingredient corresponding to ingredientSingular, in its correct English plural form, as would appear in a recipe. For example, egg -> eggs, flour -> flour, powder -> powder " +
        "Do not include the amount or unit here; this will go in ingredientAmount and ingredientAmountsUnit, respectively.",
    ),
  ingredientAmountUnit: z
    .enum(ingredientUnitList)
    .describe(
      "Fill this in third. The unit of the ingredient corresponding to ingredientSingular, and the amount given in ingredientAmount. " +
        "Ensure that the unit makes sense, for example only use an approximate unit (such as splash, sprinkle, dash, etc) if the corresponding amount itself is a whole number - otherwise use a unit reflective of the precision (e.g. tsp, tbsp, ml)" +
        "Return only a value from the provided list, exactly as it is in the list (as it will be mapped using a predefined enum); the value you return MUST be in the list." +
        " If you think that an imperial unit would be required, then give its value in g/kg instead (e.g. rather than using cup, as this is not used in Europe/UK). " +
        " If the ingredient requires KG, then convert the value to G and use that. Likewise, if the ingredient requires L, then convert the value to ML and use that. Only use UNIT if the ingredient has no measurement/unit; it is a piece itself (e.g. egg, whole fruit, whole vegetable). In particular, if it is a spice, baking agent or herb, do not use UNIT. For these you should use tsp/tbsp as this is most common and easy to measure, unless you believe another unit is more appropriate.",
    ),
  ingredientAmount: z
    .number()
    .describe(
      "Fill this in last. The amount of the ingredient corresponding to ingredientSingular, given only as an integer or float. " +
        "Furthermore, it must correspond with the unit given in the ingredientAmountUnit.",
    ),
});

// recipe generation:
function recipeSchemaZod(allNutritionalFilters, dishType) {
  const nutritionalFilterList = allNutritionalFilters
    .split(",")
    .map((v) => v.trim());
  const dishTypeList = dishType.split(",").map((v) => v.trim());
  console.log("nutritional filter list: ", nutritionalFilterList);
  console.log("dish type list: ", dishTypeList);

  return z.object({
    title: z
      .string()
      .describe("Title of the recipe, must be 42 characters or less."),

    sectionIngredients: z.array(
      z.object({
        section: z
          .string()
          .describe(
            "A heading for this list of ingredients, for example 'For the ...' e.g. 'For the filling'",
          ),
        ingredients: z.array(IngredientSchemaZod),
      }),
    ),

    servings: z
      .number()
      .int()
      .describe(
        "The number of servings (not pieces) made of this meal using the amount of ingredients in the ingredientAmounts and ingredientAmountsUnits list. " +
          "Given as an integer.",
      ),

    mealType: z
      .enum(dishTypeList)
      .describe(
        "The type of meal that this corresponds to, returned exactly and with the same capitalisation as ONE element from the list ",
      ),

    method: z
      .array(z.string())
      .describe(
        "An array of the recipe's method, each array element being an individual step. Ensure that the array is ordered from first to last step, and do not include step numbers. " +
          "For step(s) involving heating the oven, ensure you specify Celsius and Farenheit and different oven options (Fan, Gas mark etc) " +
          "When writing the units e.g. ML, ensure they correspond with those you wrote in the sectionIngredients array, but write them in the human-readable form (e.g. ML -> ml, DASH -> dash) " +
          "Do NOT include any absolute measurements/amounts within the steps (e.g. 100ml of ingredient X), only refer to the ingredients themselves (e.g. ingredient X) (as the recipe will be able to be scaled by the user). If you do need to state measurements, only use relative e.g. half of ingredient X etc." +
          "For the final step, include a nice, upbeat happy step such as 'Enjoy your homemade ...'",
      ),

    tags: z.array(
      z
        .enum(nutritionalFilterList)
        .describe(
          "Fill this in once the recipe has been generated, do not fill this in first and then fill in the recipe based on it: " +
            "A list of 'tags' which apply to this recipe, as a comma separated list e.g. 'a, b, c' " +
            "Each tag must be from the list, do not include more (Be strict with this; e.g. it is only NO-ADDED-SUGAR if no extra sugar has been added - sugar sources such as maple syrup, honey etc still count as added sugar.) ",
        ),
      //allNutritionalFilters, // nutrients
      // need to put all user specified tags here in the future
    ),

    storage: z
      .string()
      .describe(
        "A brief sentence describing how to best store the food once cooked, to maximise its shelf life. If it is freezer friendly, state this too. State how to safely reheat (and defrost if applicable, even if eaten cold) the food, if applicable.",
      ),

    recipeNotPossible: z
      .boolean()
      .describe(
        "If the provided prompt is invalid for generating a human-edible recipe, " +
          "such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an empty string, " +
          "otherwise return 'false' and generate the recipe accordingly." +
          " With this being said, though, still be creative and if it isn't immediately obvious how to make a dish within the constraints, BE CREATIVE and think of a way (as long as it is edible by a human). Only return true here if there really is no way.",
      ),

    emoji: z
      .string()
      .describe(
        "A single emoji which best describes this dish. Consider the recipe/ingredients when choosing the emoji; don't just base the emoji on the name of the dish.",
      ),

    colour: z
      .enum([
        "RED",
        "PINK",
        "ORANGE",
        "YELLOW",
        "GREEN",
        "BLUE",
        "PURPLE",
        "GREY",
        "BROWN",
      ])
      .describe(
        "The colour which best suits the recipe, preferably matching or going well with the emoji you provide. First create the recipe, then choose its emoji, then the colour accordingly.",
      ),
  });
}

// tool strategy for formatting output:
//https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
function recipeListFromIngredientsSchemaZod(dishType) {
  return z.object({
    ingredientsIdentified: z
      .string()
      .describe(
        "The ingredients that the user has entered, separated by commas, and only these ingredients. So that recipes which are possible to cook using only those ingredients can be created.",
      ),
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe is best suited to cook from the ingredients and nutritional information, for type: " +
          dishType,
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe is best suited to cook from the ingredients and nutritional information, for type: " +
          dishType,
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe is best suited to cook from the ingredients and nutritional information, for type: " +
          dishType,
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe is best suited to cook from the ingredients and nutritional information, for type: " +
          dishType,
      ),
    dishNotPossible: z
      .boolean()
      .describe(
        "If the provided ingredients are invalid for generating a human-edible recipe, such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an empty string, otherwise return 'false' and generate the dishes accordingly." +
          " With this being said, though, still be creative and if it isn't immediately obvious how to make a dish within the constraints, BE CREATIVE and think of a way (as long as it is edible by a human). Only return true here if there really is no way.",
      ),
  });
}

// tool strategy for formatting output:
//https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
function recipeListFromImageIngredientsSchemaZod(dishType) {
  return z.object({
    ingredientsIdentified: z
      .string()
      .describe("The ingredients you can see which are present in the image."),
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe can be cooked with ingredients present in the image, considering the type: " +
          dishType,
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe can be cooked with ingredients present in the image, considering the type: " +
          dishType,
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe can be cooked with ingredients present in the image, considering the type: " +
          dishType,
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe can be cooked with ingredients present in the image, considering the type: " +
          dishType,
      ),
    dishNotPossible: z
      .boolean()
      .describe(
        "If the provided ingredients in the image are invalid for generating a human-edible recipe, such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an empty string, otherwise return 'false' and generate the dishes accordingly." +
          " With this being said, though, still be creative and if it isn't immediately obvious how to make a dish within the constraints, BE CREATIVE and think of a way (as long as it is edible by a human). Only return true here if there really is no way.",
      ),
  });
}

// tool strategy for formatting output:
//https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
function recipeListFromImageSchemaZod(dishType) {
  return z.object({
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe is present in the image, considering the type: " +
          dishType,
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe is present in the image, considering the type: " +
          dishType,
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe is present in the image, considering the type: " +
          dishType,
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe is present in the image, considering the type: " +
          dishType,
      ),
    dishNotPossible: z
      .boolean()
      .describe(
        "If the provided image does not contain a valid dish, and thus invalid for generating a human-edible recipe, such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an empty string, otherwise return 'false' and generate the dishes accordingly." +
          " With this being said, though, still be creative and if it isn't immediately obvious how to make a dish within the constraints, BE CREATIVE and think of a way (as long as it is edible by a human). Only return true here if there really is no way.",
      ),
  });
}

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

    // Extract optional nutritional filters and meal type:
    const chosenNutritionalFilters = req.body?.chosenNutritionalFilters;
    const allNutritionalFilters = req.body?.allNutritionalFilters;
    const dishType = req.body?.dishType;

    // Perform the LLM request and retrieve the response, different request depending on the request type
    let result;
    switch (requestType) {
      // Generate a recipe
      case LlmRequestType.RECIPE: {
        result = await generateRecipe(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt,
          allNutritionalFilters,
          chosenNutritionalFilters,
          dishType,
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
          // all nutritional filters are not passed here as they are not used.
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt,
          chosenNutritionalFilters,
          dishType,
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

async function generateAction(req, res, actionType) {
  try {
    // Extract and validate the prompt
    const reqBody = req.body;
    if (reqBody == null) {
      return res.status(400).json({ error: "The request body is null." });
    }

    // extract the user's query and recipe
    const query = reqBody.query;
    const recipe = reqBody.recipe;

    const errorMessage = await validateRequest(req);
    if (errorMessage != "") {
      return res.status(400).json({ error: errorMessage });
    }

    // Perform the LLM request and retrieve the response, different request depending on the request type
    let result;
    switch (actionType) {
      // generate nutritional values
      case LlmActionType.NUTRITIONAL_VALUES: {
        result = await generateNutritionalValues(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          recipe,
        );
        break;
      }

      // generate an answer to the user's question
      case LlmActionType.QUESTION: {
        result = await generateQuestionAnswer(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          query,
          recipe,
        );
        break;
      }

      // generate a new, altered recipe
      case LlmActionType.ALTERATION: {
        // only used for recipe alteration:
        const allNutritionalFilters = reqBody.allNutritionalFilters;
        const dishType = reqBody.dishType;

        result = await generateAlteredRecipe(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          query,
          recipe,
          allNutritionalFilters,
          dishType,
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

// LLM CALLS using LangChain, for the recipe actions:

/**
 * Makes the LLM request using Google Vertex AI to call the LLM and retrieve the
 * response.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @returns The LLM response.
 */
async function generateNutritionalValues(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  recipe,
) {
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(NutritionSchemaZod),
  });

  console.log("Generating nutritional values for: " + JSON.stringify(recipe));

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema and ensure it is given for 1 serving. The ingredients and their amounts in the recipe are provided for 1 serving, so calculate the nutrition using these values - do not scale anything. Use British standards. " +
          "Generate the nutritional values as requested in the schema for the provided recipe. If your answer contains a decimal, ensure it is to at most 2 decimal places.",
      },
      {
        role: "user",
        content:
          "Generate the nutritional values for the following recipe: " +
          JSON.stringify(recipe),
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.
  console.log(JSON.stringify(result.structuredResponse));
  return result.structuredResponse;
}

async function generateQuestionAnswer(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  query,
  recipe,
) {
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(QuestionAnswerSchemaZod),
  });

  console.log(
    "Answering the question: " +
      query +
      " for recipe:" +
      JSON.stringify(recipe),
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not too much (keep below 100 words). Use British English. " +
          "Generate an answer to the question provided to the user, regarding the recipe also provided by the user." +
          "As the question is regarding food, ensure you conform to food safety and hygiene standards but only be explicit about this if the user specifically asks something regarding food safety/hygiene/storage.",
      },
      {
        role: "user",
        content:
          "Please answer my question: '" +
          query +
          "' about the following recipe: " +
          JSON.stringify(recipe),
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.
  console.log(JSON.stringify(result.structuredResponse));
  return result.structuredResponse;
}

async function generateAlteredRecipe(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  query,
  recipe,
  allNutritionalFilters,
  dishType,
) {
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(
      recipeSchemaZod(allNutritionalFilters, dishType),
    ),
  });

  console.log(
    "Answering the question: " +
      query +
      " for recipe:" +
      JSON.stringify(recipe),
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not too much (keep below 250 words). Use British English. " +
          "Generate a new recipe using the provided recipe, ensuring it is altered to the user's needs accordingly." +
          " The provided amounts in the recipe make 1 serving. Create your recipe for 1 serving too, but preserve the 'serving' field as the SAME value given, as this is used for recipe scaling separately. The recipe should remain the same for any part which does not need to be altered to the request." +
          "If the recipe cannot be altered in the way that the user wishes (e.g. inedible), fill in the recipeNotPossible field as true and all others as null. Only do this if there really is no way to alter it, otherwise be creative to meet the user's needs.",
      },
      {
        role: "user",
        content:
          "Please create a new recipe based on the alteration: '" +
          query +
          "' using the following recipe: " +
          JSON.stringify(recipe),
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.
  console.log(JSON.stringify(result.structuredResponse));
  return result.structuredResponse;
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
  allNutritionalFilters,
  chosenNutritionalFilters,
  dishType,
) {
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
    responseFormat: toolStrategy(
      recipeSchemaZod(allNutritionalFilters, dishType),
    ),
  });

  console.log(
    "Generating recipe for: " +
      prompt +
      " with nutritional filters: " +
      chosenNutritionalFilters +
      "and dish type: " +
      dishType +
      "and all nutritional filters: " +
      allNutritionalFilters +
      ":",
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not too much (keep below 250 words). Use British English. Do not include absolute amounts in the method part of the schema. Ensure that the recipe produced conforms to the following nutritional requirements and are thus listed in the tags array of the schema (and any more tags which you deem appropriate for the recipe): " +
          chosenNutritionalFilters +
          " and the recipe must be of type: " +
          dishType,
      },
      {
        role: "user",
        content:
          "Create a recipe for the following dish, ensuring that the recipe produced conforms to these nutritional requirements: " +
          chosenNutritionalFilters +
          " and the recipe must be of type: " +
          dishType +
          ". The recipe to generate is: " +
          prompt,
      },
    ],
  });

  // note: this returns the tokens. should use this value to increment the token count instead.
  console.log(JSON.stringify(result.structuredResponse));
  return result.structuredResponse;
}

async function generateDishesFromIngredients(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt,
  chosenNutritionalFilters,
  dishType,
) {
  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(recipeListFromIngredientsSchemaZod(dishType)),
  });

  console.log(
    "Generating dishes for ingredients: " +
      prompt +
      " with filters: " +
      chosenNutritionalFilters +
      " and type: " +
      dishType,
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, with the best dish you believe can be cooked from only these ingredients in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is satisfied and do not include more JSON. Use British English. " +
          "Write the name of each dish concisely; in less than 20 words per dish, but include detail if necessary e.g. flavour, type, characteristics." +
          "Ensure that every dish conforms to the nutritional filters: " +
          chosenNutritionalFilters,
      },
      {
        role: "user",
        content:
          "Return 4 names of dishes which are of type " +
          dishType +
          " and conform to the nutritional filters: " +
          chosenNutritionalFilters +
          ", which you believe can be cooked at home using only these ingredients (with the exception of cooking ingredients which are almost always present - small amounts of salt, pepper and oil. With that being said, though, do not include the salt/pepper/oil if it is not relevant to the recipe): " +
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
  const detail = JSON.parse(req.body?.detail);
  const dish = detail.prompt;
  const dishType = detail.dishType;
  const chosenNutritionalFilters = detail.chosenNutritionalFilters;
  const allNutritionalFilters = detail.allNutritionalFilters;

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(
      recipeSchemaZod(allNutritionalFilters, dishType),
    ),
  });

  console.log(
    "Generating recipe for dish using image" +
      " with filters: " +
      chosenNutritionalFilters +
      " and type: " +
      dishType,
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not too much (keep below 250 words). Use British English. Do not include absolute amounts in the method part of the schema. " +
          "Furthermore, a list of ingredients is supplied. The recipe must be created using only these ingredients, but the recipe does not need to " +
          "use all of these - they are simply what the user has access to and can use for the recipe. So do not use ingredients if they are not needed for the recipe." +
          "Ensure that the recipe is of type " +
          dishType +
          " and conforms to the nutritional filters given in " +
          chosenNutritionalFilters,
      },
      {
        role: "user",
        content: [
          {
            type: "text",
            text:
              "Generate a recipe for the following dish ensuring that the recipe conforms to the nutritional filters given in " +
              chosenNutritionalFilters +
              ", and creating the recipe as close to the image provided as possible: " +
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
  const detail = JSON.parse(req.body?.detail);
  const userDetail = detail.prompt;
  const dishType = detail.dishType;
  const chosenNutritionalFilters = detail.chosenNutritionalFilters;

  let userPrompt =
    "This is a picture featuring many different cooking ingredients. Return 4 names of dishes which you believe can be cooked using only these ingredients (with the exception of small amounts of salt, pepper and oil). Ensure the dishes conform to the following nutritional filters: " +
    chosenNutritionalFilters;
  if (userDetail != "") {
    userPrompt =
      "This is a picture featuring many different cooking ingredients. Return 4 names of dishes which you believe can be cooked using only these ingredients (with the exception of small amounts of salt, pepper and oil). Ensure the dishes conform to the following nutritional filters: " +
      chosenNutritionalFilters +
      " Further detail to assist classification: " +
      userDetail;
  }

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(
      recipeListFromImageIngredientsSchemaZod(dishType),
    ),
  });

  console.log(
    "Identifying ingredients and potential dishes" +
      " with filters: " +
      chosenNutritionalFilters +
      " and type: " +
      dishType,
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, first with the ingredients that you can see present in the image in the ingredients field, and then " +
          "the dish you believe is most likely able to be cooked with these ingredients in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is satisfied and do not include more JSON. Use British English. " +
          "Write the name of each dish concisely; in less than 20 words per dish, but include detail if necessary e.g. flavour, type, characteristics." +
          "Ensure the dishes are of type + " +
          dishType +
          " and conform to the following nutritional filters: " +
          chosenNutritionalFilters,
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
  const detail = JSON.parse(req.body?.detail);
  const userDetail = detail.prompt;
  const dishType = detail.dishType;
  const chosenNutritionalFilters = detail.chosenNutritionalFilters;

  let userPrompt =
    "Return 4 names of dishes which you believe are in the image. Ensure the dishes conform to the following nutritional filters: " +
    chosenNutritionalFilters;
  if (userDetail != "") {
    userPrompt =
      "Return 4 names of dishes which you believe are in the image. Ensure the dishes conform to the following nutritional filters: " +
      chosenNutritionalFilters +
      " Further detail to assist classification: " +
      userDetail;
  }

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(recipeListFromImageSchemaZod(dishType)),
  });

  console.log(
    "Identifying dishes in the image" +
      " with filters: " +
      chosenNutritionalFilters +
      " and type: " +
      dishType,
  );

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, with the dish you believe is most likely depicted in the image in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is satisfied and do not include more JSON. Use British English. " +
          "Write the name of each dish concisely; in less than 20 words per dish, but include detail if necessary e.g. flavour, type, characteristics." +
          "Ensure the dishes are of type + " +
          dishType +
          " and conform to the following nutritional filters: " +
          chosenNutritionalFilters,
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

// generate recipes:

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

// actions on already generated recipes:

app.post("/recipe-nutrition", async (req, res) => {
  await generateAction(req, res, LlmActionType.NUTRITIONAL_VALUES);
});

app.post("/query-recipe", async (req, res) => {
  await generateAction(req, res, LlmActionType.QUESTION);
});

app.post("/alter-recipe", async (req, res) => {
  await generateAction(req, res, LlmActionType.ALTERATION);
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log("Nourish endpoints are now live."));

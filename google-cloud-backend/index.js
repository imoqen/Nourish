const { GoogleGenAI } = require("@google/genai");
const { ChatVertexAI } = require("@langchain/google-vertexai");
const express = require("express"); // to handle request/responses
const multer = require("multer"); // for handling multipart/form-data (images): https://www.npmjs.com/package/multer

const { initializeApp } = require("firebase-admin/app"); // firebase admin sdk
const { getAuth } = require("firebase-admin/auth"); // auth method from firebase
const { getFirestore, FieldValue } = require("firebase-admin/firestore"); // firestore

const { z } = require("zod");

// like an enum but in javascript (enums only supported in typescript)
// https://masteringjs.io/tutorials/fundamentals/enum
// these are the same as present in the Kotlin app
const LlmRequestType = {
  RECIPE: "RECIPE",
  DISHES_FROM_INGREDIENTS: "DISHES_FROM_INGREDIENTS",
  DISHES_FROM_IMAGE: "DISHES_FROM_IMAGE",
  INGREDIENTS_FROM_IMAGE: "INGREDIENTS_FROM_IMAGE",
  RECIPE_QUERY: "RECIPE_QUERY",
  RECIPE_ALTERATION: "RECIPE_ALTERATION",
};

// for storing image in memory with 2mb limit
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 2 * 1024 * 1024 }, // filesize is in bytes
});

// langchain:
const { createAgent, toolStrategy } = require("langchain");

const GOOGLE_CLOUD_PROJECT = "GOOGLE_CLOUD_PROJECT_NAME";
const GOOGLE_CLOUD_LOCATION = "GOOGLE_CLOUD_PROJECT_REGION";

const app = express();
app.use(express.json());

initializeApp(); // admin sdk initialised, uses application default credentials
const db = getFirestore();

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
    const prompt = req.body?.request;
    console.log(`prompt: ${prompt}`);
    console.log(`body: ${req.body}`);
    if (!validateRequest(req, res, prompt, requestType)) return; //ITS NOT RETURNING

    // Perform the LLM request and retrieve the response, different request depending on the request type
    let result;
    switch (requestType) {
      // Generate a recipe
      case LlmRequestType.RECIPE:
        result = await generateRecipe(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt,
        );

      // Generate dishes from an image
      case LlmRequestType.DISHES_FROM_IMAGE:
        result = await generateDishesFromImage(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt,
          req,
        );
    }

    if (result == null) {
      console.log("Error: LLM request failed; no result.");
      return res.status(500).json({ error: "LLM request failed; no result." });
    }

    res.json(result);
    return res;
  } catch (e) {
    console.error(e);
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
    console.log("Rate limit error:" + error);
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
  // tool strategy for formatting output:
  //https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
  const RecipeSchemaZod = z.object({
    title: z.string().describe("title of the recipe"),
    ingredients: z
      .string()
      .describe(
        "a list of the ingredients of the recipe, do not include the amounts here; these will go in ingredient_amounts and ingredient_amount_units.",
      ),
    ingredient_amounts: z
      .string()
      .describe(
        "a list of the amounts of each ingredient to use, given only as integers or floats. must correspond with the ingredient in the ingredients list.",
      ),
    ingredient_amount_units: z
      .string()
      .describe(
        "a list of the ingredient units (UK) of each ingredient amount to use (e.g. tsp). must correspond with the ingredient in the ingredients list and ingredient_amounts list.",
      ),
    servings: z
      .string()
      .describe(
        "the number of servings made of this meal using the amount of ingredients in the ingredient_amounts and ingredient_amount_units list",
      ),
    meal_type: z
      .string()
      .describe(
        "the type of meal that this corresponds to (breakfast, lunch, dinner, snack)",
      ),
    method: z
      .string()
      .describe(
        "numbered list of the recipe's method, in the form \\nX. for all X to ensure each step is on its own line",
      ),
  });

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.0-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeSchemaZod),
  });

  console.log("Generating recipe for: " + prompt);

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

  console.log(result.structuredResponse);

  return result.structuredResponse;
}

async function validateRequest(req, res, prompt, requestType) {
  // only do this check on things which require a prompt.
  // so not dishes from image.
  if (!prompt && requestType == LlmRequestType.RECIPE) {
    console.log("Error: missing or malformed prompt.");
    res.status(400).json({ error: "Missing or malformed prompt." });
    return false;
  }

  // Authenticate the user using the Bearer token in the request
  const requesterUid = await authenticateUser(req);
  if (requesterUid == null) {
    console.log("Error: invalid or non-existent bearer token");
    res.status(500).json({ error: "Invalid or non-existent Bearer token" });
    return false;
  }

  // Rate limit the user by incrementing their daily token count, ensuring
  // LLM request cannot be performed if the user has exceeded their rate.
  if (!(await rateLimitUser(requesterUid))) {
    console.log("Error: token limit reached. come back tomorrow");
    res.status(500).json({ error: "Token limit reached. Come back tomorrow" });
    return false;
  }

  // Otherwise, validated
  return true;
}

// new stuff for image handling:

// to handle rthe multipart data, multer is used:
// https://medium.com/@muhebollah.diu/understanding-multipart-form-data-the-ultimate-guide-for-beginners-fd039c04553d
async function generateDishesFromImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt, // maybe use in future.
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  const base64encoded = file.buffer.toString("base64"); // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const dataUrl = `data:${filetype};base64,${base64encoded}`; // required format for gemini 2.0 flash

  console.log("Sending image to LLM...");

  // tool strategy for formatting output:
  //https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
  const RecipeListSchemaZod = z.object({
    recipes: z
      .string()
      .describe("4 possible meals that this image could be an image of."),
  });

  // https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.0-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(RecipeListSchemaZod),
  });

  //console.log("Return 4 names of meals which you believe are in the image: " + prompt);

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, in the recipes field. Write the name of each meal concisely; in less than 20 words per meal.",
      },
      {
        role: "user",
        content: [
          {
            type: "text",
            text: "Return 4 names of meals which you believe are in the image.", // put prompt variable in here once set up
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

app.post("/from-image", upload.single("image"), async (req, res) => {
  await generate(req, res, LlmRequestType.DISHES_FROM_IMAGE);
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log("Nourish endpoints are now live."));

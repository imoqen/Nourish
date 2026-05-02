/**
 * The Google Cloud Service, for LLM interaction within the Nourish Application.
 * To bring up the service:
 * gcloud run deploy llm --source . --region GOOGLE_CLOUD_PROJECT_REGION --allow-unauthenticated
 *
 * @author Imogen Pemberton, 2026
 */

const express = require("express"); // To handle request/responses
// To handle multipart/form-data (images): https://www.npmjs.com/package/multer
const multer = require("multer");

const { initializeApp } = require("firebase-admin/app"); // Firebase Admin sdk
const { getAuth } = require("firebase-admin/auth"); // Auth method from firebase
const { getFirestore, FieldValue } = require("firebase-admin/firestore"); // Firestore

// Custom LLM calls, defined in llm-calls.js:
const {
  generateNutritionalValues,
  generateQuestionAnswer,
  generateAlteredRecipe,
  generateRecipe,
  generateRecipeFromDishImage,
  generateDishesFromIngredients,
  generateDishesFromIngredientsImage,
  generateDishesFromImage,
} = require("./llm-calls.js");

// Javascript-style enums (real enums are only supported in typescript)
// these are the same as present in the Nourish Kotlin app

// LLM Requests:
const LlmRequestType = {
  RECIPE: "RECIPE",
  RECIPE_FROM_IMAGE_AND_DISH: "RECIPE_FROM_IMAGE_AND_DISH",
  DISHES_FROM_INGREDIENTS: "DISHES_FROM_INGREDIENTS",
  DISHES_FROM_IMAGE: "DISHES_FROM_IMAGE",
  DISHES_FROM_INGREDIENTS_IMAGE: "DISHES_FROM_INGREDIENTS_IMAGE",
  RECIPE_QUERY: "RECIPE_QUERY",
  RECIPE_ALTERATION: "RECIPE_ALTERATION",
};

// LLM Action Requests:
const LlmActionType = {
  NUTRITIONAL_VALUES: "NUTRITIONAL_VALUES",
  ALTERATION: "ALTERATION",
  QUESTION: "QUESTION",
};

// To store image in memory with 2mb limit:
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 2 * 1024 * 1024 }, // filesize is in bytes
});

const GOOGLE_CLOUD_PROJECT = "GOOGLE_CLOUD_PROJECT_NAME";
const GOOGLE_CLOUD_LOCATION = "GOOGLE_CLOUD_PROJECT_REGION";

const app = express();
app.use(express.json());

initializeApp(); // Admin SDK initialised, uses application default credentials
const db = getFirestore();

/**
 * Receives the request sent from the Nourish app and builds the response accordingly.
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
 * @param requestType - the LlmRequestType, defining the type of request
 *                      (generate recipe, dishes from image, etc).
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

    // Perform the LLM request and retrieve the response, different request
    // depending on the request type
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

/**
 * Receives the Action request sent from the Nourish app and builds the response
 * accordingly, as with recipe/dish generation requests.
 * @param req - The request handled by express, to be dealt with
 * @param res - The response handled by express, to be built up
 * @param requestType - the LlmActionType, defining the type of action request
 *                      (nutrition, alteration, or query). Handled accordingly.
 * @returns The response, containing its result and a 500 error with detail if
 *          an error occurred.
 */
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

    // Perform the LLM request and retrieve the response, different request
    // depending on the request type
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

// Error Validation:

/**
 * returns 400 errors (Bad Request) to signify error on the client's input;
 * invalid syntax, malformed data, invalid Bearer token
 * @param req - The request handled by express, to be dealt with
 * @param prompt - The user-entered prompt.
 * @param requestType - the LlmRequestType, defining the type of request
 *                      (generate recipe, dishes from image, etc).
 *                      thus, will be handled accordingly.
 * @returns an error message, or "" if no error occurred.
 */
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

// Endpoints:

/**
 * LlmRequest Endpoints, to predict Dishes and generate Recipes
 */
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

/**
 * LlmActionRequest Endpoints, to perform actions on existing Recipes
 */
app.post("/recipe-nutrition", async (req, res) => {
  await generateAction(req, res, LlmActionType.NUTRITIONAL_VALUES);
});

app.post("/query-recipe", async (req, res) => {
  await generateAction(req, res, LlmActionType.QUESTION);
});

app.post("/alter-recipe", async (req, res) => {
  await generateAction(req, res, LlmActionType.ALTERATION);
});

// Run the Google Cloud Service, and bring up the endpoints:
const port = process.env.PORT || 8080;
app.listen(port, () => console.log("Nourish endpoints are now live."));

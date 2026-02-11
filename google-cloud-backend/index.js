const { GoogleGenAI } = require("@google/genai");
const express = require("express"); // to handle request/responses

const { initializeApp } = require("firebase-admin/app"); // firebase admin sdk
const { getAuth } = require("firebase-admin/auth"); // auth method from firebase
const { getFirestore, FieldValue } = require("firebase-admin/firestore"); // firestore

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
 * @returns The response, containing its result and a 500 error with detail if
 *          an error occurred.
 */
async function generate(req, res) {
  try {
    // Extract and validate the prompt
    const prompt = req.body?.prompt;
    if (!prompt) {
      console.log("Error: missing or malformed prompt.");

      return res.status(400).json({ error: "Missing or malformed prompt." });
    }

    // Authenticate the user using the Bearer token in the request
    const requesterUid = await authenticateUser(req);
    if (requesterUid == null) {
      console.log("Error: invalid or non-existent bearer token");

      return res
        .status(500)
        .json({ error: "Invalid or non-existent Bearer token" });
    }

    // Rate limit the user by incrementing their daily token count, ensuring
    // LLM request cannot be performed if the user has exceeded their rate.
    if (!(await rateLimitUser(requesterUid))) {
      console.log("Error: token limit reached. come back tomorrow");
      return res
        .status(500)
        .json({ error: "Token limit reached. Come back tomorrow" });
    }

    // Perform the LLM request and retrieve the response
    const text = await generateContent(
      GOOGLE_CLOUD_PROJECT,
      GOOGLE_CLOUD_LOCATION,
      prompt,
    );
    res.json({ text });
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
async function generateContent(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt,
) {
  const client = new GoogleGenAI({
    vertexai: true,
    project: projectId,
    location: location,
  });

  const response = await client.models.generateContent({
    model: "gemini-2.0-flash",
    contents: prompt,
  });

  return response.text;
}

// To run the POST endpoint:
app.post("/generate", generate);
const port = process.env.PORT || 8080;
app.listen(port, () => console.log("Nourish /generate endpoint live."));

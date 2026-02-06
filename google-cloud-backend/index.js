const { GoogleGenAI } = require('@google/genai');
const express = require('express')

// firebase admin SDK and firestore:
const { initializeApp, applicationDefault, cert } = require('firebase-admin/app'); // firebase admin sdk
// authentication for users:
const { getAuth } = require("firebase-admin/auth"); // auth method from firebase
// firestore:
const { getFirestore, Timestamp, FieldValue, Filter } = require('firebase-admin/firestore');

const GOOGLE_CLOUD_PROJECT = 'GOOGLE_CLOUD_PROJECT_NAME';
const GOOGLE_CLOUD_LOCATION = 'GOOGLE_CLOUD_PROJECT_REGION';

const app = express();
app.use(express.json());

initializeApp(); // admin sdk initialised, uses application default credentials

// the firestore database:
const db = getFirestore();

async function generateContent(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt
) {
  const client = new GoogleGenAI({
    vertexai: true,
    project: projectId,
    location: location,
  });

  const response = await client.models.generateContent({
    model: 'gemini-2.0-flash',
    contents: prompt,
  });

  console.log(response.text);

  return response.text;
}

async function generate(
  req,
  res
) {
  try {
    const prompt = req.body?.prompt;
    if (!prompt) return res.status(400).json({ error: "Missing or malformed prompt." });

    const tokenIsValid = await validateToken(req)
    console.log("token validation result: " + tokenIsValid)

    if (tokenIsValid) {

    const text = await generateContent(
      GOOGLE_CLOUD_PROJECT,
      GOOGLE_CLOUD_LOCATION,
      prompt
    );

    res.json({ text });

    } else {
      res.status(500).json({ error: "Invalid or non existent bearer token" });
    }

  } catch (e) {
    console.error(e);
    res.status(500).json({ error: e.message });
  }
   
}

async function validateToken(req) {
  const auth = req.get("Authorization"); // gets the authorisation header via express (Authorization)
  console.log("header: " + auth)
  if (auth == null) {
    // auth header is missing
    return false
  }
  // check that it is a Bearer token
  if (!auth.startsWith("Bearer ")) {
    // early return, not authenticated - no bearer token.
    return false
  }
    const bearer = auth.slice(7) // slice the "Bearer " out to get only the token
    try {
      const decodedToken = await getAuth().verifyIdToken(bearer)
      console.log("user who made req: " + decodedToken.uid)
      // code reached this point therefore it must be valid
      return true
    } catch (e) {
      // validation failed.
      return false
    }
}

app.post("/generate", generate);
const port = process.env.PORT || 8080;
app.listen(port, () =>
  console.log('Generate endpoint running')
);
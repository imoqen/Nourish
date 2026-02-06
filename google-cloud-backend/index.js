const { GoogleGenAI } = require('@google/genai');
const express = require('express')

// firebase admin SDK and firestore:
const { initializeApp } = require('firebase-admin/app'); // firebase admin sdk
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

async function rateLimitUser(uid) {
  const userRef = db.collection("users").doc(uid)

  try {
  const userDoc = await userRef.get()

  console.log("Doc gotten successfully: " + JSON.stringify(userDoc))
  if (userDoc.exists) { // use .exists property not .exists(). exists is for admin SDK which is used in the cloud run service. .exists() is for the client app (kotlin)
    //console.log("SUCCESS: User " + uid + " data: " + JSON.stringify(doc.data()));
    const dateToday = new Date().toJSON().slice(0,10);
    //const dailyTokensRef = userRef.collection("token-usage").doc(dateToday) // check if document exists for token usage today. if it doesnt, create it with value 0. if it does, increment it.
    //console.log("daily tokens: " + JSON.stringify(dailyTokensRef))
    console.log("doc exists")

    //const collection = admin.firestore().collection("collection")

    // check that token limit for today has not been exceeded:
    const tokenAmountDoc = await userRef.collection("token-usage").doc(dateToday).get()
    if (tokenAmountDoc.exists) {
      if (tokenAmountDoc.get("amount") < 100) {
        console.log("amount is less than 100 - valid.")
        await updateUserTokenCount(userRef, dateToday, 1);
        return true;
      } else {
        console.log("amount exceeds 100 - invalid.")
        return false;
      }
    } else {
      console.log("amount = 0, valid.")
      await updateUserTokenCount(userRef, dateToday, 1);
      return true;
    }
  }

   else {
    console.log("ERROR: User " + uid + " could not be found!");
  }

  }
  catch(error) {
    console.log("Error " + JSON.stringify(userRef) + "getting document:" + error);
  }
  
}

async function updateUserTokenCount(userRef, date, incrementAmount) {
  const inc = FieldValue.increment(incrementAmount) // will increment the value by the increment amount  
  userRef.collection("token-usage").doc(date).set({amount: inc}, {merge: true}).then(
    console.log("successfully updated token usage.")
  )
}

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

    const requesterUid = await authenticateUser(req)
    if (requesterUid == null) return res.status(500).json({ error: "Invalid or non existent bearer token" });

    if (! await rateLimitUser(requesterUid)) return res.status(500).json({ error: "Token limit exceeded. Come back tomorrow" });

    // if not null, user must be authenticated with uid requesterUid found in authenticateUser
  
      // if can successfully rate limit, allow the user to query the llm. otherwise return "rate limit exceeded" error
    

        const text = await generateContent(
          GOOGLE_CLOUD_PROJECT,
          GOOGLE_CLOUD_LOCATION,
          prompt
        );
        res.json({ text });

  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: e.message });
  }
   
}

// returns:
// null if user not authenticated. uid of the user if authenticated.
async function authenticateUser(req) {
  const auth = req.get("Authorization"); // gets the authorisation header via express (Authorization)
  console.log("header: " + auth)
  if (auth == null) {
    // auth header is missing
    return null
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
      //await rateLimitUser(decodedToken.uid)
      // code reached this point therefore it must be valid
      return decodedToken.uid
    } catch (e) {
      // validation failed.
      return null
    }
}

app.post("/generate", generate);
const port = process.env.PORT || 8080;
app.listen(port, () =>
  console.log('Generate endpoint running')
);
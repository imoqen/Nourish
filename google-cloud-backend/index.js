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

async function getUserData(uid) {
  const userRef = db.collection("users").doc(uid)
  userRef.get().then((userDoc) => {
    console.log("Doc gotten successfully: " + JSON.stringify(userDoc))
    if (userDoc.exists) { // use .exists property not .exists(). exists is for admin SDK which is used in the cloud run service. .exists() is for the client app (kotlin)
      //console.log("SUCCESS: User " + uid + " data: " + JSON.stringify(doc.data()));
      const dateToday = new Date().toJSON().slice(0,10);
      //const dailyTokensRef = userRef.collection("token-usage").doc(dateToday) // check if document exists for token usage today. if it doesnt, create it with value 0. if it does, increment it.
      //console.log("daily tokens: " + JSON.stringify(dailyTokensRef))
      console.log("doc exists")

      //const collection = admin.firestore().collection("collection")

      // check that token limit for today has not been exceeded:
      userRef.collection("token-usage").doc(dateToday).get().then((tokenDoc) => {
        if (tokenDoc.exists) {
          if (tokenDoc.get("amount") < 100) {
            console.log("amount is less than 100 - valid.")
            const inc = FieldValue.increment(1)      
            // increments by 1 , and makes it if it doesnt exist:
            userRef.collection("token-usage").doc(dateToday).set({amount: inc}, {merge: true}).then(
              console.log("successfully updated token usage.")
             )

          } else {
            console.log("amount exceeds 100 - invalid.")
          }
        } else {
          console.log("amount = 0, valid.")
          const inc = FieldValue.increment(1)      
          // increments by 1 , and makes it if it doesnt exist:
          userRef.collection("token-usage").doc(dateToday).set({amount: inc}, {merge: true}).then(
           console.log("successfully updated token usage.")
          )

        }
      })

      /*dailyTokensRef.get().then((dailyTokensDoc) => {
        if (dailyTokensDoc.exists) {
          console.log("SUCCESS: daily tokens doc exists. need to increment with value +1")
          dailyTokensRef.set({
            amount: dailyTokensRef.data.amount + 1
          }).then(() => {
              console.log("Document successfully written!");
          })
          .catch((error) => {
              console.error("Error writing document: ", error);
          });
        } else {
          console.log("doesnt exist - need to make with value 1")
          dailyTokensRef.set({
            amount: 0
          }).then(() => {
              console.log("Document successfully written!");
          })
          .catch((error) => {
              console.error("Error writing document: ", error);
          });
        }
      })*/

    } else {
      console.log("ERROR: User " + uid + " could not be found!");
    }
  }).catch((error) => {
    console.log("Error " + JSON.stringify(userRef) + "getting document:" + error);
  })

  
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
      getUserData(decodedToken.uid)
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
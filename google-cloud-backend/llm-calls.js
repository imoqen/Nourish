/**
 * Llm Calls sent to Gemini, using LangChain to create a simple Agent and provide
 * it with the Zod schemas for each request type.
 * Each function in this module makes the LLM request for the corresponding
 * type, using Google Vertex AI to call the LLM and retrieve the response.
 *
 * VertexAI: https://docs.langchain.com/oss/javascript/integrations/chat/google_vertex_ai
 *
 * @author Imogen Pemberton, 2026
 */

// LangChain:
import { createAgent, toolStrategy } from "langchain";
// VertexAI:
import { ChatVertexAI } from "@langchain/google-vertexai";

// Custom LangChain schemas, defined in langchain-schemas.js:
import {
  NutritionSchemaZod,
  QuestionAnswerSchemaZod,
  RecipeSchemaZod,
  DishListFromIngredientsSchemaZod,
  DishListFromImageIngredientsSchemaZod,
  DishListFromImageSchemaZod,
} from "./langchain-schemas.js";

/**
 * LlmActionRequest: To generate nutritional values of the provided recipe.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param recipe - The recipe to generate the nutrition of.
 * @returns The LLM response.
 */
export async function generateNutritionalValues(
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

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema and ensure it is given for 1 serving. " +
          "The ingredients and their amounts in the recipe are provided for 1 " +
          "serving, so calculate the nutrition using these values - do not scale " +
          "anything. Use British standards. " +
          "Generate the nutritional values as requested in the schema for the " +
          "provided recipe. If your answer contains a decimal, ensure it is to " +
          "at most 2 decimal places.",
      },
      {
        role: "user",
        content:
          "Generate the nutritional values for the following recipe: " +
          JSON.stringify(recipe),
      },
    ],
  });
  return result.structuredResponse;
}

/**
 * LlmActionRequest: To generate an answer to a user's question about a recipe.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param query - The user's query about the recipe.
 * @param recipe - The recipe to generate the answer to the question of.
 * @returns The LLM response.
 */
export async function generateQuestionAnswer(
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

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not " +
          "too much (keep below 100 words). Use British English. " +
          "Generate an answer to the question provided to the user, regarding " +
          "the recipe also provided by the user." +
          "As the question is regarding food, ensure you conform to food safety " +
          "and hygiene standards but only be explicit about this if the user " +
          "specifically asks something regarding food safety/hygiene/storage.",
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
  return result.structuredResponse;
}

/**
 * LlmActionRequest: To generate an altered recipe.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param query - The user's query regarding how they wish the recipe to be altered.
 * @param recipe - The recipe to generate an altered recipe of.
 * @param allNutritionalFilters - All nutritional filters authored by the user.
 * @param dishType - The dish type requested by the user, or "" if none selected.
 * @returns The LLM response.
 */
export async function generateAlteredRecipe(
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
      RecipeSchemaZod(allNutritionalFilters, dishType),
    ),
  });

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not " +
          "too much (keep below 250 words). Use British English. " +
          "Generate a new recipe using the provided recipe, ensuring it is " +
          "altered to the user's needs accordingly." +
          " The provided amounts in the recipe make 1 serving. Create your " +
          "recipe for 1 serving too, but preserve the 'serving' field as the " +
          "SAME value given, as this is used for recipe scaling separately. " +
          "The recipe should remain the same for any part which does not need " +
          "to be altered to the request." +
          "If the recipe cannot be altered in the way that the user wishes " +
          "(e.g. inedible), fill in the recipeNotPossible field as true and " +
          "all others as null. Only do this if there really is no way to alter " +
          "it, otherwise be creative to meet the user's needs.",
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
  return result.structuredResponse;
}

/**
 * LlmRequest: Generates a recipe.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param prompt - The prompt to send to the LLM, combined with the system prompt
 *                 and user written prompt from the Nourish app.
 * @param allNutritionalFilters - All nutritional filters authored by the user.
 * @param chosenNutritionalFilters - A subset of allNutritionalFilters, the filters
 *                                   which were selected by the user and thus must
 *                                   be applied to the recipe to be generated.
 * @param dishType - The dish type requested by the user, or "" if none selected.
 * @returns The LLM response.
 */
export async function generateRecipe(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt,
  allNutritionalFilters,
  chosenNutritionalFilters,
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
      RecipeSchemaZod(allNutritionalFilters, dishType),
    ),
  });

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not " +
          "too much (keep below 250 words). Use British English. Do not include " +
          "absolute amounts in the method part of the schema. Ensure that the " +
          "recipe produced conforms to the following nutritional requirements " +
          "and are thus listed in the tags array of the schema (and any more " +
          "tags which you deem appropriate for the recipe): " +
          chosenNutritionalFilters +
          " and the recipe must be of type: " +
          dishType,
      },
      {
        role: "user",
        content:
          "Create a recipe for the following dish, ensuring that the recipe " +
          "produced conforms to these nutritional requirements: " +
          chosenNutritionalFilters +
          " and the recipe must be of type: " +
          dishType +
          ". The recipe to generate is: " +
          prompt,
      },
    ],
  });
  return result.structuredResponse;
}

/**
 * LlmRequest: Generates a recipe from an image of a dish. This is after a Dish
 * has been chosen, and then a recipe for this Dish is generated.
 * Multer is required to handle Multipart data (images):
 * https://medium.com/@muhebollah.diu/understanding-multipart-form-data-the-ultimate-guide-for-beginners-fd039c04553d
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param req - The request object, sent directly here so that the image remains
 *              contained, and extracted using Multer.
 * @returns The LLM response.
 */
export async function generateRecipeFromDishImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const base64encoded = file.buffer.toString("base64");
  // required format for gemini 2.5 flash:
  const dataUrl = `data:${filetype};base64,${base64encoded}`;

  // User can supply further detail if they wish, but this is optional:
  const detail = JSON.parse(req.body?.detail);
  const dish = detail.prompt;
  const dishType = detail.dishType;
  const chosenNutritionalFilters = detail.chosenNutritionalFilters;
  const allNutritionalFilters = detail.allNutritionalFilters;

  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(
      RecipeSchemaZod(allNutritionalFilters, dishType),
    ),
  });

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, write in sufficient detail but not " +
          "too much (keep below 250 words). Use British English. Do not include " +
          "absolute amounts in the method part of the schema. " +
          "Furthermore, a list of ingredients is supplied. The recipe must be " +
          "created using only these ingredients, but the recipe does not need to " +
          "use all of these - they are simply what the user has access to and " +
          "can use for the recipe. So do not use ingredients if they are not " +
          "needed for the recipe." +
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
              "Generate a recipe for the following dish ensuring that the " +
              "recipe conforms to the nutritional filters given in " +
              chosenNutritionalFilters +
              ", and creating the recipe as close to the image provided as possible. " +
              dish,
          },
          { type: "image_url", image_url: { url: dataUrl } },
        ],
      },
    ],
  });
  return result.structuredResponse;
}

/**
 * LlmRequest: Generates 4x possible dishes from a String list of ingredients.
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param prompt - The prompt to send to the LLM, combined with the system prompt
 *                 and user written prompt from the Nourish app.
 * @param chosenNutritionalFilters - A subset of allNutritionalFilters, the filters
 *                                   which were selected by the user and thus must
 *                                   be applied to the recipe to be generated.
 * @param dishType - The dish type requested by the user, or "" if none selected.
 * @returns The LLM response.
 */
export async function generateDishesFromIngredients(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  prompt,
  chosenNutritionalFilters,
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
    responseFormat: toolStrategy(DishListFromIngredientsSchemaZod(dishType)),
  });

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, with the best dish you believe can be " +
          "cooked from only these ingredients in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is " +
          "satisfied and do not include more JSON. Use British English. " +
          "Write the name of each dish concisely; in less than 20 words per " +
          "dish, but include detail if necessary e.g. flavour, type, characteristics." +
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
          ", which you believe can be cooked at home using only these " +
          "ingredients (with the exception of cooking ingredients which are " +
          "almost always present - small amounts of salt, pepper and oil. With " +
          "that being said, though, do not include the salt/pepper/oil if it " +
          "is not relevant to the recipe): " +
          prompt,
      },
    ],
  });
  return result.structuredResponse;
}

/**
 * LlmRequest: Generates 4x possible dishes from an image of ingredients.
 * Multer is required to handle Multipart data (images):
 * https://medium.com/@muhebollah.diu/understanding-multipart-form-data-the-ultimate-guide-for-beginners-fd039c04553d
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param req - The request object, sent directly here so that the image remains
 *              contained, and extracted using Multer.
 * @returns The LLM response.
 */
export async function generateDishesFromIngredientsImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const base64encoded = file.buffer.toString("base64");
  // required format for gemini 2.5 flash
  const dataUrl = `data:${filetype};base64,${base64encoded}`;

  // User can supply further detail if they wish, but this is optional:
  const detail = JSON.parse(req.body?.detail);
  const userDetail = detail.prompt;
  const dishType = detail.dishType;
  const chosenNutritionalFilters = detail.chosenNutritionalFilters;

  let userPrompt =
    "This is a picture featuring many different cooking ingredients. Return 4 " +
    "names of dishes which you believe can be cooked using only these " +
    "ingredients (with the exception of small amounts of salt, pepper and oil). " +
    "Ensure the dishes conform to the following nutritional filters: " +
    chosenNutritionalFilters;
  if (userDetail != "") {
    userPrompt =
      "This is a picture featuring many different cooking ingredients. Return " +
      "4 names of dishes which you believe can be cooked using only these " +
      "ingredients (with the exception of small amounts of salt, pepper and oil). " +
      "Ensure the dishes conform to the following nutritional filters: " +
      chosenNutritionalFilters +
      " Further detail to assist classification: " +
      userDetail;
  }

  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(
      DishListFromImageIngredientsSchemaZod(dishType),
    ),
  });

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, first with the ingredients that you " +
          "can see present in the image in the ingredients field, and then " +
          "the dish you believe is most likely able to be cooked with these " +
          "ingredients in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema is " +
          "satisfied and do not include more JSON. Use British English. " +
          "Write the name of each dish concisely; in less than 20 words per " +
          "dish, but include detail if necessary e.g. flavour, type, characteristics." +
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
  return result.structuredResponse;
}

/**
 * LlmRequest: Generates 4x possible dishes from an image of a dish.
 * Multer is required to handle Multipart data (images):
 * https://medium.com/@muhebollah.diu/understanding-multipart-form-data-the-ultimate-guide-for-beginners-fd039c04553d
 * @param projectId - ID of the Google Cloud project
 * @param location - Location of the Google Cloud project
 * @param req - The request object, sent directly here so that the image remains
 *              contained, and extracted using Multer.
 * @returns The LLM response.
 */
export async function generateDishesFromImage(
  projectId = GOOGLE_CLOUD_PROJECT,
  location = GOOGLE_CLOUD_LOCATION,
  req,
) {
  // get the image file:
  const file = req.file;
  const filetype = file.mimetype;
  // https://stackoverflow.com/questions/51798811/how-to-encoded-files-in-base64-in-nodejs-after-getting-details-from-client-side
  const base64encoded = file.buffer.toString("base64");
  // required format for gemini 2.5 flash
  const dataUrl = `data:${filetype};base64,${base64encoded}`;

  // User can supply further detail if they wish, but this is optional:
  const detail = JSON.parse(req.body?.detail);
  const userDetail = detail.prompt;
  const dishType = detail.dishType;
  const chosenNutritionalFilters = detail.chosenNutritionalFilters;

  let userPrompt =
    "Return 4 names of dishes which you believe are in the image. " +
    "Ensure the dishes conform to the following nutritional filters: " +
    chosenNutritionalFilters;
  if (userDetail != "") {
    userPrompt =
      "Return 4 names of dishes which you believe are in the image. " +
      "Ensure the dishes conform to the following nutritional filters: " +
      chosenNutritionalFilters +
      " Further detail to assist classification: " +
      userDetail;
  }

  const llm = new ChatVertexAI({
    project: projectId,
    location: location,
    model: "gemini-2.5-flash",
  });

  const agent = createAgent({
    model: llm,
    tools: [],
    responseFormat: toolStrategy(DishListFromImageSchemaZod(dishType)),
  });

  const result = await agent.invoke({
    messages: [
      {
        role: "system",
        content:
          "Return in the required schema, with the dish you believe is most " +
          "likely depicted in the image in firstMostLikelyDish field " +
          "and the other fields filled accordingly. Ensure the JSON schema " +
          "is satisfied and do not include more JSON. Use British English. " +
          "Write the name of each dish concisely; in less than 20 words per " +
          "dish, but include detail if necessary e.g. flavour, type, characteristics." +
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
  return result.structuredResponse;
}

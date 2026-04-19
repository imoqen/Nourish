/**
 * Contains the LangChain schemas, using Zod.
 * These will be filled by the LLM, strict such that exact JSON must be fulfilled.
 * Uses Zod, which is outlined in the LangChain documentation:
 * https://docs.langchain.com/oss/javascript/langchain/structured-output#tool-calling-strategy
 *
 * All schemas used in the Nourish app are:
 *  - LlmRequest:
 *      RecipeSchemaZod, DishListFromIngredientsSchemaZod,
 *      DishListFromImageIngredientsSchemaZod, DishListFromImageSchemaZod,
 *        (IngredientSchemaZod is used indirectly, in RecipeSchemaZod)
 *
 *  - LlmActionRequest:
 *      NutritionSchemaZod, QuestionAnswerSchemaZod,
 *
 * @author Imogen Pemberton, 2026
 */

// For structuring LangChain outputs:
const { z } = require("zod");

// Units: given as metric, something to address in the future would be also
// adding support for imperial which the software has the capacity to do but was
// not implemented for simplicity.
const metricUnits =
  "TSP, TBSP, ML, G, CLOVE, HEAD, BUNCH, SPRIG, BULB, STALK, STICK, PIECE, " +
  "LEAF, CM, DASH, DRIZZLE, PINCH, SPLASH, DROP, HANDFUL, SLICE, LOAF, ROLL, " +
  "SHEET, BLOCK, BAR, UNIT, PINT";
const ingredientUnitList = metricUnits.split(",").map((v) => v.trim());

// Recipe Nutrition:
export const NutritionSchemaZod = z.object({
  servings: z
    .number()
    .int()
    .describe(
      "The number of servings must be 1. The provided recipe amounts and " +
        "ingredients are given for 1 serving, so ensure the nutrition you provide " +
        "is for 1 serving.",
    ),
  calories: z
    .number()
    .int()
    .describe(
      "The estimated number of calories, in kcal, in this dish for 1 serving. " +
        "Calculate this using the servings, ingredients, amounts, and units in " +
        "the provided recipe.",
    ),
  fat: z
    .number()
    .describe(
      "The estimated amount of fat, in grams, in this dish for 1 serving. " +
        "Calculate this using the servings, ingredients, amounts, and units in " +
        "the provided recipe.",
    ),
  saturates: z
    .number()
    .describe(
      "The estimated amount of saturated fat ('of which saturates' so fill this " +
        "in after the fat field), in grams, in this dish for 1 serving. Calculate " +
        "this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  carbohydrate: z
    .number()
    .describe(
      "The estimated amount of carbohydrates, in grams, in this dish for 1 " +
        "serving. Calculate this using the servings, ingredients, amounts, and " +
        "units in the provided recipe.",
    ),
  sugars: z
    .number()
    .describe(
      "The estimated amount of sugars ('of which sugars' so fill this in after " +
        "the carbohydrate field), in grams, in this dish for 1 serving. Calculate " +
        "this using the servings, ingredients, amounts, and units in the provided recipe.",
    ),
  protein: z
    .number()
    .describe(
      "The estimated amount of protein, in grams, in this dish for 1 serving. " +
        "Calculate this using the servings, ingredients, amounts, and units in the " +
        "provided recipe.",
    ),
  salt: z
    .number()
    .describe(
      "The estimated amount of salt, in grams, in this dish for 1 serving. " +
        "Calculate this using the servings, ingredients, amounts, and units in " +
        "the provided recipe.",
    ),
  fibre: z
    .number()
    .describe(
      "The estimated amount of fibre, in grams, in this dish for 1 serving. " +
        "Calculate this using the servings, ingredients, amounts, and units in " +
        "the provided recipe.",
    ),
});

// Recipe Question:
export const QuestionAnswerSchemaZod = z.object({
  answer: z
    .string()
    .describe(
      "The answer to the user's question about the provided recipe. Keep it " +
        "clear and concise, but ensure to go into as much detail as necessary to " +
        "answer the question sufficiently. Answer in British English.",
    ),
});

// Recipe Alteration: simply uses the existing recipe schema.

// Ingredient of a Recipe:
// order is attempted to ensure that the result makes the most logical sense;
// e.g. LLM does not first fill in the amount as 1 (to use cups) but then
// realises cup is not an option.
// Not exported as it is local and auxiliary; only used in RecipeSchemaZod.
const IngredientSchemaZod = z.object({
  ingredientSingular: z
    .string()
    .describe(
      "Fill this in first. An ingredient required for this recipeSection, in " +
        "its singular form. Do not specify details such as size; simply give the ingredient " +
        "(type should be specified only if necessary, e.g. brown sugar vs " +
        "granulated sugar). Also be careful if adding baking soda / bicarbonate " +
        "of soda; ensure it is not too much and only use if necessary - most of " +
        "the time, baking powder should be used instead. If using yeast, ensure " +
        "the amount is valid and in grams (e.g. 7 grams active dry yeast for " +
        "500g flour). If the ingredient is 'garlic', only write 'garlic', " +
        "not 'garlic clove' as this will be covered by the ingredientAmountUnit " +
        "(CLOVE). Apply this rule to other ingredients where you see fit. " +
        "Do not include the amount or unit here; this will go in ingredientAmount " +
        "and ingredientAmountsUnit, respectively. Do not include temperature " +
        "characteristics of the ingredient, write them exactly as how they would " +
        "be bought in a shop / shopping list. Include any temperature details in " +
        "the method instead (e.g. if there is milk and it should be warmed, the " +
        "ingredient should only be 'milk' and outline the warming step in the method.)",
    ),
  ingredientPlural: z
    .string()
    .describe(
      "Fill this in second. The ingredient corresponding to ingredientSingular, " +
        "in its correct English plural form, as would appear in a recipe. " +
        "For example, egg -> eggs, flour -> flour, powder -> powder " +
        "Do not include the amount or unit here; this will go in ingredientAmount " +
        "and ingredientAmountsUnit, respectively.",
    ),
  ingredientAmountUnit: z
    .enum(ingredientUnitList)
    .describe(
      "Fill this in third. The unit of the ingredient corresponding to " +
        "ingredientSingular, and the amount given in ingredientAmount. " +
        "Ensure that the unit makes sense, for example only use an approximate " +
        "unit (such as splash, sprinkle, dash, etc) if the corresponding amount " +
        "itself is a whole number - otherwise use a unit reflective of the " +
        "precision (e.g. tsp, tbsp, ml)" +
        "Return only a value from the provided list, exactly as it is in the " +
        "list (as it will be mapped using a predefined enum); the value you " +
        "return MUST be in the list." +
        " If you think that an imperial unit would be required, then give its " +
        "value in g/kg instead (e.g. rather than using cup, as this is not used in Europe/UK). " +
        " If the ingredient requires KG, then convert the value to G and use that. " +
        "Likewise, if the ingredient requires L, then convert the value to ML and use that. " +
        "Only use UNIT if the ingredient has no measurement/unit; it is a piece itself " +
        "(e.g. egg, whole fruit, whole vegetable). In particular, if it is a spice, " +
        "baking agent or herb, do not use UNIT. For these you should use tsp/tbsp as " +
        "this is most common and easy to measure, unless you believe another " +
        "unit is more appropriate.",
    ),
  ingredientAmount: z
    .number()
    .describe(
      "Fill this in last. The amount of the ingredient corresponding to " +
        "ingredientSingular, given only as an integer or float. Furthermore, " +
        "it must correspond with the unit given in the ingredientAmountUnit.",
    ),
});

/**
 * Recipe generation:
 * used as a function so that parameters can be passed
 * @param allNutritionalFilters  - all nutritional filters authored by a user.
 * @param dishType - the selected dish type, or "" if none was selected.
 * @returns the Recipe Schema zod object.
 */
export function RecipeSchemaZod(allNutritionalFilters, dishType) {
  const nutritionalFilterList = allNutritionalFilters
    .split(",")
    .map((v) => v.trim());
  const dishTypeList = dishType.split(",").map((v) => v.trim());

  return z.object({
    title: z
      .string()
      .describe("Title of the recipe, must be 42 characters or less."),

    sectionIngredients: z.array(
      z.object({
        section: z
          .string()
          .describe(
            "A heading for this list of ingredients, for example " +
              "'For the ...' e.g. 'For the filling'",
          ),
        ingredients: z.array(IngredientSchemaZod),
      }),
    ),

    servings: z
      .number()
      .int()
      .describe(
        "The number of servings (not pieces) made of this meal using the " +
          "amount of ingredients in the ingredientAmounts and " +
          "ingredientAmountsUnits list. Given as an integer.",
      ),

    mealType: z
      .enum(dishTypeList)
      .describe(
        "The type of meal that this corresponds to, returned exactly and with " +
          "the same capitalisation as ONE element from the list ",
      ),

    method: z
      .array(z.string())
      .describe(
        "An array of the recipe's method, each array element being an " +
          "individual step. Ensure that the array is ordered from first to last " +
          "step, and do not include step numbers. " +
          "For step(s) involving heating the oven, ensure you specify Celsius " +
          "and Farenheit and different oven options (Fan, Gas mark etc) " +
          "When writing the units e.g. ML, ensure they correspond with those " +
          "you wrote in the sectionIngredients array, but write them in the " +
          "human-readable form (e.g. ML -> ml, DASH -> dash) " +
          "Do NOT include any absolute measurements/amounts within the steps " +
          "(e.g. 100ml of ingredient X), only refer to the ingredients " +
          "themselves (e.g. ingredient X) (as the recipe will be able to be " +
          "scaled by the user). If you do need to state measurements, only " +
          "use relative e.g. half of ingredient X etc." +
          "For the final step, include a nice, upbeat happy step such as " +
          "'Enjoy your homemade ...'",
      ),

    tags: z.array(
      z
        .enum(nutritionalFilterList)
        .describe(
          "Fill this in once the recipe has been generated, do not fill this " +
            "in first and then fill in the recipe based on it: " +
            "A list of 'tags' which apply to this recipe, as a comma separated " +
            "list e.g. 'a, b, c' " +
            "Each tag must be from the list, do not include more (Be strict " +
            "with this; e.g. it is only NO-ADDED-SUGAR if no extra sugar has " +
            "been added - sugar sources such as maple syrup, honey etc still " +
            "count as added sugar.) ",
        ),
    ),

    storage: z
      .string()
      .describe(
        "A brief sentence describing how to best store the food once cooked, " +
          "to maximise its shelf life. If it is freezer friendly, state this " +
          "too. State how to safely reheat (and defrost if applicable, even if " +
          "eaten cold) the food, if applicable.",
      ),

    recipeNotPossible: z
      .boolean()
      .describe(
        "If the provided prompt is invalid for generating a human-edible recipe, " +
          "such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an empty string, " +
          "otherwise return 'false' and generate the recipe accordingly." +
          " With this being said, though, still be creative and if it isn't " +
          "immediately obvious how to make a dish within the constraints, " +
          "BE CREATIVE and think of a way (as long as it is edible by a human). " +
          "Only return true here if there really is no way.",
      ),

    emoji: z
      .string()
      .describe(
        "A single emoji which best describes this dish. Consider the " +
          "recipe/ingredients when choosing the emoji; don't just base the " +
          "emoji on the name of the dish.",
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
        "The colour which best suits the recipe, preferably matching or going " +
          "well with the emoji you provide. First create the recipe, then choose " +
          "its emoji, then the colour accordingly.",
      ),
  });
}

/**
 * 4x Possible Dishes from a list of ingredients generation:
 * used as a function so that parameters can be passed
 * @param dishType - the selected dish type, or "" if none was selected.
 * @returns the DishListFromIngredients Schema zod object.
 */
export function DishListFromIngredientsSchemaZod(dishType) {
  return z.object({
    ingredientsIdentified: z
      .string()
      .describe(
        "The ingredients that the user has entered, separated by commas, and " +
          "only these ingredients. So that recipes which are possible to cook " +
          "using only those ingredients can be created.",
      ),
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe is best suited to cook from " +
          "the ingredients and nutritional information, for type: " +
          dishType,
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe is best suited to cook from " +
          "the ingredients and nutritional information, for type: " +
          dishType,
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe is best suited to cook from " +
          "the ingredients and nutritional information, for type: " +
          dishType,
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe is best suited to cook from " +
          "the ingredients and nutritional information, for type: " +
          dishType,
      ),
    dishNotPossible: z
      .boolean()
      .describe(
        "If the provided ingredients are invalid for generating a human-edible " +
          "recipe, such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an " +
          "empty string, otherwise return 'false' and generate the dishes " +
          "accordingly. With this being said, though, still be creative and if " +
          "it isn't immediately obvious how to make a dish within the " +
          "constraints, BE CREATIVE and think of a way (as long as it is " +
          "edible by a human). Only return true here if there really is no way.",
      ),
  });
}

/**
 * 4x Possible Dishes from an image of ingredients generation:
 * used as a function so that parameters can be passed
 * @param dishType - the selected dish type, or "" if none was selected.
 * @returns the DishListFromImageIngredients Schema zod object.
 */
export function DishListFromImageIngredientsSchemaZod(dishType) {
  return z.object({
    ingredientsIdentified: z
      .string()
      .describe("The ingredients you can see which are present in the image."),
    firstMostLikelyDish: z
      .string()
      .describe(
        "The first most likely dish you believe can be cooked with ingredients " +
          "present in the image, considering the type: " +
          dishType,
      ),
    secondMostLikelyDish: z
      .string()
      .describe(
        "The second most likely dish you believe can be cooked with ingredients " +
          "present in the image, considering the type: " +
          dishType,
      ),
    thirdMostLikelyDish: z
      .string()
      .describe(
        "The third most likely dish you believe can be cooked with ingredients " +
          "present in the image, considering the type: " +
          dishType,
      ),
    fourthMostLikelyDish: z
      .string()
      .describe(
        "The fourth most likely dish you believe can be cooked with ingredients " +
          "present in the image, considering the type: " +
          dishType,
      ),
    dishNotPossible: z
      .boolean()
      .describe(
        "If the provided ingredients in the image are invalid for generating a " +
          "human-edible recipe, such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an " +
          "empty string, otherwise return 'false' and generate the dishes accordingly." +
          " With this being said, though, still be creative and if it isn't " +
          "immediately obvious how to make a dish within the constraints, BE " +
          "CREATIVE and think of a way (as long as it is edible by a human). Only " +
          "return true here if there really is no way.",
      ),
  });
}

/**
 * 4x Possible Dishes from an image of a dish generation:
 * used as a function so that parameters can be passed
 * @param dishType - the selected dish type, or "" if none was selected.
 * @returns the DishListFromImageIngredients Schema zod object.
 */
export function DishListFromImageSchemaZod(dishType) {
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
        "If the provided image does not contain a valid dish, and thus invalid " +
          "for generating a human-edible recipe, such as something which is inedible, " +
          "return 'true' here and fill in every other required field with an " +
          "empty string, otherwise return 'false' and generate the dishes accordingly." +
          " With this being said, though, still be creative and if it isn't " +
          "immediately obvious how to make a dish within the constraints, BE " +
          "CREATIVE and think of a way (as long as it is edible by a human). " +
          "Only return true here if there really is no way.",
      ),
  });
}

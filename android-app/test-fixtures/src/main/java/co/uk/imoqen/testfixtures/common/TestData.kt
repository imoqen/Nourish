package co.uk.imoqen.testfixtures.common

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.shoppinglist.IngredientAmount

object TestData {

    // Llm Ingredients:
    val llmIngredient_cucumber = LlmIngredient(
        ingredientSingular = "cucumber",
        ingredientPlural = "cucumbers",
        ingredientAmount = 0.25,
        ingredientAmountUnit = "UNIT"
    )
    val llmIngredient_tomato = LlmIngredient(
        ingredientSingular = "tomato",
        ingredientPlural = "tomatoes",
        ingredientAmount = 100.0,
        ingredientAmountUnit = "G"
    )
    val llmIngredient_lettuce = LlmIngredient(
        ingredientSingular = "lettuce",
        ingredientPlural = "lettuce",
        ingredientAmount = 4.0,
        ingredientAmountUnit = "LEAF"
    )

    val llmIngredient_honey = LlmIngredient(
        ingredientSingular = "honey",
        ingredientPlural = "honey",
        ingredientAmount = 5.0,
        ingredientAmountUnit = "ML"
    )
    val llmIngredient_vinegar = LlmIngredient(
        ingredientSingular = "vinegar",
        ingredientPlural = "vinegar",
        ingredientAmount = 1.0,
        ingredientAmountUnit = "DASH"
    )
    val llmIngredient_lemonjuice = LlmIngredient(
        ingredientSingular = "lemon juice",
        ingredientPlural = "lemon juice",
        ingredientAmount = 1.0,
        ingredientAmountUnit = "DROP"
    )
    val llmIngredient_salt = LlmIngredient(
        ingredientSingular = "salt",
        ingredientPlural = "salt",
        ingredientAmount = 1.0,
        ingredientAmountUnit = "SPRINKLE"
    )
    val llmIngredient_pepper = LlmIngredient(
        ingredientSingular = "pepper",
        ingredientPlural = "pepper",
        ingredientAmount = 1.0,
        ingredientAmountUnit = "DASH"
    )

    // Llm Ingredient Sections:
    val llmIngredientSection_salad: LlmIngredientSection = LlmIngredientSection(
        section = "For the salad",
        ingredients = listOf( // for one serving:
            llmIngredient_cucumber,
            llmIngredient_tomato,
            llmIngredient_lettuce
        )
    )

    // illustrates many of the possible ingredient units being used
    val llmIngredientSection_dressing: LlmIngredientSection = LlmIngredientSection(
        section = "For the dressing",
        ingredients = listOf(
            llmIngredient_honey,
            llmIngredient_vinegar,
            llmIngredient_lemonjuice,
            llmIngredient_salt,
            llmIngredient_pepper
        )
    )

    // Llm Recipes:

    val llmRecipe_salad = LlmRecipe(
        "Healthy Salad",
        sectionIngredients = listOf( llmIngredientSection_salad, llmIngredientSection_dressing ),
        servings = 1,
        mealType = "Lunch",
        method = listOf(
            "First, wash the vegetables.", "Cut the vegetables.",
            "Make the dressing.", "Assemble and mix everything together in a big bowl."
        ),
        tags = listOf(
            "VEGAN", "LOW-CALORIE"
        ),
        storage = "Eat immediately.",
        recipeNotPossible = false,
        emoji = "\uD83E\uDD57",
        colour = "GREEN"
    )

    val llmRecipe_recipeNotPossible = LlmRecipe(
        "",
        sectionIngredients = listOf(),
        servings = 1,
        mealType = "",
        method = listOf(),
        tags = listOf(),
        storage = "",
        recipeNotPossible = true,
        emoji = "",
        colour = ""
    )

    val llmDishesProposed_cherry = LlmDishesProposed(
        ingredientsIdentified = "cherries, flour, sugar, eggs, milk",
        firstMostLikelyDish = "Cherry Pancakes",
        secondMostLikelyDish = "Cherry Pie",
        thirdMostLikelyDish = "Cherry Bakewell Tart",
        fourthMostLikelyDish = "Cherry Mousse",
        dishNotPossible = false
    )


    // Recipe Tags:

    val tag_vegan = Tag(
        id = 0,
        identifier = "VEGAN",
        displayName = "Vegan",
        colour = "GREEN",
        authorId = "testUserUid"
    )
    val tag_lowcalorie = Tag(
        id = 0,
        identifier = "LOW-CALORIE",
        displayName = "Low Calorie",
        colour = "ORANGE",
        authorId = "testUserUid"
    )

    // Recipe tags and their selection status:

    val tagSelection_vegan =
        TagSelection(tag_vegan, false)

    val tagSelection_lowcalorie =
        TagSelection(tag_lowcalorie, false)

    val recipeTags = listOf(
        tagSelection_vegan,
        tagSelection_lowcalorie
    )

    val recipeTagsFirstSelected = listOf(
        tagSelection_vegan.copy(selected = true),
        tagSelection_lowcalorie
    )

    // Recipe Types:

    val recipeType_breakfast = RecipeType(
        id = 0,
        title = "Breakfast",
        ordinal = 0
    )
    val recipeType_lunch = RecipeType(
        id = 1,
        title = "Lunch",
        ordinal = 1
    )
    val recipeType_dinner = RecipeType(
        id = 2,
        title = "Dinner",
        ordinal = 2
    )
    val recipeType_dessert = RecipeType(
        id = 3,
        title = "Dessert",
        ordinal = 3
    )
    val recipeType_side = RecipeType(
        id = 4,
        title = "Side",
        ordinal = 4
    )
    val recipeType_drink = RecipeType(
        id = 5,
        title = "Drink",
        ordinal = 5
    )

    val recipeTypes = listOf(
        recipeType_breakfast, recipeType_lunch, recipeType_dinner, recipeType_dessert,
        recipeType_side, recipeType_drink
    )

    // Dishes proposed

    val llmDishesProposed_pasta = LlmDishesProposed(
        ingredientsIdentified = "Pasta, pesto, tomatoes, bacon, cheese, eggs, parmesan",
        firstMostLikelyDish = "Pasta Pesto",
        secondMostLikelyDish = "Spaghetti Carbonara",
        thirdMostLikelyDish = "Tomato pasta bake",
        fourthMostLikelyDish = "Pesto traybake",
        dishNotPossible = false
    )

    val llmDishesProposed_dishNotPossible = LlmDishesProposed(
        ingredientsIdentified = "",
        firstMostLikelyDish = "",
        secondMostLikelyDish = "",
        thirdMostLikelyDish = "",
        fourthMostLikelyDish = "",
        dishNotPossible = true // dishes could not be generated; dish not possible.
    )

    val recipe_salad = Recipe(
        id = 0,
        authorUid = "valid_user",
        title = "Salad Recipe",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        archived = false,
        storageInstructions = "Eat immediately.",
        recipeTypeId = 1,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withInvalidRecipeTypeId = Recipe(
        id = 0,
        authorUid = "valid_user",
        title = "Salad Recipe",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        archived = false,
        storageInstructions = "Eat immediately.",
        recipeTypeId = -1,
        emoji = "\uD83C\uDF5C"
    )

    val ingredient_cherry = Ingredient(
        id = 0,
        singular = "cherry",
        plural = "cherries",
        userAdded = false
    )

    val llmIngredient_cherry = LlmIngredient(
        ingredientSingular = "cherry",
        ingredientPlural = "cherries",
        ingredientAmount = 1000.0,
        ingredientAmountUnit = "G"
    )
}
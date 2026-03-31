package co.uk.imoqen.testfixtures.common

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeSteps
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeTags
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutrition
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntry
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.RecipeCardExpanded
import uk.co.imoqen.nourish.recipebook.RecipeCardUnexpanded
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection
import uk.co.imoqen.nourish.shoppinglist.ShoppingListUiEntry

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
    val llmIngredient_invalidUnit = LlmIngredient(
        ingredientSingular = "tomato",
        ingredientPlural = "tomatoes",
        ingredientAmount = 100.0,
        ingredientAmountUnit = "Invalid, nonexistent unit"
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

    val llmRecipe_correspondingToRecipe_Salad = LlmRecipe(
        title="Salad Recipe",
        sectionIngredients= listOf(
            LlmIngredientSection(
                section="For the Salad",
                ingredients= listOf(
                    LlmIngredient(
                        ingredientSingular="cherry",
                        ingredientPlural="cherries",
                        ingredientAmount=500.0,
                        ingredientAmountUnit="G"
                    )
                )
            )
        ),
        servings=2,
        mealType="Dessert",
        method=listOf("Wash the cherries"),
        tags=listOf("VEGAN"),
        storage="Eat immediately.",
        recipeNotPossible=false,
        emoji="🍜",
        colour="PINK"
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

    val recipeTags_salad = listOf(
        Pair("Vegan", NourishColour.GREEN)
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

    val typeSelection_breakfast = TypeSelection(type=RecipeType(id=0, title="Breakfast", ordinal=0), selected=false)
    val typeSelection_lunch = TypeSelection(type=RecipeType(id=1, title="Lunch", ordinal=1), selected=false)
    val typeSelection_dinner= TypeSelection(type=RecipeType(id=2, title="Dinner", ordinal=2), selected=false)
    val typeSelection_dessert = TypeSelection(type=RecipeType(id=3, title="Dessert", ordinal=3), selected=false)
    val typeSelection_side = TypeSelection(type=RecipeType(id=4, title="Side", ordinal=4), selected=false)
    val typeSelection_drink = TypeSelection(type=RecipeType(id=5, title="Drink", ordinal=5), selected=false)

    // type selection corresponding to recipeTypes above ^
    val typeSelection_allRecipeTypes = listOf(
        typeSelection_breakfast, typeSelection_lunch, typeSelection_dinner, typeSelection_dessert,
        typeSelection_side, typeSelection_drink
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
        storageInstructions = "Eat immediately.",
        recipeTypeId = -1,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withInvalidId = Recipe(
        id = -1, // invalid
        authorUid = "valid_user",
        title = "Salad Recipe",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        storageInstructions = "Eat immediately.",
        recipeTypeId = 2,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withInvalidIngredients = Recipe(
        id = -1, // triggers invalid when getting ingredients, in the fake.
        authorUid = "valid_user",
        title = "Salad Recipe",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        storageInstructions = "Eat immediately.",
        recipeTypeId = -1,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withWaterIngredients = Recipe(
        id = 3, // corresponds to water ingredient
        authorUid = "valid_user",
        title = "Soup Recipe",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        storageInstructions = "Freezable.",
        recipeTypeId = 1,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withDuplicateIngredients = Recipe(
        id = 5, // corresponds to a recipe with duplicated ingredients
        authorUid = "valid_user",
        title = "Carrot Cake",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        storageInstructions = "Freezable.",
        recipeTypeId = -1,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withUnitToConvert = Recipe(
        id = 7, // corresponds to a recipe with duplicated ingredients
        authorUid = "valid_user",
        title = "Apple Pie",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        storageInstructions = "Freezable.",
        recipeTypeId = -1,
        emoji = "\uD83C\uDF5C"
    )

    val recipe_withUnitToNotKeepInShoppingList = Recipe(
        id = 8, // corresponds to a recipe with duplicated ingredients
        authorUid = "valid_user",
        title = "Orange Cake",
        numberOfServings = 2,
        colour = "PINK",
        generated = false,
        imagePath = "image.png",
        storageInstructions = "Freezable.",
        recipeTypeId = -1,
        emoji = "\uD83C\uDF5C"
    )

    val ingredient_cherry = Ingredient(
        id = 0,
        singular = "cherry",
        plural = "cherries",
        userAdded = false
    )

    val ingredient_water = Ingredient(
        id = 0,
        singular = "water",
        plural = "water",
        userAdded = false
    )

    val ingredient_lettuce = Ingredient(
        id = 5,
        singular = "lettuce",
        plural = "lettuce",
        userAdded = false
    )

    val ingredient_unitToConvert = Ingredient(
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

    val sectionsWithIngredients_salad = mutableMapOf(
        "For the Salad" to mutableListOf(
            Triple(
                "kg cherries",
                1.0,
                0L
            )
        )
    )

    val sectionsWithIngredients_withML = mutableMapOf(
        "For the Salad" to mutableListOf(
            Triple(
                "L cherries",
                1.0,
                0L
            )
        )
    )

    val sectionsWithIngredients_afterPluralConversion = mutableMapOf(
        "For the Salad" to mutableListOf(
            Triple(
                "kg cherries",
                2.2,
                0L
            )
        )
    )

    val sectionsWithIngredients_singleUnit = mutableMapOf(
        "For the Salad" to mutableListOf(
            Triple(
                "slice of cherry",
                1.0,
                0L
            )
        )
    )

    val sectionsWithIngredients_unitUnit = mutableMapOf(
        "For the Salad" to mutableListOf(
            Triple(
                "cherry",
                1.0,
                0L
            )
        )
    )

    val recipeCardExpanded_cherry = RecipeCardExpanded(
        title = "Cherry Pie",
        sectionsWithIngredients = mutableMapOf(
            "For the Pie" to mutableListOf(
                Triple(
                    "Cherry",
                    5.0,
                    5L
                )
            )
        ),
        method = mutableListOf(Triple("Wash the cherries", false, 1L)),
        serves = 2,
        recipeType = Pair("Dessert", 1),
        recipeTags = mutableListOf(Pair("Low calorie", NourishColour.PINK)),
        storageInstructions = "Eat immediately.",
        imagePath = "image.png",
        emoji = "\uD83C\uDF52",
        colour = NourishColour.RED,
        llmGenerated = true
    )

    val recipeCardExpanded_salad = RecipeCardExpanded(
        title = "Salad Recipe",
        sectionsWithIngredients = mutableMapOf(
            "For the Salad" to mutableListOf(
                Triple(
                    "kg cherries",
                    1.0,
                    0L
                )
            )
        ),
        method = mutableListOf(Triple("Wash the cherries", false, 0L)),
        serves = 2,
        recipeType = Pair("Dessert", 3),
        recipeTags = mutableListOf(Pair("Vegan", NourishColour.GREEN)),
        storageInstructions = "Eat immediately.",
        imagePath = "image.png",
        emoji = "\uD83C\uDF5C",
        colour = NourishColour.PINK,
        llmGenerated = false
    )

    val recipeCardUnexpanded_cherry = RecipeCardUnexpanded(
        title = "Cherry Pie",
        recipeTags = mutableListOf(
            Pair("Low calorie", NourishColour.PINK)
        ),
        imagePath = "image.png",
        emoji = "\uD83C\uDF52",
        recipeId = 0,
        colour = NourishColour.RED
    )

    val recipeCardUnexpanded_salad = RecipeCardUnexpanded(
        title = "Salad Recipe",
        recipeTags = mutableListOf(
            Pair("Vegan", NourishColour.GREEN)
        ),
        imagePath = "image.png",
        emoji = "\uD83C\uDF5C",
        recipeId = 0,
        colour = NourishColour.PINK
    )

    val recipeCardUnexpanded_soup = RecipeCardUnexpanded(
        title = "Soup Recipe",
        recipeTags = mutableListOf(
            Pair("Vegan", NourishColour.GREEN)
        ),
        imagePath = "image.png",
        emoji = "\uD83C\uDF5C",
        recipeId = 3,
        colour = NourishColour.PINK
    )

    val recipeBookMap_dessert = mutableMapOf(
        recipeType_dessert to mutableListOf(
            recipeCardUnexpanded_cherry
        )
    )

    val recipeBookMap_dessertSalad = mutableMapOf(
        recipeType_dessert to mutableListOf(
            recipeCardUnexpanded_salad,
            recipeCardUnexpanded_soup
        )
    )

    val typeSelection = listOf(
        TypeSelection(
            recipeType_dessert, true
        ),
        TypeSelection(
            recipeType_breakfast, true
        )
    )

    val shoppingListIngredient_cherry = ShoppingListIngredient(
        ingredientId = 0,
        plural = "cherries",
        unit = IngredientUnit.G,
        amount = 250.0
    )

    val shoppingListIngredient_oatMilk = ShoppingListIngredient(
        ingredientId = 0,
        plural = "oat milk",
        unit = IngredientUnit.ML,
        amount = 1000.0
    )

    val shoppingListIngredient_triggerError = ShoppingListIngredient(
        ingredientId = 0,
        plural = "error ingredient",
        unit = IngredientUnit.UNIT,
        amount = 1.0
    )

    val ingredientsToAddToShoppingList = mutableListOf(
        Pair(shoppingListIngredient_cherry, true),
        Pair(shoppingListIngredient_oatMilk, true)
    )

    val ingredientsToAddToShoppingList_oneSelected_oneNotSelected = mutableListOf(
        Pair(shoppingListIngredient_cherry, true), // selected
        Pair(shoppingListIngredient_oatMilk, false) // not selected
    )

    val ingredientsToAddToShoppingList_triggerError = mutableListOf(
        Pair(shoppingListIngredient_triggerError, true)
    )

    val shoppingListUiEntry_cherries = ShoppingListUiEntry(
        id = 0,
        plural = "cherries",
        unit = IngredientUnit.G,
        amount = 500.0,
        purchased = false,
        ordinal = 0
    )

    val shoppingListEntry_cherries = ShoppingListEntry(
        id = 0,
        ingredientId = 0,
        purchaserUid = "valid_user",
        unit = IngredientUnit.G,
        amount = 500.0,
        purchased = false,
        ordinal = 0
    )

    val shoppingListUiEntry_oatMilk = ShoppingListUiEntry(
        id = 1,
        plural = "oat milk",
        unit = IngredientUnit.ML,
        amount = 1000.0,
        purchased = false,
        ordinal = 1
    )

    val shoppingListUiEntry_bananas = ShoppingListUiEntry(
        id = 2,
        plural = "bananas",
        unit = IngredientUnit.UNIT,
        amount = 1.0,
        purchased = false,
        ordinal = 2
    )

    val shoppingListUiEntry_null = ShoppingListUiEntry(
        id = 2,
        plural = "",
        unit = IngredientUnit.UNIT,
        amount = 0.0,
        purchased = false,
        ordinal = 2
    )

    val shoppingListEntry_oatMilk = ShoppingListEntry(
        id = 1,
        ingredientId = 1,
        purchaserUid = "valid_user",
        unit = IngredientUnit.ML,
        amount = 1000.0,
        purchased = false,
        ordinal = 1
    )

    val shoppingListUiEntries = listOf(
        shoppingListUiEntry_cherries,
        shoppingListUiEntry_oatMilk
    )

    val shoppingListEntries = listOf(
        shoppingListEntry_cherries,
        shoppingListEntry_oatMilk
    )

    val shoppingListEntry_toConvertToL = ShoppingListEntry(
        id = 2,
        ingredientId = 2,
        purchaserUid = "user_with_ingr_to_convert_l_and_kg",
        unit = IngredientUnit.ML,
        amount = 2000.0, // will change to 2L in the UI
        purchased = false,
        ordinal = 2
    )

    val shoppingListEntry_toConvertToKG = ShoppingListEntry(
        id = 3,
        ingredientId = 3,
        purchaserUid = "user_with_ingr_to_convert_l_and_kg",
        unit = IngredientUnit.G,
        amount = 5000.0, // will change to 5KG in the UI
        purchased = false,
        ordinal = 3
    )

    val recipeWithRecipeIngredient_salad = RecipeWithRecipeIngredients(
        recipe = recipe_salad,
        ingredients = listOf(
            RecipeIngredient(
                id = 0,
                recipeId = 0,
                ingredientId = 0,
                ingredientUnit = IngredientUnit.G,
                amount = 500.0,
                sectionId = 0
            )
        )
    )

    val recipeWithRecipeIngredient_containsML = RecipeWithRecipeIngredients(
        recipe = recipe_salad,
        ingredients = listOf(
            RecipeIngredient(
                id = 0,
                recipeId = 0,
                ingredientId = 0,
                ingredientUnit = IngredientUnit.ML,
                amount = 500.0,
                sectionId = 0
            )
        )
    )

    val recipeWithRecipeIngredient_shouldConvertToPlural = RecipeWithRecipeIngredients(
        recipe = recipe_salad,
        ingredients = listOf(
            RecipeIngredient(
                id = 0,
                recipeId = 0,
                ingredientId = 0,
                ingredientUnit = IngredientUnit.KG,
                amount = 1.1, // rounds to 1.0
                sectionId = 0
            )
        )
    )

    val recipeWithRecipeIngredient_shouldHaveSingularUnit = RecipeWithRecipeIngredients(
        recipe = recipe_salad,
        ingredients = listOf(
            RecipeIngredient(
                id = 0,
                recipeId = 0,
                ingredientId = 0,
                ingredientUnit = IngredientUnit.SLICE,
                amount = 0.5, // singular unit required for half a slice of bread; not half slices of bread
                sectionId = 0
            )
        )
    )

    val recipeWithRecipeIngredient_withUnitUnit = RecipeWithRecipeIngredients(
        recipe = recipe_salad,
        ingredients = listOf(
            RecipeIngredient(
                id = 0,
                recipeId = 0,
                ingredientId = 0,
                ingredientUnit = IngredientUnit.UNIT,
                amount = 0.5, // singular unit required for half a slice of bread; not half slices of bread
                sectionId = 0
            )
        )
    )

    val recipeWithRecipeTags_salad = RecipeWithRecipeTags(
        recipe = recipe_salad,
        recipeTags = listOf(RecipeTag(0, 0, 0))
    )

    val recipeWithRecipeTags_empty = RecipeWithRecipeTags(
        recipe = recipe_salad,
        recipeTags = listOf(RecipeTag(10, 10, 10)) // tag id of 10 doesn't exist in the db
    )

    val recipeIngredientSection_salad = RecipeIngredientSection(
        id = 1,
        title = "For the Salad"
    )

    val recipeStep_washCherries = RecipeStep(
        id = 0,
        recipeId = 0,
        index = 0,
        instruction = "Wash the cherries",
        complete = false
    )

    val recipeWithRecipeSteps_salad = RecipeWithRecipeSteps(
        recipe = recipe_salad,
        recipeSteps = listOf(recipeStep_washCherries)
    )

    val recipeNutrition_salad = RecipeNutrition(
        id = 0,
        recipeId = 0,
        servings = 1,
        calories = 180,
        fat = 4.0,
        saturates = 5.0,
        carbohydrate = 10.0,
        sugars = 7.0,
        protein = 1.0,
        salt = 0.5,
        fibre = 20.0
    )

    val llmNutrition_salad = LlmNutrition(
        servings = 1,
        calories = 180,
        fat = 4.0,
        saturates = 5.0,
        carbohydrate = 10.0,
        sugars = 7.0,
        protein = 1.0,
        salt = 0.5,
        fibre = 20.0
    )

    val recipeQuery_salad_healthiness = RecipeQuery(
        id = 0,
        recipeId = 0,
        userQuestion = "Is this healthy?",
        llmAnswer = "Yes, this recipe is extremely healthy. However, it is low in protein so may not keep you full."
    )

    val recipeQuery_salad_protein = RecipeQuery(
        id = 1,
        recipeId = 0,
        userQuestion = "How can I increase the protein in this recipe?",
        llmAnswer = "Add some sources of protein such as chicken or tofu."
    )

    val recipeQueriesList_salad = listOf(
        recipeQuery_salad_healthiness,
        recipeQuery_salad_protein
    )

    val llmQuestionResponse = LlmQuestionResponse(
        answer = "This recipe is healthy."
    )

}
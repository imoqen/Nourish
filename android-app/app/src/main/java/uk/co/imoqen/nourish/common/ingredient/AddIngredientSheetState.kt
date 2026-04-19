package uk.co.imoqen.nourish.common.ingredient

import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

/**
 * The UiState used in the add ingredient modal bottom sheet.
 *
 * @author Imogen Pemberton, 2026
 */
data class AddIngredientSheetState(
    // this will be set to the singular and plural of a new Ingredient. then when the LLM produces
    // ingredient it will check if either of these match. if they do, it will overwrite the one which
    // doesn't match. Thus, we can still save the user inputted ingredients fine.
    // this is also used in the recipe add / edit.
    val name: String = "",

    // purchaser uid is just current uid.

    // can only be chosen from: none specified (SINGLE), unit (UNIT), ml (ML) and g (G).
    // switching between KG and L is also supported.
    val unit: IngredientUnit = IngredientUnit.SINGLE,

    // string for the UI, but converted to double when inserting the shopping list entry
    val amount: String = "",

    // purchased will be set to false. ordinal will be the next max ordinal value.
    val error: String = ""
)

/**
 * Text displayed regarding an ingredient in the add ingredient modal bottom sheet.
 */
data class AddIngredientSheetText(
    val title: String,
    val name: String,
    val amount: String,
    val unit: String,
    val complete: String
)
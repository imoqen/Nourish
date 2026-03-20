package uk.co.imoqen.nourish.common.ingredient

import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

data class AddIngredientSheetState(
    val name: String = "", // this will be set to the singular and plural of a new Ingredient. then when the LLM produces ingredient it will check if either of these match. if they do, it will overwrite the one which doesnt match. thuis, we can still save the user inputted ingredients fine. this will also be used in the recipe add / edit.
    // purchaser uid is just current uid
    val unit: IngredientUnit = IngredientUnit.SINGLE, // can only be chosen from: none specified (SINGLE), unit (UNIT), ml (ML) and g (G). ADD SUPPORT FOR KG AND L just convert them to ml and g then display as kg / g if > 1000 or so
    val amount: String = "", // string for the UI but converted to double when inserting the shopping list entry
    // purchased will be set to false. ordinal will be the next max ordinal value.
    val error: String = ""
)
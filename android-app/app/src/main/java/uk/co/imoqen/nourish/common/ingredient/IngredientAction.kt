package uk.co.imoqen.nourish.common.ingredient

import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

/**
 * Declares the actions which can be performed in the Ingredient Sheet,
 * utilised and implemented across all ViewModels in which ingredients can be added.
 * (shopping list and recipe creation).
 * Hence, these are shared actions which can be used across different view models, rather than
 * through duplicate code.
 *
 * @author Imogen Pemberton, 2026
 */
sealed interface IngredientSheetAction {
    class OnDismissRequest(): IngredientSheetAction
    data class SetIngredientName(val name: String): IngredientSheetAction
    data class SetIngredientAmount(val amount: String): IngredientSheetAction
    data class SetIngredientUnit(val unit: IngredientUnit): IngredientSheetAction
    class OnComplete(): IngredientSheetAction
}
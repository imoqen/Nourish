package uk.co.imoqen.nourish.common.ingredient

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.tag.Tag

sealed interface IngredientSheetAction {
    class OnDismissRequest(): IngredientSheetAction
    data class SetIngredientName(val name: String): IngredientSheetAction
    data class SetIngredientAmount(val amount: String): IngredientSheetAction
    data class SetIngredientUnit(val unit: IngredientUnit): IngredientSheetAction
    class OnComplete(): IngredientSheetAction
}
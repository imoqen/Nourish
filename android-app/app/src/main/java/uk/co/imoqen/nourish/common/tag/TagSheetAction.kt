package uk.co.imoqen.nourish.common.tag

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.tag.Tag

sealed interface TagSheetAction {
    data class SetFilterDisplayState(val displayState: FilterState): TagSheetAction

    data class UpdateSelected(val tag: Tag, val selected: Boolean): TagSheetAction

    data class SetCreatedTagName(val name: String): TagSheetAction

    data class SetCreatedTagColour(val colour: NourishColour): TagSheetAction

    class CreateTag(): TagSheetAction

    class CloseTagCreation(): TagSheetAction

    data class ShowTagConfirmationDialog(val show: Boolean): TagSheetAction

    data class SetTagToDelete(val tag: Tag?): TagSheetAction

    class DeleteTag(): TagSheetAction
}
package uk.co.imoqen.nourish.common.tag

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.tag.Tag

/**
 * Declares the actions which can be performed in the Tag Sheet,
 * utilised and implemented across all ViewModels in which tags can be created, applied, and edited.
 * Namely, the RecipeDiscovery, RecipeBook, RecipeCreation, and editing.
 * Hence, these are shared actions which can be used across different view models, rather than
 * through duplicate code.
 *
 * @author Imogen Pemberton, 2026
 */
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
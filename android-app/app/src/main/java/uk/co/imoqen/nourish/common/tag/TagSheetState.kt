package uk.co.imoqen.nourish.common.tag

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.recipebook.TagSelection

/**
 * The UiState used in the Tag Sheet, for selecting, creating and deleting, tags.
 */
data class TagSheetState (
    // Determines what part of the filter selection is shown
    val filterDisplayState: FilterState = FilterState.NONE,

    // Determines whether to display the confirmation dialog for tag related actions (deleting a tag)
    val showTagConfirmationDialog: Boolean = false,

    // Tag deletion
    val tagToDelete: Tag? = null,

    // Tag creation
    val createdTagName: String = "",
    val createdTagColour: NourishColour? = null,
    val tagCreationError: String = "",

    // The recipe tags and whether they have been selected
    val recipeTags: List<TagSelection>? = null
)

enum class FilterState {
    APPLY, // to apply filters to the query
    CREATE, // to add a new filter
    EDIT, // to delete filters
    NONE // to not display the tag sheet: modal bottom sheet is closed.
}
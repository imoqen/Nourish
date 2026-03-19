package uk.co.imoqen.nourish.tag

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.recipebook.TagSelection

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
    EDIT, // delete filters
    NONE // when the modal bottom sheet for filtering should not be displayed
}
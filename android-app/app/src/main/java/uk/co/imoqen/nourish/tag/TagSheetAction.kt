package uk.co.imoqen.nourish.tag

sealed interface TagSheetAction {
    data class SetFilterDisplayState(val displayState: FilterState): TagSheetAction
}
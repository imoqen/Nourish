package uk.co.imoqen.nourish.common.tag

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishErrorText
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.TagsDisplayForFiltering
import uk.co.imoqen.nourish.common.backgroundColour
import uk.co.imoqen.nourish.common.dimmedBackgroundColour
import uk.co.imoqen.nourish.common.onBackgroundColour

/**
 * The modal bottom sheet Composable for selecting, creating, and deleting tags. Used in the
 * RecipeDiscovery, RecipeBook, RecipeCreation, and editing views.
 * @param sheetState - The state of the modal bottom sheet for tags.
 * @param tagSheetState - The UiState of the TagSheet, which holds data such as the tag to be created
 * @param tagSheetAction - Possible tag actions such as creation and deletion of tags.
 * @param title - The text to display as the title in the bottom sheet.
 *
 * @author Imogen Pemberton, 2026
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeTagSelection(
    sheetState: SheetState,
    tagSheetState: TagSheetState,
    tagSheetAction: (TagSheetAction) -> Unit,
    title: String = "Filter by Nutritional Tag(s)"
) {
    ModalBottomSheet(
        onDismissRequest = {
            tagSheetAction(TagSheetAction.SetFilterDisplayState(FilterState.NONE))
        },
        sheetState = sheetState
    ) {

        val keyboardFocusManager = LocalFocusManager.current

        AnimatedContent(
            targetState = tagSheetState.filterDisplayState,
        ) { filterDisplayState ->
            when (filterDisplayState) {
                FilterState.APPLY -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        NourishTextDivider(title)

                        TagsDisplayForFiltering(
                            tags = tagSheetState.recipeTags,
                            onTagPressed =  { tag, selected ->
                                tagSheetAction(TagSheetAction.UpdateSelected(tag, selected)) },
                            displayAddButton = true,
                            onAddButtonPressed = {
                                tagSheetAction(
                                    TagSheetAction.SetFilterDisplayState(FilterState.CREATE)
                                )
                            },
                            displayEditButton = true,
                            onEditButtonPressed = {
                                tagSheetAction(
                                    TagSheetAction.SetFilterDisplayState(FilterState.EDIT)
                                )
                            }
                        )
                    }
                }

                FilterState.CREATE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp) // Ensures its not too close to the screen.
                            .animateContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        NourishTextDivider(
                            "Create a Nutritional Tag",
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        NourishTextField(
                            value = tagSheetState.createdTagName,
                            onValueChange = {
                                tagSheetAction(TagSheetAction.SetCreatedTagName(it))
                                            },
                            placeholder = "Enter a description, e.g. 'Vegan'",
                            onDone = { keyboardFocusManager.clearFocus() }
                        )

                        // Identifier is created by making the display name all caps and replacing
                        // spaces with -. only alphanumerics can be added.
                        NourishTextDivider(
                            "Choose a colour",
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        // Colour picker too from predefined colours.
                        // 3x3 as there are 9 colours
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            items(NourishColour.entries) { colour ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) { // simply for padding between tags
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color =
                                                    if (isSystemInDarkTheme()) {
                                                        colour.onBackgroundColour()
                                                    } else {
                                                        colour.dimmedBackgroundColour()
                                                    },
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                tagSheetAction(
                                                    TagSheetAction.SetCreatedTagColour(
                                                        colour
                                                    )
                                                )
                                            }
                                            .then(
                                                // Highlight if this is the selected colour for the tag:
                                                if (tagSheetState.createdTagColour == colour) {
                                                    Modifier.border(
                                                        width = 1.dp,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .then(
                                                Modifier.padding(4.dp)
                                            )
                                    ) {
                                        Text(
                                            text = colour.name.lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            color =
                                                if (isSystemInDarkTheme()) {
                                                    colour.backgroundColour()
                                                } else {
                                                    colour.onBackgroundColour()
                                                },
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            NourishIconTextButton(
                                onClick = { tagSheetAction(TagSheetAction.CreateTag()) },
                                painter = painterResource(R.drawable.tick),
                                description = "Create tag"
                            )
                            NourishIconTextButton(
                                onClick = { tagSheetAction(TagSheetAction.CloseTagCreation()) },
                                painter = painterResource(R.drawable.cross),
                                description = "Discard tag"
                            )
                        }

                        if (tagSheetState.tagCreationError != "") {
                            NourishErrorText(tagSheetState.tagCreationError)
                        }
                    }
                }

                FilterState.EDIT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        NourishTextDivider("Delete a Nutritional Tag")

                        TagsDisplayForFiltering(
                            tags = tagSheetState.recipeTags,
                            trailingIcon = painterResource(R.drawable.delete),
                            onTrailingIconPress = {
                                tagSheetAction(TagSheetAction.ShowTagConfirmationDialog(true))
                                                  },
                            setTagOnTrailingIconPress = {
                                tag -> tagSheetAction(TagSheetAction.SetTagToDelete(tag))
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        NourishIconTextButton(
                            onClick = { tagSheetAction(
                                TagSheetAction.SetFilterDisplayState(FilterState.APPLY))
                                      },
                            painter = painterResource(R.drawable.arrow_left),
                            description = "Back",
                        )
                    }

                    NourishConfirmationDialog(
                        show = tagSheetState.showTagConfirmationDialog,
                        title = "Confirm Tag Deletion",
                        description = "Are you sure you wish to delete this nutritional tag?\n\nThis action cannot be undone.",
                        onConfirmation = { tagSheetAction(TagSheetAction.DeleteTag()) },
                        onDismiss = { tagSheetAction(TagSheetAction.ShowTagConfirmationDialog(false)) }
                    )
                }

                FilterState.NONE -> {
                    // do nothing - unreachable. only to satisfy compiler.
                }
            }
        }
    }
}
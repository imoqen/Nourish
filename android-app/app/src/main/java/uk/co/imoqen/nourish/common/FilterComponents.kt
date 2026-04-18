package uk.co.imoqen.nourish.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection

// Contains Composables used across the app in Tag (Nutritional Filter)
// and Recipe Type (aka Meal Type) selection.

/**
 * Composable to display the tag selection options for filtering by nutritional tag. This is used
 * in the RecipeDiscovery view and RecipeBook view, to apply the tags.
 * @param tags - All tags for a user and whether they are selected.
 * @param onTagPressed - Function which runs when a tag is pressed. This is optional, so null if not provided.
 * @param displayAddButton - Whether tags should be create-able in this menu. (true in RecipeDiscovery,
 *                           false in RecipeBook).
 * @param onAddButtonPressed - Function which runs when the add tag button is pressed.
 * @param displayEditButton - Whether tags should be edit-able in this menu. (true in RecipeDiscovery,
 *                            false in RecipeBook).
 * @param onEditButtonPressed - Function which runs when the edit tag button is pressed.
 * @param trailingIcon - Trailing icon to display on the button, e.g. cross for deleting tag
 * @param trailingIconDescription - Content description of the icon, used for screen readers and tests.
 * @param onTrailingIconPress - Function which runs when the trailing icon button is pressed.
 *                              e.g. to delete a tag.
 * @param setTagOnTrailingIconPress - Function to set the tag when the trailing icon is pressed.
 *                                    e.g. to set it as the tag to be deleted.
 */
@Composable
fun TagsDisplayForFiltering(
    tags: List<TagSelection>?,
    onTagPressed: ((Tag, Boolean) -> Unit)? = null,
    displayAddButton: Boolean = false,
    onAddButtonPressed: () -> Unit = {},
    displayEditButton: Boolean = false,
    onEditButtonPressed: () -> Unit = {},
    trailingIcon: Painter? = null,
    trailingIconDescription: String = "Tag Trailing Icon",
    onTrailingIconPress: () -> Unit = {  },
    setTagOnTrailingIconPress: (Tag?) -> Unit = {  },
) {
    FlowRow(
        modifier = Modifier.padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tags?.forEach { (tag, selected) ->
            // Alternates colour of tag depending on dark / light mode
            val colour = enumValueOf<NourishColour>(tag.colour)

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.background(
                        shape = RoundedCornerShape(12.dp),
                        color =
                            if (isSystemInDarkTheme()) {
                                colour.onBackgroundColour()
                            } else {
                                colour.dimmedBackgroundColour()
                            }
                    ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = tag.displayName,
                        fontSize = 14.sp,
                        modifier = Modifier
                            // only clickable if onTagPressed is present:
                            .then(
                                if (onTagPressed != null) {
                                    Modifier.clickable {
                                        onTagPressed.invoke(tag, !selected) // do the selection
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .then(
                                // Don't display outline if trailing icon present (for deletion)
                                if (selected && trailingIcon == null) {
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
                                Modifier
                                    .padding(4.dp)
                                    .padding(horizontal = 2.dp)
                            ),
                        color = if (isSystemInDarkTheme()) {
                            colour.backgroundColour()
                        } else {
                            colour.onBackgroundColour()
                        }
                    )

                    if (trailingIcon != null) {
                        Icon(
                            painter = trailingIcon,
                            contentDescription = trailingIconDescription,
                            modifier = Modifier
                                .size(18.dp)
                                .then(
                                        Modifier.clickable {
                                            // Invoke is required because the methods are nullable
                                            setTagOnTrailingIconPress(tag) // set the tag to delete
                                            onTrailingIconPress()
                                        }
                                )
                                .then(
                                    Modifier.padding(end = 2.dp)
                                ),
                            tint = if (isSystemInDarkTheme()) {
                                colour.backgroundColour()
                            } else {
                                colour.onBackgroundColour()
                            }
                        )
                    }
                }

            }
        }
        if (displayAddButton) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable { onAddButtonPressed() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add your own",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(4.dp)
                            .padding(horizontal = 2.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Add a new nutritional tag",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 2.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        if (displayEditButton) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable { onEditButtonPressed() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(4.dp)
                            .padding(horizontal = 2.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "Edit your nutritional tags",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 2.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

/**
 * Displays the Recipe Types (Breakfast, Lunch, Dinner, Dessert, Side, and Drink) such that they are
 * mutually exclusive; only one type can be selected at once.
 * This is used in RecipeDiscovery and RecipeCreation, to choose a recipe type to apply to the query
 * / user-added recipe - important because only one recipe type can be selected for a dish.
 * Note that Recipe Types are also referred to as Meal Types.
 * @param types - All recipe types
 * @param selectedType - The single recipe type which has been selected.
 * @param updateSelectedType - Function which runs when the type to select should be updated.
 * @param allowNone - Allows no recipe type to be selected. By default, the user is allowed to not
 * enter a recipe type in RecipeDiscovery. However, not allowed for when adding recipe - it must have one.
 */
@Composable
fun TypesDisplayForMutuallyExclusiveFiltering(
    types: List<RecipeType>?,
    selectedType: RecipeType?,
    updateSelectedType: (RecipeType?) -> Unit,
    allowNone: Boolean = true
) {
    FlowRow(
        modifier = Modifier.padding(2.dp).fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        types?.forEach { type ->
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = type.title,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background
                        )
                        .clickable {
                            if (type == selectedType) {
                                // Deselect it: already selected.
                                updateSelectedType(null)
                            } else {
                                // Select it as its not already selected
                                updateSelectedType(type)
                            }
                        }
                        .then(
                            if (type == selectedType) {
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
                            Modifier
                                .padding(4.dp)
                                .padding(horizontal = 2.dp)
                        ),
                    color = MaterialTheme.colorScheme.onBackground

                )

            }
        }
        // reset button:
        if (allowNone) {
            NourishIconButton(
                onClick = { updateSelectedType(null) }, // Reset back to all types
                painter = painterResource(R.drawable.cross),
                modifier = Modifier.size(32.dp),
                contentDescription = "reset"
            )
        }
    }
}

/**
 * Composable to display all Recipe Types such that their selection does not need to be mutually
 * exclusive; any number of Recipe Types can be selected at once. This is used in the RecipeBook
 * to search for recipes by recipe type.
 * @param types - All recipe types and for each, whether it is selected.
 * @param updateSelectedType - Function to update the list of selected recipe types.
 */
// used in recipe book
@Composable
fun TypesDisplayForFiltering(
    types: List<TypeSelection>?,
    updateSelectedType: (RecipeType, Boolean) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        types?.forEach { (type, selected) ->
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = type.title,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background
                        )
                        .clickable {
                            updateSelectedType(type, !selected) // Perform the selection
                        }
                        .then(
                            if (selected) {
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
                            Modifier
                                .padding(4.dp)
                                .padding(horizontal = 2.dp)
                        ),
                    color = MaterialTheme.colorScheme.onBackground

                )

            }
        }
    }
}

/**
 * Composable to display the currently selected Tags.
 * @param tags - All tags and for each whether they are selected or not.
 * @param allowEdit - Whether the tags can be edited (selected / deselected) within this display.
 * @param updateSelectedTag - If allowEdit is true, this function is used to update the selected tag.
 */
@Composable
fun SelectedTags(
    tags: List<TagSelection>?,
    allowEdit: Boolean = false,
    updateSelectedTag: (Tag, Boolean) -> Unit = {_,_ -> },
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            // Max height of 120px to match the image in the upload, this holds 3 rows of tags nicely
            .heightIn(max = 140.dp)
            .verticalScroll(scrollState)
    ) {
        FlowRow(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            tags?.forEach { (tag, selected) ->
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    // Alternate colour of tag depending on dark / light mode
                    val colour = enumValueOf<NourishColour>(tag.colour)

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSystemInDarkTheme()) {
                                    colour.onBackgroundColour()
                                } else {
                                    colour.dimmedBackgroundColour()
                                }
                            )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tag.displayName,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .padding(horizontal = 2.dp),
                                color = if (isSystemInDarkTheme()) {
                                    colour.backgroundColour()
                                } else {
                                    colour.onBackgroundColour()
                                }
                            )
                            if (allowEdit) {
                                Icon(
                                    painter = painterResource(R.drawable.cross),
                                    contentDescription = "Remove Tag",
                                    modifier = Modifier
                                        .clickable {
                                            updateSelectedTag(tag, false) // Remove the tag
                                        }
                                        .size(18.dp)
                                        .padding(end = 2.dp),
                                    tint = if (isSystemInDarkTheme()) {
                                        colour.backgroundColour()
                                    } else {
                                        colour.onBackgroundColour()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
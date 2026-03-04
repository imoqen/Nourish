package uk.co.imoqen.nourish.common

import android.R.attr.clickable
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
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
import uk.co.imoqen.nourish.recipebook.RecipeBookViewModel
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection

@Composable
fun TagsDisplayForFiltering(
    tags: List<TagSelection>?,
    onTagPressed: ((Tag, Boolean) -> Unit)? = null, // optional so that doesnt do anything when in the edit menu
    displayAddButton: Boolean = false,
    onAddButtonPressed: () -> Unit = {},
    displayEditButton: Boolean = false,
    onEditButtonPressed: () -> Unit = {},
    trailingIcon: Painter? = null,
    trailingIconDescription: String = "",
    onTrailingIconPress: () -> Unit = {  },
    setTagOnTrailingIconPress: (Tag?) -> Unit = {  },
) {
    //val tags = recipeBookViewModel.uiState.collectAsState().value.allTags
    FlowRow(
        modifier = Modifier.padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tags?.forEach { (tag, selected) ->
            // alternate colour of tag depending on dark / light mode
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
                                if (selected && trailingIcon == null) { //dont display outline if trailing icon present (for deletion)
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
                                    //if (onTrailingIconPress != null && setTagOnTrailingIconPress != null) {

                                        Modifier.clickable {
                                            Log.i("tag delete", "setting tag to delete")
                                            // invoke is required because the methods are nullable
                                            setTagOnTrailingIconPress(tag) // set thetag (to delete)
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
                        contentDescription = "Add a new nutritional filter",
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
                        contentDescription = "Edit your nutritional filters",
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

// used in recipe discovery
@Composable
fun TypesDisplayForMutuallyExclusiveFiltering(
    types: List<RecipeType>?,
    selectedType: RecipeType?,
    updateSelectedType: (RecipeType?) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(2.dp),
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
                                // deselect it: already selected.
                                updateSelectedType(null)
                            } else {
                                // select it as its not already selected
                                updateSelectedType(type)
                            }
                        }
                        .then(
                            if (type == selectedType) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error,
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
    // reset button:
    NourishIconButton(
        onClick = { updateSelectedType(null) }, // reset back to all types
        painter = painterResource(R.drawable.cross),
    )
}

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
                            updateSelectedType(type, !selected) // do the selection
                        }
                        .then(
                            if (selected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error,
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

// recipe types are mutually exclusive: only one can be selected. so this is why a list isnt used
@Composable
fun SelectedType(
    type: RecipeType,
    updateSelectedType: (RecipeType) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .heightIn(max = 140.dp)   // max height of 120px to match the image in the upload, this holds 3 rows of tags nicely
            .verticalScroll(scrollState)
    ) {

    }}

@Composable
fun SelectedTags(
    tags: List<TagSelection>?,
    updateSelectedTag: (Tag, Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .heightIn(max = 140.dp)   // max height of 120px to match the image in the upload, this holds 3 rows of tags nicely
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
                    // alternate colour of tag depending on dark / light mode
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
                            Icon(
                                painter = painterResource(R.drawable.cross),
                                contentDescription = "Remove Tag",
                                modifier = Modifier
                                    .clickable {
                                        updateSelectedTag(tag, false) // remove the tag
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
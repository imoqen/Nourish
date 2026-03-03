package uk.co.imoqen.nourish.common

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    updateSelectedTag: (Tag, Boolean) -> Unit
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
                modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = tag.displayName,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSystemInDarkTheme()) {
                                colour.onBackgroundColour()
                            } else {
                                colour.dimmedBackgroundColour()
                            }
                        )
                        .clickable {
                            updateSelectedTag(tag, !selected) // do the selection
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
                            Modifier.padding(4.dp).padding(horizontal=2.dp)
                        ),
                    color = if (isSystemInDarkTheme()) {
                        colour.backgroundColour()
                    } else {
                        colour.onBackgroundColour()
                    }
                )

            }
        }
    }
}

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
                modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(12.dp))
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
                            Modifier.padding(4.dp).padding(horizontal = 2.dp)
                        ),
                    color = MaterialTheme.colorScheme.onBackground

                )

            }
        }
    }
}

@Composable
fun SelectedTags(
    tags: List<TagSelection>?,
    updateSelectedTag: (Tag, Boolean) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tags?.forEach { (tag, selected) ->
            if (selected) { // only display selected tags.
                // alternate colour of tag depending on dark / light mode
                val colour = enumValueOf<NourishColour>(tag.colour)

                Box(
                    modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(12.dp)).background(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSystemInDarkTheme()) {
                            colour.onBackgroundColour()
                        } else {
                            colour.dimmedBackgroundColour()
                        }
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tag.displayName,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(4.dp).padding(horizontal = 2.dp),
                            color = if (isSystemInDarkTheme()) {
                                colour.backgroundColour()
                            } else {
                                colour.onBackgroundColour()
                            }
                        )
                        Image(
                            painter = painterResource(R.drawable.cross),
                            contentDescription = "Remove Tag",
                            modifier = Modifier
                                .clickable {
                                    updateSelectedTag(tag, false) // remove the tag
                                }
                        )
                    }

                }
            }
        }
    }
}
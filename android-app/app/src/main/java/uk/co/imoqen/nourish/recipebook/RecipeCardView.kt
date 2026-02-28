package uk.co.imoqen.nourish.recipebook

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.flow.emptyFlow
import uk.co.imoqen.nourish.common.NourishH1
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.db.recipe.Recipe
import java.io.File

@Composable
fun RecipeCardScreen(
    recipeBookViewModel: RecipeBookViewModel,
    recipeId: Long
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        val recipe = recipeBookViewModel.getRecipeById(recipeId).collectAsState(initial = null).value
        // should constrain it to only return recipe made by that user, for security. address in a future stepping stone ^
        if (recipe != null) {
            recipeBookViewModel.setSelectedRecipe(recipe)
            RecipeCard(recipeBookViewModel)
        }
    }
}

@Composable
fun RecipeCard(
    recipeBookViewModel: RecipeBookViewModel
) {
    val recipe = recipeBookViewModel.uiState.collectAsState().value.selectedRecipe
    recipeBookViewModel.buildRecipeCard()
    val recipeCard = recipeBookViewModel.uiState.collectAsState().value.selectedRecipeCard


    if (recipeCard != null) {
        val imagePath = recipeCard.imagePath
        Card(


        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (imagePath != "") { 320.dp } else { 160.dp })
            ) {

                // image:
                val imagePath = recipeCard.imagePath
                val context = LocalContext.current

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    // Image:
                    val imageFile =
                        File(
                            context.filesDir,
                            imagePath
                        )
                    var displayImage = false
                    if (imageFile.exists() && imagePath != "") {
                        displayImage = true
                        //Use Coil to display the selected image
                        val painter = rememberAsyncImagePainter(
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(imageFile)
                                .build()
                        )

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .height(320.dp-84.dp+32.dp) // total size of card - size of text bit (200 - 84) + extra for the rounded corner shape (32.dp)
                                .fillMaxWidth(), // 2/3 of the card height - maybe use consts for this in future to ensure they match upon changes
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth() // matches the card width
                                .height(160.dp-84.dp+32.dp) // takes up much less space because no image
                                .background(MaterialTheme.colorScheme.error) // put the chosen colour
                        )
                    }
                    // Emoji:
                    Text(
                        text = recipeCard.emoji,
                        fontSize = 56.sp,
                        // center emoji if no image, otherwise put at bottom center
                        modifier = if (displayImage) {
                            Modifier.align(Alignment.BottomCenter).offset(y = 24.dp) // offsets are added to account for the img displayed for the rounded corner shape
                        } else {
                            Modifier.align(Alignment.Center).offset(y = 14.dp) // offsets are added to account for the bg displayed for the rounded corner shape
                        },
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(4f, 4f),
                                blurRadius = 4f
                            )
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            // bottom corners are rounded, top are not:
                            shape = RoundedCornerShape(
                                bottomStart = 32.dp,
                                bottomEnd = 32.dp
                            )
                        )
                        .padding(vertical = 16.dp)
                    //contentAlignment = Alignment.Center
                ) {
                    NourishH2(recipeCard.title,
                        modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.8f)
                    )

                    // back button:
                    IconButton(
                        onClick = { },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    }
                }


            }
            

            // in future, id here can be used to edit the complete status of the recipe.
            Text("Method:")
            recipeCard.method.forEachIndexed{ i, (instruction, complete, id) ->
                Text(
                    text = "Step ${i+1}. $instruction",
                    textDecoration = if (complete) { TextDecoration.LineThrough} else TextDecoration.None
                )
                Button(
                    onClick = {
                        recipeBookViewModel.markStepAsComplete(id, !complete)
                    }
                ) {
                    Text("Mark as complete")
                }
            }

            Text("Serves: ${recipeCard.serves}")
            Text("Recipe type: ${recipeCard.recipeType}")
            if (recipeCard.recipeTags.isNotEmpty()) {
                Text("Tags:")
                recipeCard.recipeTags.forEach { (tag, colour) ->
                    Text(
                        text = tag,
                        color = colour.onBackgroundColour()
                    )
                }
            }

            Text("Storage guidance: ${recipeCard.storageInstructions}")

        }
    }
}

// shaves off the decimal if it is .0, otherwise keeps it.
// e.g. 5.0 -> 5 but 5.5 remains -> 5.5
fun formatIngredientAmount(
    ingredientAmount: Double
): String {
    if (ingredientAmount % 1.0 == 0.0) {
        return ingredientAmount.toInt().toString()
    }
    return ingredientAmount.toString()
}
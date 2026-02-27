package uk.co.imoqen.nourish.recipediscovery

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import java.io.File

/**
 * View for RecipeDiscovery, where LLM calls can be made and recipes thus generated.
 * @param recipeDiscoveryViewModel - the view model holding methods and state for use in the view.
 */
@Composable
fun RecipeDiscoveryScreen(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel
) {
    val uiState by recipeDiscoveryViewModel.uiState.collectAsState()

    val keyboardFocusManager = LocalFocusManager.current // To close the keyboard once user submits input

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
    ) {
        OutlinedTextField(
            value = uiState.userRequest,
            onValueChange = { request: String ->
                recipeDiscoveryViewModel.setUserRequest(request)
            },
            placeholder = {
                Text(uiState.placeholderText)
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Uses the inputted user request to make the LLM call using the ViewModel
        val context = LocalContext.current

        Button(
            onClick = {
                recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS) // also covers dishes from ingredients image, but have to pass this in when making the llm call as it is different.
                recipeDiscoveryViewModel.setPlaceholderText("Enter your ingredients")
            }
        ) {
            Text("From ingredients")
        }

        Button(
            onClick = {
                recipeDiscoveryViewModel.setRequestType(LlmRequestType.RECIPE) // also covers dishes from ingredients image, but have to pass this in when making the llm call as it is different.
                recipeDiscoveryViewModel.setPlaceholderText("What would you like to cook?")
            }
        ) {
            Text("Generate recipe")
        }


        Button(
            onClick = {
                keyboardFocusManager.clearFocus()
                // generate a recipe from text:
                if (uiState.savedImageFilename == "" && uiState.requestType == LlmRequestType.RECIPE) {
                    Log.i("recipe", "making recipe")
                    // generate recipe from text
                    recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE) // make llm call
                }
                // generate potential dishes from ingredients list:
                else if (uiState.savedImageFilename == "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS) {
                    recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.DISHES_FROM_INGREDIENTS)
                }
                // generate potential dishes from image of ingredients, e.g. image of contents of a fridge:
                else if (uiState.savedImageFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS) {
                    recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE, uiState.savedImageFilename, context.filesDir)
                }
                // generate potential dishes from an image of a dish, e.g. image of pasta:
                else {
                    Log.i("recipe", "from image")
                    // generate recipe from image
                    recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.DISHES_FROM_IMAGE, uiState.savedImageFilename, context.filesDir)
                }
            }
        ) {
            Text("Discover a recipe")
        }

        // Photo picker: https://developer.android.com/training/data-storage/shared/photo-picker#compose
        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            recipeDiscoveryViewModel.handleImage(uri, context)
        }

        // only allow image upload if not exist already
        if (uiState.savedImageFilename == "") {
            Button(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text("Upload photo")
            }
        }

        val dishes = uiState.dishes
        if (dishes != null) {
            val dishes = uiState.dishes
            val arrayOfDishes = listOf(dishes!!.firstMostLikelyDish, dishes.secondMostLikelyDish, dishes.thirdMostLikelyDish, dishes.fourthMostLikelyDish)
            Text("Choose a recipe to generate:")
            arrayOfDishes.forEach { dish ->
                Button(onClick={
                    if (dishes.ingredientsIdentified != null) {
                        recipeDiscoveryViewModel.setUserRequest(dish + ", using only ingredients: ${dishes.ingredientsIdentified}")
                    } else {
                        recipeDiscoveryViewModel.setUserRequest(dish)
                    }
                    if (uiState.savedImageFilename == "") {
                        // generate without image
                        Log.i("GENERATING", "generating without image")
                        recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE)
                    } else {
                        // generate with image
                        Log.i("GENERATING", "generating with image")
                        recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH, uiState.savedImageFilename, context.filesDir)
                    }
                }){
                    Text(dish)
                }
            }
            if (dishes.ingredientsIdentified != null) {
                Text("Ingredients identified: ${dishes.ingredientsIdentified}")
            }
        }

        // Outputs the response, or error if occurred.
        if (uiState.apiResponse != "") {
            Log.i("recipe res", "not null")
            Text(
                uiState.apiResponse,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.savedImageFilename != "") {
            val imageFile = File(context.filesDir, uiState.savedImageFilename)
            if (imageFile.exists()) {
                //Use Coil to display the selected image
                val painter = rememberAsyncImagePainter(
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageFile)
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = null
                )

                Button(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Choose a new photo")
                }
                Button(onClick = { imageFile.delete(); recipeDiscoveryViewModel.setSavedImageFilename("") }) {
                    Text("Remove photo") // NOTE: this deletes the image file so if a recipe has been generated using this image, it will be deleted.
                }
            }
        }

        IndeterminateCircularIndicator()
    }
}

// Progress bar:
// https://developer.android.com/develop/ui/compose/components/progress
@Composable
fun IndeterminateCircularIndicator(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel = hiltViewModel()
) {
    val uiState by recipeDiscoveryViewModel.uiState.collectAsState()

    // if awaiting anything, need to show the loading screen
    if (!uiState.awaitingResponse) return

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}
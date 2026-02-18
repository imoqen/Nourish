package uk.co.imoqen.nourish.recipediscovery

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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

    Column() {
        OutlinedTextField(
            value = uiState.userRequest,
            onValueChange = { request: String ->
                recipeDiscoveryViewModel.setUserRequest(request)
            },
            placeholder = {
                Text("What would you like to cook?")
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Uses the inputted user request to make the LLM call using the ViewModel
        val context = LocalContext.current

        Button(
            onClick = {
                keyboardFocusManager.clearFocus()
                if (uiState.savedImageFilename == "") {
                    // generate recipe from text
                    recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE) // make llm call
                }
                else {
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
            Button(onClick = { recipeDiscoveryViewModel.setSavedImageFilename("") }) {
                Text("Remove photo")
                // potentially actually delete it from local storage here too
            }
        }

        // Outputs the response, or error if occurred.
        if (uiState.apiResponse != "") {
            Text(uiState.apiResponse)
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
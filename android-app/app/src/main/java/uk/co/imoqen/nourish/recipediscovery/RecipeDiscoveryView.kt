package uk.co.imoqen.nourish.recipediscovery

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import uk.co.imoqen.nourish.common.ErrorMessage

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
        Button(
            onClick = {
                keyboardFocusManager.clearFocus()
                recipeDiscoveryViewModel.makeLlmApiCall()
            }
        ) {
            Text("Discover a recipe")
        }

        val context = LocalContext.current

        // Photo picker: https://developer.android.com/training/data-storage/shared/photo-picker#compose
        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                recipeDiscoveryViewModel.compressImage(uri, context.contentResolver)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        Button(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))} ) {
            Text("from photo")
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

    if (!uiState.awaitingResponse) return

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}
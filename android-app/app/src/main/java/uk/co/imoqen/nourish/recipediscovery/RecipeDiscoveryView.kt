package uk.co.imoqen.nourish.recipediscovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import uk.co.imoqen.nourish.constants.ErrorMessage

/**
 * View for RecipeDiscovery, where LLM calls can be made and recipes thus generated.
 * @param recipeDiscoveryViewModel - the view model holding methods and state for use in the view.
 */
@Composable
fun RecipeDiscoveryScreen(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel
) {
    val uiState by recipeDiscoveryViewModel.uiState.collectAsState()

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
                if (uiState.userRequest == "") {
                    recipeDiscoveryViewModel.setApiResponseError(ErrorMessage.API_ERROR_BLANK_PROMPT)
                    recipeDiscoveryViewModel.setApiResponse("")
                } else {
                    recipeDiscoveryViewModel.makeLlmApiCall()
                }
            }
        ) {
            Text("Discover a recipe")
        }

        // Outputs the response, or error if occurred.
        if (uiState.apiResponse != "") {
            Text(uiState.apiResponse)
        }
        else if (uiState.apiResponseError != "") {
            Text(uiState.apiResponseError)
        }
    }
}
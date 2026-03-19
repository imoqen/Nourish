package uk.co.imoqen.nourish.recipecreation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.TypesDisplayForMutuallyExclusiveFiltering
import uk.co.imoqen.nourish.common.dimmedBackgroundColour
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryViewModel
import uk.co.imoqen.nourish.tag.FilterState
import uk.co.imoqen.nourish.tag.RecipeTagSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationScreen(
    viewModel: RecipeCreationViewModel,
    onBack: () -> Unit,
) {
    // Title top area
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                // bottom corners are rounded, top are not:
                shape = RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            )
            .padding(vertical = 16.dp)
    ) {
        NourishH2(
            "Add a Recipe",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
        )

        // back button:
        IconButton(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = null
            )
        }
    }

    val uiState = viewModel.uiState.collectAsState().value
    val keyboardFocusManager = LocalFocusManager.current
    LaunchedEffect("GET_RECIPE_TYPES_AND_TAGS") {
        viewModel.getAllRecipeTypes()
        viewModel.getAllTagsForUser()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(top=70.dp) // room from top for the title area
    ) {
        // Contents, * means it is mandatory:

        // Title of recipe *
        NourishTextField(
            value = uiState.title,
            onValueChange = { newRecipeTitle ->
                viewModel.setTitle(newRecipeTitle)
            },
            placeholder = "Enter a title",
            onDone = { keyboardFocusManager.clearFocus() },
            leadingIcon = ImageVector.vectorResource(R.drawable.text),
        )

        // Type of recipe *
        NourishTextDivider("Recipe Type")
        Box(
            modifier = Modifier.clickable { viewModel.setDisplayRecipeTypeSelector(true) }
                .clip(RoundedCornerShape(12.dp))
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
        ) {
            NourishH3(text = uiState.recipeType?.title ?: "None")
        }

        // Tags
        NourishTextDivider("Recipe Tags")
        val nutritionalTagSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
        )
        Button(
            onClick = { viewModel.setFilterDisplayState(FilterState.APPLY) }
        ) {
            "Add tags"
        }
        if (uiState.tagSheetState.filterDisplayState != FilterState.NONE) {
            RecipeTagSelection(
                sheetState = nutritionalTagSheetState,
                tagSheetState = uiState.tagSheetState,
                tagSheetAction = viewModel::onTagSheetAction
            )
        }

        // Number of servings * (defaults to 1)
        // Ingredients *

        // Method *

        // Storage guidance
    }

    // modal bottom sheet for selecting recipe type:
    if (uiState.displayRecipeTypeSelector) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.setDisplayRecipeTypeSelector(false)
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NourishTextDivider(
                    "Choose a type for the dish",
                )
                TypesDisplayForMutuallyExclusiveFiltering(
                    uiState.recipeTypes,
                    uiState.recipeType,
                    viewModel::setRecipeType,
                    allowNone = false
                )
            }
        }
    }
}
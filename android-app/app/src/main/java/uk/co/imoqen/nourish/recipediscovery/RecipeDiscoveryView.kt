package uk.co.imoqen.nourish.recipediscovery

import android.graphics.Paint
import android.util.Log
import uk.co.imoqen.nourish.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishH4
import uk.co.imoqen.nourish.common.NourishIconButton
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.NourishTextMessage
import uk.co.imoqen.nourish.common.TextSender
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
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            NourishTextMessage(
                textSender = TextSender.LLM,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Box(
                    modifier = Modifier.padding(8.dp)
                ) {
                    NourishH3(
                        text = "Let's find a recipe for you.",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            NourishTextMessage(
                textSender = TextSender.USER,
                modifier = Modifier.align(Alignment.End)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NourishH4(
                        text = "Discover a recipe, by entering a...",
                        modifier = Modifier.padding(8.dp)
                    )

                    NourishIconTextButton(
                        onClick = {
                            recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_DESCRIPTION)
                            recipeDiscoveryViewModel.setPlaceholderText("Dish or description ...")
                            recipeDiscoveryViewModel.setRequestType(LlmRequestType.RECIPE)
                                  },
                        painter = painterResource(R.drawable.cook),
                        description = "Dish or Description",
                        modifier = Modifier.padding(top=8.dp)
                    )

                    NourishIconTextButton(
                        onClick = {
                            recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_IMAGE)
                            recipeDiscoveryViewModel.setPlaceholderText("Optional details ...")
                            recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_IMAGE)
                                  },
                        painter = painterResource(R.drawable.camera),
                        description = "Photo of a Dish",
                        modifier = Modifier.padding(top=8.dp)
                    )

                    NourishIconTextButton(
                        onClick = {
                            recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_INGREDIENTS)
                            recipeDiscoveryViewModel.setPlaceholderText("Ingredients ...")
                            recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS)
                                  },
                        painter = painterResource(R.drawable.ingredients),
                        description = "List or Photo of ingredients",
                        modifier = Modifier.padding(8.dp)
                    )

                }
            }

            if (uiState.userChoice != null) {
                when (uiState.userChoice) {
                    LlmMode.FROM_DESCRIPTION -> {
                        NourishTextMessage(
                            textSender = TextSender.LLM,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                NourishH4(
                                    text = "Enter the name or description of a dish.",
                                    modifier = Modifier.padding(8.dp)
                                )
                                NourishTextDivider("Tip")

                                Text(
                                    text = "The dish does not need to be precise; for example, you can enter 'something sweet but healthy ...'",
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    LlmMode.FROM_IMAGE -> {
                        NourishTextMessage(
                            textSender = TextSender.LLM,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                NourishH4(
                                    text = "Upload the image of the dish.",
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                )

                                NourishTextDivider("Tip")

                                Text(
                                    text = "You can also provide further information as text, if you wish.",
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    LlmMode.FROM_INGREDIENTS -> {
                        NourishTextMessage(
                            textSender = TextSender.LLM,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                NourishH4(
                                    text = "List your ingredients below, or upload an image of them.",
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                )

                                NourishTextDivider("Tip")

                                Text(
                                    text = "Try uploading an image of the contents of your fridge!",
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                        }
                    }

                    null -> {
                        // never reached
                    }
                }

                // if image is uploaded, change request type for the ingredient case

                val context = LocalContext.current
                val keyboardFocusManager = LocalFocusManager.current
                // Photo picker: https://developer.android.com/training/data-storage/shared/photo-picker#compose
                // Registers a photo picker activity launcher in single-select mode.
                val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    recipeDiscoveryViewModel.handleImage(uri, context)
                }

                Column( ) {

                    // image is above if selected:
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

                            Box(
                                modifier = Modifier
                                    .height(200.dp).width(200.dp)
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,

                                    // scale down the height of it
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                NourishIconButton(
                                    onClick = {  },
                                    painter = painterResource(R.drawable.expand),
                                    modifier = Modifier.align(
                                        Alignment.TopEnd
                                    ).offset(x=16.dp)
                                )
                                NourishIconButton(
                                    onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    painter = painterResource(R.drawable.edit),
                                    modifier = Modifier.align(
                                        Alignment.CenterEnd
                                    ).offset(x=16.dp)
                                )
                                NourishIconButton(
                                    onClick = { imageFile.delete(); recipeDiscoveryViewModel.setSavedImageFilename("") },
                                    painter = painterResource(R.drawable.delete),
                                    modifier = Modifier.align(
                                        Alignment.BottomEnd
                                    ).offset(x=16.dp)
                                )
                            }

                        }
                    }

                    //
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface
                            )
                    ) {
                        // text box for user text input:
                        NourishTextField(
                            value = uiState.userRequest,
                            onValueChange = { request: String ->
                                recipeDiscoveryViewModel.setUserRequest(request)
                            },
                            placeholder = uiState.placeholderText,
                            icon = Icons.Filled.Create,
                            onDone = { keyboardFocusManager.clearFocus() },
                            modifier =
                                if (uiState.userChoice == LlmMode.FROM_DESCRIPTION) {
                                    Modifier.fillMaxWidth()
                                } // only leaves space for cam if needed
                                else {
                                    Modifier.fillMaxWidth(0.85f)
                                }
                                    .then(Modifier.padding(4.dp)),
                            trailingIcon = Icons.Filled.Send,
                            onTrailingIconPress = {

                                // generate the recipe:

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
                                    recipeDiscoveryViewModel.makeLlmApiCall(
                                        LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE,
                                        uiState.savedImageFilename,
                                        context.filesDir
                                    )
                                }
                                // generate potential dishes from an image of a dish, e.g. image of pasta:
                                else {
                                    Log.i("recipe", "from image")
                                    // generate recipe from image
                                    recipeDiscoveryViewModel.makeLlmApiCall(
                                        LlmRequestType.DISHES_FROM_IMAGE,
                                        uiState.savedImageFilename,
                                        context.filesDir
                                    )
                                }
                            },
                            alwaysDisplayTrailingIcon = true
                        )
                        // only allow image upload if not exist already
                        if (uiState.savedImageFilename == "" && uiState.userChoice != LlmMode.FROM_DESCRIPTION) {
                            NourishIconButton(
                                onClick = {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                painter = painterResource(R.drawable.camera),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }

            }




        }

        /*
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



        IndeterminateCircularIndicator()

         */
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
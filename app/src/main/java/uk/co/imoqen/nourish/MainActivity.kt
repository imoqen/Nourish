package uk.co.imoqen.nourish

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.imoqen.nourish.authentication.AuthenticationViewModel
import uk.co.imoqen.nourish.authentication.LoginScreen
import uk.co.imoqen.nourish.navigation.AppNavHost
import uk.co.imoqen.nourish.navigation.NourishNavigationBar
import uk.co.imoqen.nourish.navigation.TabDestination
import uk.co.imoqen.nourish.network.ApiClient
import uk.co.imoqen.nourish.network.LlmRequest
import uk.co.imoqen.nourish.network.LlmResponse
import uk.co.imoqen.nourish.ui.theme.NourishTheme


class MainActivity : ComponentActivity() {

    //private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        //auth = Firebase.auth

        enableEdgeToEdge()

        setContent {
            NourishTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    // Creates the navigation controller:
    val navController = rememberNavController()

    // The start destination to begin the app at:
    val startDestination = TabDestination.RECIPE_FINDER

    // authentication view model, used across the whole app therefore declared here:
    val authenticationViewModel: AuthenticationViewModel = viewModel()

    // get the authentication state (not the ui state as this is used only in the authentication view):
    val authenticationState by authenticationViewModel.state.collectAsState()

    Log.i("inii", "intiialis")

    val loggedInUser = authenticationState.currentUser

    if (loggedInUser == null) {
        // display sign up page if null
        Scaffold(
            modifier = modifier,
        ) { contentPadding ->
            LoginScreen(modifier = Modifier.padding(contentPadding), authenticationViewModel)
        }

    } else {
        // otherwise display rest of app and pass the logged in user and logout function to everything else

        // Note that the selected destinations are remembered by the NavController/NavHost, so dont need to remember it here.
        // The currently selected tab value to display (dynamically determined based on the route - first char of the string x/route),
        // required as RouteDestinations are within tabs. So must determine which tab to display for each route.
        val selectedDestinationTab = navController.currentBackStackEntryAsState().value?.destination?.route?.firstOrNull() ?: startDestination.route[0]

        Scaffold(
            modifier = modifier,
            bottomBar = {
                // Bottom navbar destinations are the first 3 values of the TabDestination enum (0..2)
                NourishNavigationBar(TabDestination.entries.take(3), selectedDestinationTab, navController)
                // Potentially move this to Navigation.kt
            },
            topBar = {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    //Text("Nourish") //title text.
                    NourishNavigationBar(TabDestination.entries.drop(3), selectedDestinationTab, navController)
                }
                // Top navbar destinations are those after the first 3 items of the TabDestination enum (3.. end)
            }
        ) { contentPadding ->
            // Navigation is controlled by the app navigation host, also with the auth state and sign out method passed in.
            // current user must not be null here so !! can be used, since check was above. as must be logged in.
            AppNavHost(navController, startDestination.route, loggedInUser, authenticationViewModel::signOut, modifier = Modifier.padding(contentPadding))

        }
    }

}

@Composable
fun RecipeFinderScreen(
    loggedInUser: FirebaseUser,
    authenticationViewModel: AuthenticationViewModel = viewModel()
    //authenticationViewModel: AuthenticationViewModel put view models here
) {
    //LoginScreen()
    Column() {
        Text("Recipe Finder Screen, hello ${loggedInUser.email}")

        val data = rememberSaveable { mutableStateOf("") }
        val userInput = rememberSaveable { mutableStateOf("") }

        OutlinedTextField(
            value = userInput.value,
            onValueChange = { newValue: String ->
                userInput.value = newValue
            },
            placeholder = {
                Text("What would you like to cook?")
            },
            modifier = Modifier.fillMaxWidth()
        )
        // will fail if: NO INTERNET PERMISSIONS GRANTED OR POOR INTERNET CONNECTION. need to handle exception, not currently being handled.
        // permission has been added to the android manifest to get internet permission.

        // tutorials followed: https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet#6
        // https://medium.com/@imkuldeepsinghrai/api-calls-with-retrofit-in-android-kotlin-a-comprehensive-guide-e049e19deba9
        Button(
            onClick = {
                val request = LlmRequest("Generate a recipe for this food, give fewest steps and list ingredients at the start. Don't say okay lets do it etc, just get straight to the recipe. Keep it in under 100 words, list ingredients all on one line and dont do formatting apart from new lines for each recipe step: $userInput")
                val loggedInToken = FirebaseAuth.getInstance().currentUser!!.getIdToken(true).addOnSuccessListener { tokenResult ->
                    Log.i("true token", "true: ${tokenResult.token}")
                }
                val token = loggedInUser.getIdToken(true).addOnSuccessListener { tokenResult ->
                    // if token is obtained, send request with that token
                    val token = tokenResult.token
                    Log.i("token success:", "the token is: $token")
                    if (token != null) {
                        val call = ApiClient.apiService.generateRecipe(request, "Bearer $token")

                        // callback, call, response etc are all retrofit2. not javax. had issue where used javax
                        call.enqueue(object : Callback<LlmResponse> {
                            override fun onResponse(
                                call: Call<LlmResponse>,
                                response: Response<LlmResponse>
                            ) {
                                if (response.isSuccessful) {
                                    val post = response.body()
                                    if (post != null) {
                                        data.value =
                                            post.text // gets the text field from the response
                                    }
                                } else {
                                    Log.i("api error", "an error occurred")
                                }
                            }

                            override fun onFailure(call: Call<LlmResponse>, t: Throwable) {
                                Log.i("api failure", "a failure occurred")
                            }
                        })
                    }
                }
                    .addOnFailureListener {
                        // otherwise return error
                        Log.i("error", "error while getting token")
                    }



            }
        ) {
            Text("Click me to make the POST request")
        }

        if (data.value != "") {
            Text(data.value)
        }
    }
}

@Composable
fun RecipeBookScreen() {
    Text("Recipe Book Screen")
}

@Composable
fun ShoppingListScreen() {
    Text("Shopping List Screen")
}

@Composable
fun ProfileScreen(
    signOut: () -> Unit // sign out method
) {

    Column() {

        Text("Profile Screen")

        Button(
            onClick = {
                signOut() // sign out locally by changing the auth current user state via the method
                Log.i("signoutreq", "signing out")
            }
        ) {
            Text("Sign out")
        }
    }
}

@Composable
fun SettingsScreen() {
    Text("Settings Screen")
}
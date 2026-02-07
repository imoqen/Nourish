package uk.co.imoqen.nourish

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.imoqen.nourish.authentication.AuthenticationViewModel
import uk.co.imoqen.nourish.authentication.LoginScreen
import uk.co.imoqen.nourish.navigation.AppNavHost
import uk.co.imoqen.nourish.navigation.NourishNavigationBar
import uk.co.imoqen.nourish.navigation.TabDestination
import uk.co.imoqen.nourish.network.ApiClient
import uk.co.imoqen.nourish.network.LlmError
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
) {
    //LoginScreen()
    Column() {
        Text("Recipe Finder Screen, hello ${loggedInUser.email}")

        val data = rememberSaveable { mutableStateOf("") }
        val responseError = rememberSaveable { mutableStateOf("") }
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
                if (userInput.value == "") { responseError.value = "Prompt cannot be blank."
                } else {
                    val request =
                        LlmRequest("Test request. simply reply with the following text plus test appended if you received this: ${userInput.value}")
                    val loggedInToken = FirebaseAuth.getInstance().currentUser!!.getIdToken(true)
                        .addOnSuccessListener { tokenResult ->
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
                                            //responseError.value = post.error.toString()
                                        }
                                    } else {
                                        val error = response.errorBody()?.string()
                                        // extracts the error json from the error string using the LlmError template
                                        val errorJson = Gson().fromJson(error, LlmError::class.java)
                                        responseError.value = errorJson.error
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
            }
        ) {
            Text("Click me to make the POST request")
        }

        if (data.value != "") {
            Text(data.value)
        }
        else if (responseError.value != "") {
            Text(responseError.value)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    signOut: () -> Unit // sign out method
) {

    Column() {

        Text("Profile Screen")

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        var showBottomSheet by remember { mutableStateOf(false) }
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("") }

        // to ensure password reset is only possible for username/password accounts (not Google accounts), to avoid confusion:
        val hasPasswordProvider = Firebase.auth.currentUser!!.providerData.any { it.providerId == "password" }

        if (hasPasswordProvider) {
            Button(
                onClick = {
                    showBottomSheet = true
                }
            ) {
                Text("Change password")
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    // Sheet content
                    Column() {
                        // old password:
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { password ->
                                oldPassword = password
                            },
                            placeholder = {
                                Text("Enter your account's existing password")
                            },
                            leadingIcon = {
                                Icons.Filled.Lock
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // new password:
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { updatedPassword ->
                                newPassword = updatedPassword
                            },
                            placeholder = {
                                Text("Enter a new password for your account")
                            },
                            leadingIcon = {
                                Icons.Filled.Lock
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (oldPassword == "" || newPassword == "") {
                                    status = "All fields must not be blank."
                                    return@Button // early return
                                }
                                val credentials = EmailAuthProvider.getCredential(
                                    Firebase.auth.currentUser!!.email!!,
                                    oldPassword
                                )
                                Firebase.auth.currentUser!!.reauthenticate(credentials)
                                    .addOnSuccessListener {
                                        // now can change the password as user authenticated successfully:
                                        Firebase.auth.currentUser!!.updatePassword(newPassword)
                                            .addOnSuccessListener {
                                                status = "Password updated successfully."
                                            }
                                            .addOnFailureListener {
                                                status = "Password update failed."
                                            }
                                    }
                                    .addOnFailureListener { error ->

                                        status = "The existing password inputted is incorrect."

                                    }
                            }
                        ) {
                            Text("Update Password")
                        }

                        if (status != "") {
                            Text(status)
                        }

                        Button(onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }) {
                            Text("Hide bottom sheet")
                        }
                    }
                }
            }
        }

        var deleteAccountBottomSheetState = rememberModalBottomSheetState()
        var showDeleteAccountBottomSheet by remember { mutableStateOf(false) }
        var deleteAccountPassword by remember { mutableStateOf("") }
        var deleteStatus by remember { mutableStateOf("") }


        Button(
            onClick = {
                showDeleteAccountBottomSheet = true
            }
        ) {
            Text("Delete Account")
        }

        if (showDeleteAccountBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDeleteAccountBottomSheet = false
                },
                sheetState = deleteAccountBottomSheetState
            ) {
                // Sheet content
                Column() {
                    Text("Are you sure you wish to delete your account?")
                    Text("This will delete all data associated with your account. This action cannot be undone.")

                    if (hasPasswordProvider) {
                        Text("Please type your password below to confirm.")
                        // old password:
                        OutlinedTextField(
                            value = deleteAccountPassword,
                            onValueChange = { password ->
                                deleteAccountPassword = password
                            },
                            placeholder = {
                                Text("Enter your password")
                            },
                            leadingIcon = {
                                Icons.Filled.Lock
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (deleteAccountPassword == "") {
                                    deleteStatus = "The password must not be blank."
                                    return@Button // early return
                                }
                                val credentials = EmailAuthProvider.getCredential(
                                    Firebase.auth.currentUser!!.email!!,
                                    deleteAccountPassword
                                )
                                Firebase.auth.currentUser!!.reauthenticate(credentials)
                                    .addOnSuccessListener {
                                        // delete all of the user data from firestore:
                                        scope.launch() {
                                            deleteAllUserData(Firebase.auth.currentUser!!)
                                        }
                                        // then delete the user:
                                        Firebase.auth.currentUser!!.delete().addOnSuccessListener {
                                            signOut() // sign them out as the account is now deleted

                                        }
                                        // now can change the password as user authenticated successfully:
                                        //deleteStatus = "Account deleted successfully."
                                    }
                                    .addOnFailureListener { error ->

                                        deleteStatus =
                                            "Invalid password. Account could not be deleted."

                                    }
                            }
                        ) {
                            Text("Delete Account")
                        }
                    }
                    else {
                        Text("Confirm by signing back in to google")
                        val context = LocalContext.current
                        val credentialManager = CredentialManager.create(context)
                        val scope = rememberCoroutineScope()
                        val activity = LocalActivity.current

                        Button(
                            onClick = {
                                scope.launch() {
                                    // https://stackoverflow.com/questions/40898952/how-to-re-authenticate-a-user-on-firebase-with-google-provider
                                    val provider = OAuthProvider.newBuilder("google.com").build()

                                    if (activity != null) {
                                        Firebase.auth.currentUser!!.startActivityForReauthenticateWithProvider(
                                            activity,
                                            provider
                                        )
                                            .addOnSuccessListener {
                                                // delete all of the user data from firestore:
                                                scope.launch() {
                                                    deleteAllUserData(Firebase.auth.currentUser!!)
                                                }
                                                // then delete the user:
                                                Firebase.auth.currentUser!!.delete().addOnSuccessListener {
                                                signOut() // sign them out as the account is now deleted
                                            } }
                                            .addOnFailureListener { e -> deleteStatus = "error: $e" }
                                    }

                                }
                            }
                        ) {
                            Text("Delete Account")
                        }

                    }

                    if (deleteStatus != "") {
                        Text(deleteStatus)
                    }

                    Button(onClick = {
                        scope.launch { deleteAccountBottomSheetState.hide() }.invokeOnCompletion {
                            if (!deleteAccountBottomSheetState.isVisible) {
                                showDeleteAccountBottomSheet = false
                            }
                        }
                    }) {
                        Text("Hide bottom sheet")
                    }
                }
            }
        }



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

suspend fun deleteAllUserData(user: FirebaseUser) {
    val db = Firebase.firestore

    val userRef = db.collection("users").document(user.uid)
    // recursively delete all of the user data
    val tokenUsages = userRef.collection("token-usage").get().await().documents
    tokenUsages.forEach{ tokenUsage ->
        tokenUsage.reference.delete().await()
    }
    userRef.delete()
}

@Composable
fun SettingsScreen() {
    Text("Settings Screen")
}
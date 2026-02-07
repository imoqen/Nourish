package uk.co.imoqen.nourish

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.co.imoqen.nourish.authentication.AuthenticationViewModel
import uk.co.imoqen.nourish.authentication.LoginScreen
import uk.co.imoqen.nourish.navigation.AppNavHost
import uk.co.imoqen.nourish.navigation.NourishNavigationBar
import uk.co.imoqen.nourish.navigation.TabDestination
import uk.co.imoqen.nourish.ui.theme.NourishTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    //val loggedIn = authenticationState.loggedIn
    Log.i("auth state: ", "${authenticationState.loggedIn}")
    if (!authenticationState.loggedIn) {
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
        val selectedDestinationTab =
            navController.currentBackStackEntryAsState().value?.destination?.route?.firstOrNull()
                ?: startDestination.route[0]

        Scaffold(
            modifier = modifier,
            bottomBar = {
                // Bottom navbar destinations are the first 3 values of the TabDestination enum (0..2)
                NourishNavigationBar(
                    TabDestination.entries.take(3),
                    selectedDestinationTab,
                    navController
                )
                // Potentially move this to Navigation.kt
            },
            topBar = {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    //Text("Nourish") //title text.
                    NourishNavigationBar(
                        TabDestination.entries.drop(3),
                        selectedDestinationTab,
                        navController
                    )
                }
                // Top navbar destinations are those after the first 3 items of the TabDestination enum (3.. end)
            }
        ) { contentPadding ->
            // Navigation is controlled by the app navigation host, also with the auth state and sign out method passed in.
            // current user must not be null here so !! can be used, since check was above. as must be logged in.
            AppNavHost(
                navController,
                startDestination.route,
                authenticationViewModel::signOut,
                modifier = Modifier.padding(contentPadding)
            )

        }
    }

}
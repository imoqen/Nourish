package uk.co.imoqen.nourish

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import uk.co.imoqen.nourish.navigation.AppNavHost
import uk.co.imoqen.nourish.navigation.TabDestination
import uk.co.imoqen.nourish.ui.theme.NourishTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

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

    // Note that the selected destinations are remembered by the NavController/NavHost, so dont need to remember it here.
    // The currently selected route to display
    val selectedDestinationRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: startDestination.route
    // Then the tab value of this destination, required as RouteDestinations are within tabs. So must determine which tab to display for each route.
    var selectedDestinationTab = selectedDestinationRoute[0] // gets the first character as this encodes which tab the content is on

    Log.i("selected tab", "$selectedDestinationTab and destination route: $selectedDestinationRoute")

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Potentially move this to Navigation.kt
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                // Bottom navbar destinations are the first 3 values of the TabDestination enum (0..2)
                TabDestination.entries.take(3).forEachIndexed { index, destination ->
                    NavigationBarItem(
                        // If the selected destination is equal to the destination identified, highlight that as being selected
                        selected = selectedDestinationTab == destination.route[0],
                        onClick = {
                            navController.navigate(route = destination.route) // Update the currently selected item in navigation
                            selectedDestinationTab = destination.route[0] // Then select the corresponding screen to update which icon is highlighted in the navbar
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.iconContentDescription
                            )
                        },
                        label = { Text(destination.title) }
                    )
                }
            }
        },
        topBar = {
            Text("Nourish")
            // Potentially move this to Navigation.kt
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                // Top navbar destinations are those after the first 3 items of the TabDestination enum (3.. end)
                TabDestination.entries.drop(3).forEachIndexed { index, destination ->
                    NavigationBarItem(
                        // If the selected destination is equal to the destination identified, highlight that as being selected
                        selected = selectedDestinationTab == destination.route[0],
                        onClick = {
                            navController.navigate(route = destination.route) // Update the currently selected item in navigation
                            selectedDestinationTab = destination.route[0] // Then select the corresponding screen to update which icon is highlighted in the navbar
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.iconContentDescription
                            )
                        },
                        label = { Text(destination.title) }
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(navController, startDestination.route, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun RecipeFinderScreen() {
    Text("Recipe Finder Screen")
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
fun ProfileScreen() {
    Text("Profile Screen")
}

@Composable
fun SettingsScreen() {
    Text("Settings Screen")
}
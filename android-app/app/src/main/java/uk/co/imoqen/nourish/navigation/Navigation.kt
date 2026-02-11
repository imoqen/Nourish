package uk.co.imoqen.nourish.navigation

import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import uk.co.imoqen.nourish.profile.ProfileScreen
import uk.co.imoqen.nourish.profile.ProfileViewModel
import uk.co.imoqen.nourish.recipebook.RecipeBookScreen
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryViewModel
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryScreen
import uk.co.imoqen.nourish.shoppinglist.ShoppingListScreen


// Navigation host: https://developer.android.com/guide/navigation/design#compose

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        Log.i("nav host", "nav hosting")
        // From the navigation bar, to go to certain screens:

        // Bottom Navbar: Tab destinations
        composable(TabDestination.RECIPE_FINDER.route) {
            val recipeDiscoveryViewModel: RecipeDiscoveryViewModel = hiltViewModel()
            // View models to pass in:
            // PUT VIEW MODELS USED SPECIFICALLY ONLY IN EACH SCREEN, IN THESE.
            // if share, can declare it on line 40 (above NavHost) in this composable and pass into both. (havent tested this but assume it will work)
            // as the authentication view model is used across the app,
            // The composable to display for this tab:
            RecipeDiscoveryScreen(recipeDiscoveryViewModel)
        }
        composable(TabDestination.RECIPE_BOOK.route) { RecipeBookScreen() }
        composable(TabDestination.SHOPPING_LIST.route) { ShoppingListScreen() }

        // Top Navbar: Tab destinations
        composable(TabDestination.PROFILE.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(profileViewModel)
        }
        //composable(TabDestination.SETTINGS.route) { SettingsScreen() }

        // From within screens:

        // Can add Route destinations too (those not from tabs)
        // can add here. then can add params to them as shown in this video: https://www.youtube.com/watch?v=wJKwsI5WUI4
        // only pass IDs, not complex data, then can find the data from the view model and it can make calls to the db.
    }
}

@Composable
fun NourishNavigationBar(
    navigationItems: List<TabDestination>,
    selectedDestinationTab: Char,
    navController: NavController
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        // Bottom navbar destinations are the first 3 values of the TabDestination enum (0..2)
        navigationItems.forEachIndexed { index, destination ->
            NavigationBarItem(
                // If the selected destination is equal to the destination identified, highlight that as being selected
                selected = selectedDestinationTab == destination.route[0],
                onClick = {
                    navController.navigate(route = destination.route) { // Update the currently selected item in navigation
                        //popUpTo(navController.graph.id) {inclusive = true; saveState = false} // clear root back stack, which disposes app back stack entries
                        launchSingleTop = true // ensures destinations are not duplicated
                        // the following 4 lines are to prevent previous destinations from being destroyed and recreated
                        // e.g. a -> b -> a, to use the same a as the start:
                        // from android docs https://developer.android.com/guide/navigation/backstack/multi-back-stacks
                        restoreState = false // to reset view models as user has changed
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
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
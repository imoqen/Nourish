package uk.co.imoqen.nourish.navigation

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import uk.co.imoqen.nourish.RecipeFinderScreen
import androidx.navigation.compose.composable
import uk.co.imoqen.nourish.ProfileScreen
import uk.co.imoqen.nourish.RecipeBookScreen
import uk.co.imoqen.nourish.SettingsScreen
import uk.co.imoqen.nourish.ShoppingListScreen


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
        // From the navigation bar, to go to certain screens:

        // Bottom Navbar: Tab destinations
        composable(TabDestination.RECIPE_FINDER.route) { RecipeFinderScreen() }
        composable(TabDestination.RECIPE_BOOK.route) { RecipeBookScreen() }
        composable(TabDestination.SHOPPING_LIST.route) { ShoppingListScreen() }

        // Top Navbar: Tab destinations
        composable(TabDestination.PROFILE.route) { ProfileScreen() }
        composable(TabDestination.SETTINGS.route) { SettingsScreen() }

        // From within screens:

        // Can add Route destinations too (those not from tabs)
        // can add here. then can add params to them as shown in this video: https://www.youtube.com/watch?v=wJKwsI5WUI4
        // only pass IDs, not complex data, then can find the data from the view model and it can make calls to the db.
    }

}
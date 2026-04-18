package uk.co.imoqen.nourish.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import uk.co.imoqen.nourish.profile.ProfileScreen
import uk.co.imoqen.nourish.profile.ProfileViewModel
import uk.co.imoqen.nourish.recipebook.RecipeBookScreen
import uk.co.imoqen.nourish.recipebook.RecipeBookViewModel
import uk.co.imoqen.nourish.recipebook.RecipeCardScreen
import uk.co.imoqen.nourish.recipecreation.RecipeCreationScreen
import uk.co.imoqen.nourish.recipecreation.RecipeCreationViewModel
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryViewModel
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryScreen
import uk.co.imoqen.nourish.shoppinglist.ShoppingListScreen
import uk.co.imoqen.nourish.shoppinglist.ShoppingListViewModel


/**
 * Navigation host, enabling navigation across the application and functionality of the Android
 * back button.
 * Based on https://developer.android.com/guide/navigation/design#compose
 * @param navController - the navController used to control the navigation
 * @param startDestination - default destination to navigate to
 * @param modifier - the modifier for the navigation Composable
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // Bottom Navbar: Tab destinations
        composable(TabDestination.RECIPE_FINDER.route) {
            // View models are defined in their respective routes:
            val recipeDiscoveryViewModel: RecipeDiscoveryViewModel = hiltViewModel()
            // The composable to display for this tab:
            RecipeDiscoveryScreen(recipeDiscoveryViewModel)
        }
        composable(TabDestination.RECIPE_BOOK.route) {
            val recipeBookViewModel: RecipeBookViewModel = hiltViewModel()
            RecipeBookScreen(
                recipeBookViewModel,
                onViewRecipeCard = { id -> navController.navigate("b/recipe-card/$id") },
                onAddRecipe = { navController.navigate("b/add-recipe") }
            )
        }
        composable(TabDestination.SHOPPING_LIST.route) {
            val shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
            ShoppingListScreen(shoppingListViewModel)
        }
        composable(TabDestination.PROFILE.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(profileViewModel)
        }

        // within the Recipe Book, can navigate to a specific Recipe Card:
        composable(
            route = "b/recipe-card/{id}",
            // the recipeId to display the recipe card of:
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("id")!!
            val recipeBookViewModel: RecipeBookViewModel = hiltViewModel()
            val recipeBookUiState = recipeBookViewModel.uiState.collectAsState().value
            RecipeCardScreen(
                recipeBookViewModel,
                recipeBookUiState,
                recipeId,
                onBack={ navController.popBackStack() },
                onEditRecipe = { id -> navController.navigate("b/edit-recipe/$id") }
            )
        }
        // Within the Recipe Book, can add a new Recipe Card:
        composable(
            route = "b/add-recipe",
        ) { backStackEntry ->
            val recipeCreationViewModel: RecipeCreationViewModel = hiltViewModel()
            RecipeCreationScreen(recipeCreationViewModel, onBack={ navController.popBackStack() })
        }
        // Edit recipe card:
        composable(
            route = "b/edit-recipe/{id}",
            // the recipeId to edit:
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("id")!!
            // Uses recipe creation, but just parses the recipe to be displayed within it.
            // then user can make changes to it.
            val recipeCreationViewModel: RecipeCreationViewModel = hiltViewModel()
            if (recipeCreationViewModel.uiState.collectAsState().value.recipeEditId != recipeId) {
                // If the recipe to edit doesn't match the current recipe being edited, update it:
                recipeCreationViewModel.populateFromId(recipeId)
            }
            // go to the new recipe, and remove the edit from the stack as if it was never there.
            // So user cant go back to back. and single top to ensure that the new recipe
            // replaces the old one
            RecipeCreationScreen(
                recipeCreationViewModel,
                onBack={ navController.popBackStack() },
                onFinishEdit={ navController.navigate("b/recipe-card/$recipeId") {
                    popUpTo("b/edit-recipe/$recipeId") {
                        inclusive = true
                    };
                    launchSingleTop = true }
                }
            )
        }
    }
}

/**
 * The navigation bar displayed at the bottom of the screen for the user.
 * @param navigationItems - the items contained within the navbar
 * @param selectedDestinationTab - the currently selected tab within the navbar
 * @param navController - the navigation controller
 */
@Composable
fun NourishNavigationBar(
    navigationItems: List<TabDestination>,
    selectedDestinationTab: Char,
    navController: NavController
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        navigationItems.forEachIndexed { index, destination ->
            NavigationBarItem(
                // If the selected destination is equal to the destination identified, highlight that
                // as being selected.
                selected = selectedDestinationTab == destination.route[0],
                onClick = {
                    // Update the currently selected item in navigation
                    navController.navigate(route = destination.route) {
                        launchSingleTop = true // ensures destinations are not duplicated
                        // The following lines are to prevent previous destinations from being
                        // destroyed and recreated,
                        // from Android docs https://developer.android.com/guide/navigation/backstack/multi-back-stacks
                        restoreState = true // to reset view models as user has changed
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
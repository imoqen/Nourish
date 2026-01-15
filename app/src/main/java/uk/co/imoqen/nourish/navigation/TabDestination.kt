package uk.co.imoqen.nourish.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Destination for the navigation bar: Tab Destinations
 * @param route - the route to navigate to in the NavigationController
 *                 Routes are prepended with x/ to denote the tab they belong to, useful in RouteDestinations
 *                 to determine which tab to display as currently selected in the top/bottom navbars.
 *                 Bottom navbar: f = RecipeFinder tab, b = RecipeBook tab, l = ShoppingList tab.
 *                 Top navbar: p = profile, s = settings
 * @param title - the title displayed underneath the icon for this navigation element
 * @param icon - the icon displayed for this navigation element
 * @param iconContentDescription - describes the icon content, for screen readers
 */
enum class TabDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconContentDescription: String
) {
    RECIPE_FINDER("f/recipe-finder", "Recipe Finder", Icons.Filled.Create, "Go to find a recipe"),
    RECIPE_BOOK("b/recipe-book","Recipe Book", Icons.Filled.FavoriteBorder, "Go to view recipes"),
    SHOPPING_LIST("l/shopping-list", "Shopping List", Icons.AutoMirrored.Filled.List, "Go to shopping list"),
    PROFILE("p/profile", "Profile", Icons.Filled.Person, "Go to profile"),
    SETTINGS("s/settings", "Settings", Icons.Filled.Settings, "Go to settings")

}
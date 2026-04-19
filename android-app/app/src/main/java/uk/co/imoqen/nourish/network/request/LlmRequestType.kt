package uk.co.imoqen.nourish.network.request

/**
 * Enum outlining generations that an LLM can perform, to generate a recipe. Used in RecipeDiscovery.
 *
 * @author Imogen Pemberton, 2026
 */
enum class LlmRequestType {
    // Generate recipe:
    RECIPE,

    // Generate dishes:
    DISHES_FROM_INGREDIENTS,
    DISHES_FROM_IMAGE,
    DISHES_FROM_INGREDIENTS_IMAGE,

    // To generate a recipe after a dish has been chosen and if an image is included.
    // otherwise, the recipe can simply be generated from the "recipe" method,
    // as only strings are involved.
    RECIPE_FROM_IMAGE_AND_DISH,
}
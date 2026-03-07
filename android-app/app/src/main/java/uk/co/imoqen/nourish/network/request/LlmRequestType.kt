package uk.co.imoqen.nourish.network.request

// Actions that an LLM can doto generate a recipe. From the recipe discovery screen.
enum class LlmRequestType {
    // generate recipe:
    RECIPE,

    // generate dishes:
    DISHES_FROM_INGREDIENTS,
    DISHES_FROM_IMAGE,
    DISHES_FROM_INGREDIENTS_IMAGE,

    // to generate a recipe after a dish has been chosen and if an image is included.
    // otherwise, the recipe can simply be generated from the "recipe" method, as only strings are involved.
    RECIPE_FROM_IMAGE_AND_DISH,
}
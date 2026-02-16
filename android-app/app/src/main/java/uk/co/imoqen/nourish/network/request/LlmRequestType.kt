package uk.co.imoqen.nourish.network.request

enum class LlmRequestType {
    RECIPE,
    DISHES_FROM_INGREDIENTS,
    DISHES_FROM_IMAGE,
    INGREDIENTS_FROM_IMAGE,
    RECIPE_QUERY,
    RECIPE_ALTERATION
}
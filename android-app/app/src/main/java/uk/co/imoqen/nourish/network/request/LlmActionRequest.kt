package uk.co.imoqen.nourish.network.request

import uk.co.imoqen.nourish.network.response.LlmRecipe

data class LlmActionRequest (
    val recipe: LlmRecipe,
    val query: String // will be either: question, alteration, "generate a new recipe such that ...", or "generate nutrition" for the corresponding recipe.
)
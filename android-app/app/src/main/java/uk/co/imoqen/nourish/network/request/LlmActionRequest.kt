package uk.co.imoqen.nourish.network.request

import uk.co.imoqen.nourish.network.response.LlmRecipe

/**
 * Data class for the Llm Action Request; used in the RecipeBook to perform an action on a recipe
 * (alteration, nutrition generation, or question).
 *
 * @author Imogen Pemberton, 2026
 */
data class LlmActionRequest (
    val recipe: LlmRecipe,

    // will be either: question, alteration, "generate a new recipe such that ...",
    // or nutrition generation for the corresponding recipe:
    val query: String,

    // optional, only for recipe alteration:
    val allNutritionalFilters: String,

    // optional, only for recipe alteration:
    val dishType: String
)
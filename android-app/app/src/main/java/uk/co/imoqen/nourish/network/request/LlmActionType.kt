package uk.co.imoqen.nourish.network.request

// Actions that an LLM can do before the recipe is generated. from the recipe book view.
enum class LlmActionType {
    QUERY_RECIPE,
    ALTER_RECIPE,
    GENERATE_NUTRITION
}
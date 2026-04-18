package uk.co.imoqen.nourish.common.exception

/**
 * Exception for Recipe Creation, thrown when the created recipe is invalid. For example, doesn't
 * contain any ingredients.
 */
class RecipeCreationException(message: String) : Exception(message)
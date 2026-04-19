package uk.co.imoqen.nourish.common.exception

/**
 * Exception for Recipe Creation, thrown when the created recipe is invalid. For example, doesn't
 * contain any ingredients.
 *
 * @author Imogen Pemberton, 2026
 */
class RecipeCreationException(message: String) : Exception(message)
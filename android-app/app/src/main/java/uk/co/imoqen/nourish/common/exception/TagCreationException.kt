package uk.co.imoqen.nourish.common.exception

/**
 * Exception for tag creation, thrown when the tag to create is invalid. For example, doesn't have
 * a colour.
 *
 * @author Imogen Pemberton, 2026
 */
class TagCreationException(message: String) : Exception(message)
package uk.co.imoqen.nourish.common.exception

/**
 * Exception for tag creation, thrown when the tag to create is invalid. For example, doesn't have
 * a colour.
 */
class TagCreationException(message: String) : Exception(message)
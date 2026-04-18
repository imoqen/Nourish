package uk.co.imoqen.nourish.common

import kotlinx.coroutines.flow.first
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection
import uk.co.imoqen.nourish.common.exception.TagCreationException
import javax.inject.Inject
import kotlin.collections.map
import kotlin.math.round

/**
 * The RecipeActions class, a class provided for reusability. Provides recipe-related actions
 * which can be called from other ViewModels.
 * @param uiEventBus - The UiEventBus, to enqueue and display Snackbar alerts / messages.
 * @param authenticationManager - The authentication manager, for user authentication.
 *                                Required to access the uid of the currently logged in user.
 * @param tagRepository - The repository providing access to CRUD operations on the Tag database table.
 * @param recipeRepository - The repository providing access to CRUD operations on the Recipe database table.
 */
class RecipeActions @Inject constructor(
    private val uiEventBus: UiEventBus,
    private val authenticationManager: AuthenticationManager,
    private val tagRepository: TagRepository,
    private val recipeRepository: RecipeRepository
){

    /**
     * Creates a tag given user input and saves it to the database.
     * @param description - A description of the tag.
     * @param colour - The selected colour of the tag.
     * @param allRecipeTags - All recipe tags and whether they are currently selected, to add the
     *                        new tag to.
     * @throws TagCreationException if there was an error creating the tag.
     */
    suspend fun createTag(
        description: String,
        colour: NourishColour?,
        allRecipeTags: List<TagSelection>?
    ) {
        runCatching {
            // Error handling:
            if (description.trim() == "") {
                throw TagCreationException("Please enter a description.")
            }
            if (description.length > 27) {
                throw TagCreationException(
                    "The description is too long. It must be less than 27 characters."
                )
            }
            if (colour == null) {
                throw TagCreationException("Please select a colour.")
            }
            // Check that tag doesn't already exist
            if (allRecipeTags != null && allRecipeTags.map{
                    it.tag.identifier.uppercase()
                }.contains(description.uppercase().replace(" ", "-"))) {
                throw TagCreationException(
                    "Please ensure that the tag description is unique; it cannot be the same as another tag."
                )
            }
            val uid = authenticationManager.getUid()
            if (uid == "") {
                throw NullPointerException()
            }
            // Valid; create the tag
            tagRepository.insert(Tag(
                identifier = description.uppercase().replace(" ", "-"),
                displayName = description,
                colour = colour.name,
                authorId = uid
            ))
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Tag could not be created."
                ))
                // Then throw the tag creation exception to handle in ViewModel,
                // and output to the user.
                throw e
            }
    }

    /**
     * Returns all tags authored by a given user.
     * @param previousTags - The tags and their current selection before all tags are reobtained,
     *                       to ensure they are updated correctly.
     * @param reset - Boolean reflecting whether the tags should be reset, so all deleted.
     * @return The updated list of tags authored by a given user, or null if an error occurred.
     */
    suspend fun getAllTagsForUser(
        previousTags: List<TagSelection>?,
        reset: Boolean = false
    ): List<TagSelection>? {
        runCatching {
            val uid = authenticationManager.getUid()
            if (uid == "") {
                throw NullPointerException()
            }
            // Only update if an update has been made to the list of tags,
            // or no tags have been obtained yet.
            val allTagsUpdated = tagRepository.getAllForUser(uid).first()
            if (previousTags == null || previousTags.size != allTagsUpdated.size || reset) {
                return (
                        (tagRepository.getAllForUser(uid).first())
                            .map { tag ->
                                val oldTag = previousTags?.find { it.tag == tag }
                                val selected = if (!reset) {
                                    (oldTag?.selected) ?: false
                                } else {
                                    false
                                }
                                // So that old tags preserve their selected values
                                TagSelection(tag, selected)
                            }
                        )
            }
        }
        return null // If failure, return null.
    }

    /**
     * Returns all recipe types. Not per user, because every user has the same recipe types and
     * these cannot be edited. This is used when multiple recipe types can be selected, i.e.
     * in the RecipeBook.
     * @param previousTypes - The types and their current selection before all types are reobtained,
     *                        to ensure they are updated correctly.
     * @returns The updated list of types, or null if an error occurred.
     */
    suspend fun getAllRecipeTypesSelectionList(
        previousTypes: List<TypeSelection>?
    ): List<TypeSelection>? {
        runCatching {
            val updatedTypes = recipeRepository.getAllRecipeTypes().first()
            // Only set if not already set or value updated:
            if (previousTypes == null || updatedTypes.size != previousTypes.size) {
                return (recipeRepository.getAllRecipeTypes().first().map { type ->
                    TypeSelection(
                        type,
                        false
                    ) // Set all recipe types to not be selected currently
                })
            }
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't fetch recipe types. Please try again later."
                ))
            }
        return null // Failure occurred
    }

    /**
     * Returns all recipe types. Not per user, because every user has the same recipe types and
     * these cannot be edited. This is used when only ONE recipe type can be selected, i.e.
     * mutual exclusive selection in the RecipeDiscovery and RecipeCreation.
     * @returns The updated list of types, or null if an error occurred.
     */
    suspend fun getAllRecipeTypes(): List<RecipeType>? {
        runCatching {
            return recipeRepository.getAllRecipeTypes().first()
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't fetch recipe types. Please try again later."
                ))
            }
        return null // Failure occurred
    }


    /**
     * Updates a Tag to show as selected when pressed. Overloaded.
     * @param tag - The tag to update
     * @param status - Whether the tag is selected or not.
     * @param allTags - A list of all tags and, for each, whether they are currently selected.
     * @returns Updated list of all tags.
     * @throws NullPointerException if there are no tags, therefore nothing can be updated.
     */
    suspend fun updateSelected(
        tag: Tag,
        status: Boolean,
        allTags: List<TagSelection>?
    ): List<TagSelection>? {
        runCatching {
            if (allTags == null) {
                throw NullPointerException()
            }
            return (allTags.map { tagSelection ->
                if (tag == tagSelection.tag) {
                    tagSelection.copy(selected = status)
                } else {
                    tagSelection
                }
            })
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "The tag could not be selected. Please try again later."
                ))
            }
        return null
    }

    /**
     * Updates a Type to show as selected when pressed. Overloaded.
     * @param type - The recipe type to update
     * @param status - Whether the recipe type is selected or not.
     * @param recipeTypes - A list of all TYPES and, for each, whether they are currently selected.
     * @returns Updated list of all RECIPE TYPES.
     * @throws NullPointerException if there are no TYPES, therefore nothing can be updated.
     */
    suspend fun updateSelected(
        type: RecipeType,
        status: Boolean,
        recipeTypes: List<TypeSelection>?
    ): List<TypeSelection>? {
        runCatching {
            if (recipeTypes == null) {
                throw NullPointerException()
            }
            return (recipeTypes.map { recipeSelection ->
                if (type == recipeSelection.type) {
                    recipeSelection.copy(selected = status)
                } else {
                    recipeSelection
                }
            })
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "The type could not be selected. Please try again later."
                ))
            }
        return null
    }

    /**
     * Deletes a Tag.
     * @param tag - The tag to delete.
     */
    suspend fun deleteTag(
        tag: Tag?
    ) {
        runCatching {
            if (tag == null) {
                uiEventBus.enqueueEvent(
                    UiEvent.ShowSnackbar("Tag could not be deleted.")
                )
            } else {
                tagRepository.delete(tag)
            }
        }
    }

    /**
     * Determined the plurality of a unit, i.e. whether it should be displayed in singular or plural.
     * The result is 1.0 if the value rounds to 1.0 between 0.25 intervals.
     * e.g. 1.2 would round to 1.25, so != 1.0 but 1.1 would round to 1.0 so == 1.0.
     * If only 1, the unit should always be singular. And we don't care about the kg or L instance here,
     * because the unit doesn't change pluralities for kg or l.
     * @param ingredientAmount - The amount of the ingredient to determine unit plurality of.
     * @returns Boolean reflecting whether to use singular (false) or to use plural (true).
     */
    fun determineUnitPlurality(
        ingredientAmount: Double,
    ): Boolean {
        return round(ingredientAmount*4)/4 != 1.0
    }

    /**
     * Determined the plurality of an ingredient, i.e. whether it should be displayed in singular or plural.
     * If the amount is 1.25 or less, then it just appears as 1.0 (or less) which should always be singular.
     * e.g. 1 strawberry = singular
     * but 1 cup strawberry = plural; 1 cup strawberries. Thus, it depends on both the amount and unit.
     * @param ingredientAmount - The amount of the ingredient to determine ingredient plurality of.
     * @param ingredientUnit - The unit of the ingredient to determine the ingredient plurality of.
     * @returns Boolean reflecting whether to use singular (false) or to use plural (true).
     */
    fun determineIngredientPlurality(
        ingredientAmount: Double,
        ingredientUnit: IngredientUnit
    ): Boolean {
        if ("%.2f".format(round(ingredientAmount*4)/4).toDouble() <= 1.0) {
            if (ingredientUnit.useSingularForOne) {
                return false // Use singular
            }
        }
        return true
    }

    /**
     * Formats the ingredient amount, to display in the UI.
     * This shaves off the decimal if it is .0, otherwise keeps it.
     * e.g. 5.0 -> 5 but 5.5 remains -> 5.5
     * @param ingredientAmount - the amount of the ingredient to format.
     * @param roundToQuarter - Whether it should be rounded to a quarter or not. It should only not
     *                         be rounded to a quarter (thus be precise) if KG or L.
     * @returns The formatted ingredient amount string.
     */
    fun formatIngredientAmount(
        ingredientAmount: Double,
        roundToQuarter: Boolean = true
    ): String {
        val amount = if (roundToQuarter) {
            "%.2f".format(round(ingredientAmount*4)/4).toDouble()
        }
        else {
            "%.2f".format(ingredientAmount).toDouble()
        }
        if (amount % 1.0 == 0.0) {
            return amount.toInt().toString()
        }
        return amount.toString()
    }

    /**
     * Formats the ingredient text, used for displaying LLM ingredients as they are handled differently.
     * @param ingredient - The LlmIngredient to format.
     * @returns String of the formatted ingredient text.
     */
    fun formatIngredientText(
        ingredient: LlmIngredient
    ): String {
        runCatching {
            val amount = ingredient.ingredientAmount

            val ingredientUnit = IngredientUnit.valueOf(ingredient.ingredientAmountUnit)

            val unit = if (determineUnitPlurality(amount)) {
                ingredientUnit.pluralName
            } else {
                ingredientUnit.singularName
            }

            val ingredientPluralisedName =
                if (determineIngredientPlurality(
                        ingredient.ingredientAmount, ingredientUnit)
                    ) {
                    ingredient.ingredientPlural
                } else {
                    ingredient.ingredientSingular
                }

            return if (unit != "") {
                ("${formatIngredientAmount(amount)} $unit $ingredientPluralisedName")
            } else {
                ("${formatIngredientAmount(amount)} $ingredientPluralisedName")
            }
        }
        return "" // invalid if reached here, so return empty string.
    }

    /**
     * Finds the corresponding recipe type in the database, given a recipe type's ID.
     * @param recipeTypeId - the ID of the recipe type to find.
     * @returns Pair of the recipe type's title and order.
     */
    suspend fun findRecipeType(
        recipeTypeId: Long
    ): Pair<String, Int>? {
        val recipeType = recipeRepository.getRecipeTypeById(recipeTypeId).first()
        if (recipeType == null) {
            return recipeType
        }
        return Pair(recipeType.title, recipeType.ordinal)
    }

}
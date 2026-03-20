package uk.co.imoqen.nourish.common

import android.util.Log
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
import uk.co.imoqen.nourish.common.tag.TagCreationException
import javax.inject.Inject
import kotlin.collections.map
import kotlin.math.round

class RecipeActions @Inject constructor(
    private val uiEventBus: UiEventBus,
    private val authenticationManager: AuthenticationManager,
    private val tagRepository: TagRepository,
    private val recipeRepository: RecipeRepository
){

    suspend fun createTag(
        description: String,
        colour: NourishColour?,
        allRecipeTags: List<TagSelection>?
    ) {
        runCatching {

            if (description == "") {
                throw TagCreationException("Please enter a description.")
            }
            if (description.length > 27) {
                throw TagCreationException("The description is too long. It must be less than 27 characters.")
            }
            if (colour == null) {
                throw TagCreationException("Please select a colour.")
            }
            // check that tag doesnt already exist
            if (allRecipeTags != null && allRecipeTags.map{
                    it.tag.identifier.uppercase()
                }.contains(description.uppercase().replace(" ", "-"))) {
                throw TagCreationException("Please ensure that the tag description is unique; it cannot be the same as another tag.")
            }

            val uid = authenticationManager.getUid()
            if (uid == null) {
                throw NullPointerException()
            }
            // create the tag
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
                // then throw the tag creation exception to be handled in the viewmodel, and output to the user.
                throw e
            }
    }

    // gets all tags
    suspend fun getAllTagsForUser(
        previousTags: List<TagSelection>?,
        reset: Boolean = false // to reset them to all be not selected
    ): List<TagSelection>? {
        runCatching {
            Log.i("setting","setting all tags")
            val uid = authenticationManager.getUid()
            if (uid == null) {
                throw NullPointerException()
            }
            // only update if an update has been made to the list of tags, or no tags have been obtained yet:
            val allTagsUpdated = tagRepository.getAllForUser(uid).first()
            if (previousTags == null || previousTags.size != allTagsUpdated.size || reset) {
                return (
                        (tagRepository.getAllForUser(uid).first())
                            .map { tag ->
                                val oldTag = previousTags?.find { it.tag == tag }
                                val selected = if (!reset) {
                                    Log.i("selected", "${oldTag?.selected}")
                                    (oldTag?.selected) ?: false
                                } else {
                                    Log.i("selected", "RESETTING TO FALSE")
                                    false
                                }
                                TagSelection(tag, selected) // so that old tags preserve their selected values
                            }
                        )
            }
        }
            .onFailure { e ->
                Log.i("tag obtaining", "failed: $e")
            }
        return null // if failure, return null.
    }

    // not "ForUser" because recipe types are the same for all users.
    // for when MULTIPLE recipe types can be selected
    suspend fun getAllRecipeTypesSelectionList(
        previousTypes: List<TypeSelection>?
    ): List<TypeSelection>? {
        runCatching {
            val updatedTypes = recipeRepository.getAllRecipeTypes().first()
            // only set if not already set or value updated:
            if (previousTypes == null || updatedTypes.size != previousTypes.size) {
                Log.i("get types", "not null")
                return (recipeRepository.getAllRecipeTypes().first().map { type ->
                    TypeSelection(
                        type,
                        false
                    ) // set all recipe types to not be selected currently
                })
            }
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't fetch recipe types. Please try again later."
                ))
                Log.i("recipe types", "failed: $e")
            }
        Log.i("get types", "null")
        return null // failure occurred
    }

    // for when only one type can be selected
    suspend fun getAllRecipeTypes(): List<RecipeType>? {
        runCatching {
            return recipeRepository.getAllRecipeTypes().first()
        }
            .onFailure { e ->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't fetch recipe types. Please try again later."
                ))
                Log.i("recipe types", "failed: $e")
            }
        Log.i("get types", "null")
        return null // failure occurred
    }


    // update the tag to show as selected when pressed.
    suspend fun updateSelected(
        tag: Tag,
        status: Boolean,
        allTags: List<TagSelection>?
    ): List<TagSelection>? {
        runCatching {
            Log.i("updating","updating tag: ${tag.displayName}")
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
                Log.i("tag select", "failed: $e")
            }
        return null
    }

    // update the type to show as selected when pressed.
    // OVERLOADED method to support the RecipeType param instead.
    suspend fun updateSelected(
        type: RecipeType,
        status: Boolean,
        recipeTypes: List<TypeSelection>?
    ): List<TypeSelection>? {
        runCatching {
            Log.i("updating","updating type: ${type.title}")
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
                Log.i("type select", "failed: $e")
            }
        return null
    }

    suspend fun deleteTag(
        tag: Tag?
    ) {
        runCatching {
            if (tag == null) {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Tag could not be deleted."))
            } else {
                tagRepository.delete(tag)
            }
        }
    }

    fun determineUnitPlurality(
        ingredientAmount: Double,
    ): Boolean {
        Log.i("ingredient amount:", "$ingredientAmount , rounded: ${round(ingredientAmount*4.0)/4.0} and is not equal to 1.0: ${round(ingredientAmount*4.0)/4.0 != 1.0 }")
        // plural form should be used in most cases; just a few cases where it shouldn't.
        return round(ingredientAmount*4)/4 != 1.0 // if only 1, should always be singular. and dont care about the kg or L  instance here, because the unit doesnt change pluralities for kg or l.
    }

    fun determineIngredientPlurality(
        ingredientAmount: Double,
        ingredientUnit: IngredientUnit
    ): Boolean {
        if ("%.2f".format(round(ingredientAmount*4)/4).toDouble() <= 1.0) { // because if 1.25 or less then it just appears as 1.0 (or less) which should always be singular
            // e.g. 1 strawberry = singular
            // but 1 cup strawberry should be plural: 1 cup strawberries
            if (ingredientUnit.useSingularForOne) {
                return false // use singular
            }
        }
        return true
    }

    // shaves off the decimal if it is .0, otherwise keeps it.
    // e.g. 5.0 -> 5 but 5.5 remains -> 5.5
    fun formatIngredientAmount(
        ingredientAmount: Double,
        roundToQuarter: Boolean = true // true by default, only false for kg / l as they need to be more precise.
    ): String {
        // formats the amount such that it is to 2 d.p. and always rounded to the nearest 0.25

        val amount = if (roundToQuarter) { "%.2f".format(round(ingredientAmount*4)/4).toDouble() }
        else {
            "%.2f".format(ingredientAmount).toDouble()
        }
        if (amount % 1.0 == 0.0) {
            return amount.toInt().toString()
        }
        return amount.toString()
    }



    // only used for displaying llm ingredient as they are handled differently
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
                if (determineIngredientPlurality(ingredient.ingredientAmount, ingredientUnit)) {
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
        return "" // invalid if reached here
    }

}
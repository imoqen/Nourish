package uk.co.imoqen.nourish.common

import android.util.Log
import kotlinx.coroutines.flow.first
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection
import javax.inject.Inject
import kotlin.collections.map

class FilterActions @Inject constructor(
    private val uiEventBus: UiEventBus,
    private val authenticationManager: AuthenticationManager,
    private val tagRepository: TagRepository,
    private val recipeRepository: RecipeRepository
){

    suspend fun createTag(
        description: String,
        colour: NourishColour
    ) {
        runCatching {
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
                Log.i("tag creation", "failed: $e")
            }
    }

    // gets all tags
    suspend fun getAllTagsForUser(
        previousTags: List<TagSelection>?
    ): List<TagSelection>? {
        runCatching {
            Log.i("setting","setting all tags")
            val uid = authenticationManager.getUid()
            if (uid == null) {
                throw NullPointerException()
            }
            // only update if an update has been made to the list of tags, or no tags have been obtained yet:
            val allTagsUpdated = tagRepository.getAllForUser(uid).first()
            if (previousTags == null || previousTags.size != allTagsUpdated.size) {
                return (
                        (tagRepository.getAllForUser(uid).first())
                            .map { tag ->
                                TagSelection(tag, false) // set all to not be checked initially
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
    suspend fun getAllRecipeTypes(
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

}
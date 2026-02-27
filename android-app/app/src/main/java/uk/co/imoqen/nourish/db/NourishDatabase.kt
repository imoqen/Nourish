package uk.co.imoqen.nourish.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientDao
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeDao
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredientDao
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSectionDao
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStepDao
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTagDao
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeTypeDao
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagDao
import uk.co.imoqen.nourish.db.user.User
import uk.co.imoqen.nourish.db.user.UserDao

@Database(
    entities = [
        // classes relating to Recipe:
        Recipe::class, RecipeIngredient::class, RecipeType::class, RecipeTag::class, RecipeIngredientSection::class, RecipeStep::class,
        // other classes:
        User::class, Ingredient::class, Tag::class],
    version = 9
)
@TypeConverters(Converters::class)
abstract class NourishDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeTypeDao(): RecipeTypeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun recipeIngredientSectionDao(): RecipeIngredientSectionDao
    abstract fun recipeStepDao(): RecipeStepDao
    abstract fun recipeTagDao(): RecipeTagDao
    abstract fun tagDao(): TagDao
}
package uk.co.imoqen.nourish.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientDao
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeDao
import uk.co.imoqen.nourish.db.recipe.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.RecipeType
import uk.co.imoqen.nourish.db.user.User
import uk.co.imoqen.nourish.db.user.UserDao

@Database(
    entities = [Recipe::class, RecipeIngredient::class, RecipeType::class, User::class, Ingredient::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class NourishDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
    abstract fun ingredientDao(): IngredientDao

}
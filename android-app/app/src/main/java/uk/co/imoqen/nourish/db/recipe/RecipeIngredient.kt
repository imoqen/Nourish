package uk.co.imoqen.nourish.db.recipe

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

// junction table to relate ingredients to recipes
// store for 1 serving size then can multiply accordingly
@Entity(tableName = "RecipeIngredient")
data class RecipeIngredient(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "recipeId") val recipeId: Int, // foreign key for recipe relation
    @ColumnInfo(name = "ingredientId") val ingredientId: Int, // foreign key for ingredient relation
    @ColumnInfo(name = "ingredientUnit") val ingredientUnit: IngredientUnit, // IngredientUnit is an enum and gets converted using converters class
    @ColumnInfo(name = "amount") val amount: Int, // foreign key for ingredient relation
)

data class IngredientsForRecipeIngredient(
    @Embedded val recipeIngredient: RecipeIngredient, // parent
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val collections: List<Ingredient>, // entity
)
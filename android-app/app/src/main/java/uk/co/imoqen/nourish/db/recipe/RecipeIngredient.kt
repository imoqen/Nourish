package uk.co.imoqen.nourish.db.recipe

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

// junction table to relate ingredients to recipes
// store for 1 serving size then can multiply accordingly
@Entity(
    tableName = "RecipeIngredient",
    foreignKeys = [
        // recipe relation:
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"], // the primary key of Recipe
            childColumns = ["recipeId"], // the foreign key which holds this reference, in RecipeIngredient
            onDelete = ForeignKey.CASCADE
        ),
        // ingredient relation:
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"], // the primary key of Recipe
            childColumns = ["ingredientId"], // the foreign key which holds this reference, in RecipeIngredient
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "recipeId") val recipeId: Long, // foreign key for recipe relation
    @ColumnInfo(name = "ingredientId") val ingredientId: Long, // foreign key for ingredient relation
    @ColumnInfo(name = "ingredientUnit") val ingredientUnit: IngredientUnit, // IngredientUnit is an enum and gets converted using converters class
    @ColumnInfo(name = "amount") val amount: Int, // foreign key for ingredient relation
)

// defines how to join the two objects:

// recipe ingredient to ingredient relation
data class IngredientWithRecipeIngredients(
    @Embedded val ingredient: Ingredient, // parent
    @Relation(
        parentColumn = "id", // the primary key of the parent (Ingredient)
        entityColumn = "ingredientId" // the foreign key in RecipeIngredient referencing it
    )
    val collections: List<RecipeIngredient>, // entity
)

// recipe ingredient to recipe relation
data class RecipeWithIngredientss(
    @Embedded val recipe: Recipe, // parent
    @Relation(
        parentColumn = "id", // the primary key of the parent (Recipe)
        entityColumn = "recipeId" // the foreign key in RecipeIngredient referencing it
    )
    val collections: List<Ingredient>, // entity
)
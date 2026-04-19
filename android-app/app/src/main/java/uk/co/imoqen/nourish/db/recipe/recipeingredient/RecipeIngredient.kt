package uk.co.imoqen.nourish.db.recipe.recipeingredient

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection

/**
 * The RecipeIngredient table in the Room database; this is a junction table to relate ingredients
 * to recipes.
 * Amounts are stored for 1 serving size and scaled accordingly in the UI.
 *
 * @author Imogen Pemberton, 2026
 */
@Entity(
    tableName = "RecipeIngredient",
    foreignKeys = [
        // Recipe relation:
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        // Ingredient relation:
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        ),
        // IngredientSection relation:
        ForeignKey(
            entity = RecipeIngredientSection::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("recipeId"),
        Index("ingredientId"),
        Index("sectionId"),
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipeId") val recipeId: Long, // Foreign key for Recipe relation
    @ColumnInfo(name = "ingredientId") val ingredientId: Long, // Foreign key for Ingredient relation
    // IngredientUnit is an enum and gets converted using converters class
    @ColumnInfo(name = "ingredientUnit") val ingredientUnit: IngredientUnit,
    @ColumnInfo(name = "amount") val amount: Double, // Amount of ingredient for 1 serving size
    @ColumnInfo(name = "sectionId") val sectionId: Long // The section of this ingredient (e.g. "for the pastry ...")
)

// defines how to join the Recipe and its RecipeIngredients
data class RecipeWithRecipeIngredients(
    @Embedded val recipe: Recipe, // parent
    @Relation(
        parentColumn = "id", // the primary key of the parent (Recipe)
        entityColumn = "recipeId" // the foreign key in RecipeIngredient referencing it
    )
    val ingredients: List<RecipeIngredient>,
)
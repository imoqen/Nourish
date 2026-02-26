package uk.co.imoqen.nourish.db.recipe.recipestep

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.recipe.Recipe

@Entity(
    tableName = "RecipeStep",
    foreignKeys = [
        // recipe relation:
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"], // the primary key of Recipe
            childColumns = ["recipeId"], // the foreign key which holds this reference, in RecipeStep
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("recipeId"),
    ]
)
data class RecipeStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipeId") val recipeId: Long, // foreign key for recipe relation
    @ColumnInfo(name = "index") val index: Int, // to order the steps (0 - ...)
    @ColumnInfo(name = "instruction") val instruction: String, // the actual instruction of the step
    @ColumnInfo(name = "complete") val complete: Boolean // for allowing user to cross out step to mark it as done
)
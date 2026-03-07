package uk.co.imoqen.nourish.db.recipe.recipenutrition

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.recipe.Recipe

@Entity(
    tableName = "RecipeNutrition",
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
data class RecipeNutrition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipeId") val recipeId: Long, // foreign key for recipe relation
    @ColumnInfo(name = "servings") val servings: Int,
    @ColumnInfo(name = "calories") val calories: Int,
    @ColumnInfo(name = "fat") val fat: Double,
    @ColumnInfo(name = "saturates") val saturates: Double,
    @ColumnInfo(name = "carbohydrate") val carbohydrate: Double,
    @ColumnInfo(name = "sugars") val sugars: Double,
    @ColumnInfo(name = "protein") val protein: Double,
    @ColumnInfo(name = "salt") val salt: Double,
)
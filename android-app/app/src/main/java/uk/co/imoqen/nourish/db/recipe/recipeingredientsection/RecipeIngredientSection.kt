package uk.co.imoqen.nourish.db.recipe.recipeingredientsection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The RecipeIngredientSection table in the Room database.
 */
@Entity(
    tableName = "RecipeIngredientSection"
)
data class RecipeIngredientSection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String, // Title of the section e.g. "for the pastry"
)
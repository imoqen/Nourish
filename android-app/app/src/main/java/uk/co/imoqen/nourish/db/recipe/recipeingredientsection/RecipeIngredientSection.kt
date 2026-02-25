package uk.co.imoqen.nourish.db.recipe.recipeingredientsection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "RecipeIngredientSection"
)
data class RecipeIngredientSection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String, // title of the section e.g. "for the pastry"
)
package uk.co.imoqen.nourish.db.recipe.recipetype

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipeType",
    indices = [Index(value = ["title"], unique = true)] // to use the conflict strategy if recipetype with this name already exists
)
data class RecipeType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "ordinal") val ordinal: Int // for ordering
)
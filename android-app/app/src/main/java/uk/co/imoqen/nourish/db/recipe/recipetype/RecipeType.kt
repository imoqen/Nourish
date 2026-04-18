package uk.co.imoqen.nourish.db.recipe.recipetype

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The RecipeType table in the Room database.
 * This is pre-populated upon login on a device.
 */

@Entity(
    tableName = "recipeType",
    // Uses the conflict strategy if a RecipeType with this name already exists
    indices = [Index(value = ["title"], unique = true)]
)
data class RecipeType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "ordinal") val ordinal: Int // there is a specific order to recipe types.
)
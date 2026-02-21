package uk.co.imoqen.nourish.db.recipe

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// breakfast, lunch, dinner, snack, then users can add more and these will be sent to llm for classification.
// need to initialise the app by making these and mnot allow them to be deleted. only user made ones can be deleted.

@Entity(
    tableName = "recipeType",
    indices = [Index(value = ["title"], unique = true)] // to use the conflict strategy if recipetype with this name already exists
)
data class RecipeType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String
)
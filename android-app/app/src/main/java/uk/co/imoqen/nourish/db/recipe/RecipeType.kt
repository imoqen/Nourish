package uk.co.imoqen.nourish.db.recipe

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

// breakfast, lunch, dinner, snack, then users can add more and these will be sent to llm for classification.
// need to initialise the app by making these and mnot allow them to be deleted. only user made ones can be deleted.

@Entity
data class RecipeType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipeTypeId") val recipeTypeId: Int,
    @ColumnInfo(name = "title") val title: String
)

data class RecipesOfType(
    @Embedded val recipeType: RecipeType, // parent
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeTypeId"
    )
    val recipesOfType: List<Recipe>, // entity
)
package uk.co.imoqen.nourish.db.recipe.recipequery

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.recipe.Recipe

@Entity(
    tableName = "RecipeQuery",
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
data class RecipeQuery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipeId") val recipeId: Long, // foreign key for recipe relation
    @ColumnInfo(name = "userQuestion") val userQuestion: String, // the question sent by the user
    @ColumnInfo(name = "llmAnswer") val llmAnswer: String, // the answer. can be "" until llm responds.
)
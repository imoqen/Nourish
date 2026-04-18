package uk.co.imoqen.nourish.db.recipe.recipequery

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.recipe.Recipe

/**
 * The RecipeQuery table in the Room database.
 */
@Entity(
    tableName = "RecipeQuery",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
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
    @ColumnInfo(name = "llmAnswer") val llmAnswer: String, // the answer. Can be "" until llm responds.
)
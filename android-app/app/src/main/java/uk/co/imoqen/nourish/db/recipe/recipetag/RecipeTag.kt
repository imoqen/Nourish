package uk.co.imoqen.nourish.db.recipe.recipetag

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.tag.Tag

/**
 * The RecipeTag table in the Room database.
 * This is a junction table between Recipe and Tag, to prevent a many-to-many relationship.
 * One Tag can have many RecipeTags, and one Recipe can have many RecipeTags.
 *
 * @author Imogen Pemberton, 2026
 */
@Entity(
    tableName = "RecipeTag",
    foreignKeys = [
        // Recipe relation:
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"], // the primary key of Recipe
            childColumns = ["recipeId"], // the foreign key which holds this reference, in RecipeTag
            onDelete = ForeignKey.CASCADE
        ),
        // Tag relation:
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"], // the primary key of Tag
            childColumns = ["tagId"], // the foreign key which holds this reference, in RecipeTag
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("recipeId"),
        Index("tagId"),
    ]
)

data class RecipeTag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipeId") val recipeId: Long, // corresponding Recipe
    @ColumnInfo(name = "tagId") val tagId: Long, // corresponding Tag
)
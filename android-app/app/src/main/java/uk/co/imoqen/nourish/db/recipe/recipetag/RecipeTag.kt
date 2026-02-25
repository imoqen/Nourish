package uk.co.imoqen.nourish.db.recipe.recipetag

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.tag.Tag

// relates a tag to a recipe as they have a many to many relationship,
// this junction table breaks it down.
// one tag can have many recipe tags. one recipe can have many recipe tags.

@Entity(
    tableName = "RecipeTag",
    foreignKeys = [
        // recipe relation:
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"], // the primary key of Recipe
            childColumns = ["recipeId"], // the foreign key which holds this reference, in RecipeTag
            onDelete = ForeignKey.CASCADE
        ),
        // tag relation:
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
    @ColumnInfo(name = "recipeId") val recipeId: Long, // corresponding recipe
    @ColumnInfo(name = "tagId") val tagId: Long, // corresponding tag
)

data class RecipeWithRecipeTags(
    @Embedded val recipe: Recipe, // parent
    @Relation(
        parentColumn = "id", // the primary key of the parent (Recipe)
        entityColumn = "recipeId" // the foreign key in RecipeTag referencing it
    )
    val collections: List<RecipeTag>, // entity
)

data class TagWithRecipeTags(
    @Embedded val tag: Tag, // parent
    @Relation(
        parentColumn = "id", // the primary key of the parent (Tag)
        entityColumn = "tagId" // the foreign key in RecipeTag referencing it
    )
    val collections: List<RecipeTag>, // entity
)
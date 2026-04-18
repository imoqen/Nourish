package uk.co.imoqen.nourish.db.ingredient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The Ingredient table in the Room database.
 */
@Entity(
    tableName = "Ingredient",
    // to prevent duplicate ingredients being added,
    // indexed by always the plural form, as this is what is used in the shopping list.
    indices = [Index(value = ["plural"], unique = true)]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "singular") val singular: String,
    @ColumnInfo(name = "plural") val plural: String,
    // so that the LLM will overwrite it to get correct singular / plural if a userAdded Ingredient
    // is encountered:
    @ColumnInfo(name = "userAdded") val userAdded: Boolean
)
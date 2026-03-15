package uk.co.imoqen.nourish.db.ingredient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Ingredient",
    indices = [Index(value = ["plural"], unique = true)] // to prevent duplicate ingredients being added, always the plural form as this is what is used in the shopping list.
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "singular") val singular: String,
    @ColumnInfo(name = "plural") val plural: String,
)
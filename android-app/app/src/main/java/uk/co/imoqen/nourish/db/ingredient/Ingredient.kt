package uk.co.imoqen.nourish.db.ingredient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Ingredient")
data class Ingredient(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "ingredientId") val ingredientId: Int, // foreign key for recipe relation
    @ColumnInfo(name = "name") val name: String, // foreign key for ingredient relation
)
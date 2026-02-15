package uk.co.imoqen.nourish.db.recipe

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecipeImage(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "uri") val uri: String?,
    // also can add associated recipe here in future
)

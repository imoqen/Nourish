package uk.co.imoqen.nourish.db.tag

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Tag"
)
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "identifier") val identifier: String, // identifier of the tag e.g. "Vegan"
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "colour") val colour: String, // maps to colour enum, from RED, PINK, ORANGE, YELLOW, GREEN, BLUE, PURPLE, GREY
)
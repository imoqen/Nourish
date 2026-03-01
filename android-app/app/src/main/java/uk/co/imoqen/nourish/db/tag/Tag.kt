package uk.co.imoqen.nourish.db.tag

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.user.User

@Entity(
    tableName = "Tag",
    foreignKeys = [
        // user relation:
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"], // the primary key of User
            childColumns = ["authorId"], // the foreign key which holds this reference, in Tag
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["authorId", "displayName"], unique = true), // authorId and displayName combination are unique; ensures no dupe tags are added.
    ]
)
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "identifier") val identifier: String, // identifier of the tag e.g. "Vegan"
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "colour") val colour: String, // maps to colour enum, from RED, PINK, ORANGE, YELLOW, GREEN, BLUE, PURPLE, GREY
    @ColumnInfo(name = "authorId") val authorId: String // owner of the tag
)
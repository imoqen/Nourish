package uk.co.imoqen.nourish.db.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// password not stored locally - only on firebase.
@Entity(tableName = "User")
data class User(
    @PrimaryKey() val uid: String,
    @ColumnInfo(name = "email") val email: String
)
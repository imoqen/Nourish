package uk.co.imoqen.nourish.db.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The User table in the Room database. The password is not stored locally, only on Firebase.
 * The purpose of the User table is to connect a user to its recipes and associated data,
 * so that multiple users can use the app at a given time.
 */
@Entity(tableName = "User")
data class User(
    @PrimaryKey() val uid: String,
    @ColumnInfo(name = "email") val email: String
)
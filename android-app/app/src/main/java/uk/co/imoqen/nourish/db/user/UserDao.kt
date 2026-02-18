package uk.co.imoqen.nourish.db.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uk.co.imoqen.nourish.db.ingredient.Ingredient

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM user WHERE uid = :uid")
    suspend fun getByUid(uid: String): User?

    @Insert
    fun insertAllUsers(vararg users: User)

    @Delete
    fun deleteUser(user: User)

    // insert one, ignore if already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)
}
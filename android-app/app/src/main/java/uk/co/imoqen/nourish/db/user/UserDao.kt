package uk.co.imoqen.nourish.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * The UserDao, holding logic to perform CRUD operations on the database relating to User.
 */

@Dao
interface UserDao {
    // insert one, ignore if already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)
}
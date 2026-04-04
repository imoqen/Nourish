package uk.co.imoqen.nourish.db.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uk.co.imoqen.nourish.db.ingredient.Ingredient

@Dao
interface UserDao {
    // insert one, ignore if already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)
}
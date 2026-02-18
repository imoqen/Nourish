package uk.co.imoqen.nourish.db.user

import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
){
    //suspend fun numberOfItemsInDB() = ingredientDao.numberOfItemsInDB()

    suspend fun insert(user: User) = userDao.insert(user)

    suspend fun getByUid(uid: String) = userDao.getByUid(uid)
}
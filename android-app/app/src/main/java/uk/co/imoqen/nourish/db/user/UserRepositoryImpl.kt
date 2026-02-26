package uk.co.imoqen.nourish.db.user

import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
): UserRepository {
    override suspend fun insert(user: User) = userDao.insert(user)
    override suspend fun getByUid(uid: String): User? = userDao.getByUid(uid)
}
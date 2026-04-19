package uk.co.imoqen.nourish.db.user

import javax.inject.Inject

/**
 * The UserRepositoryImpl, implementing the repository for User by interacting with the required DAO.
 * @param userDao - the User DAO
 *
 * @author Imogen Pemberton, 2026
 */
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
): UserRepository {
    override suspend fun insert(user: User) = userDao.insert(user)
}
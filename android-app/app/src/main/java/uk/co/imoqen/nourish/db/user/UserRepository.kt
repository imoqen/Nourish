package uk.co.imoqen.nourish.db.user

/**
 * The UserRepository, providing a contract of methods to interact with the DAO regarding
 * User-related logic.
 *
 * @author Imogen Pemberton, 2026
 */
interface UserRepository {
    suspend fun insert(user: User)
}
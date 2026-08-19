package co.uk.imoqen.testfixtures.repository

import uk.co.imoqen.nourish.db.user.User
import uk.co.imoqen.nourish.db.user.UserRepository

/**
 * Mock UserRepository for tests.
 *
 * @author Imogen Pemberton, 2026
 */
class FakeUserRepository(): UserRepository {
    override suspend fun insert(user: User) {
    }
}
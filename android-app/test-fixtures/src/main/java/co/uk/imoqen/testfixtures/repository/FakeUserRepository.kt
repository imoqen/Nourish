package co.uk.imoqen.testfixtures.repository

import uk.co.imoqen.nourish.db.user.User
import uk.co.imoqen.nourish.db.user.UserRepository

class FakeUserRepository(): UserRepository {
    override suspend fun insert(user: User) {
    }
}
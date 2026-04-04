package uk.co.imoqen.nourish.db.user

interface UserRepository {
    suspend fun insert(user: User)
}
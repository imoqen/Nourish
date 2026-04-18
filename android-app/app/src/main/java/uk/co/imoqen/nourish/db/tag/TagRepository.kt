package uk.co.imoqen.nourish.db.tag

import kotlinx.coroutines.flow.Flow

/**
 * The TagRepository, providing a contract of methods to interact with the DAO regarding
 * Tag-related logic.
 */
interface TagRepository {
    fun getAllForUser(uid: String): Flow<List<Tag>>

    suspend fun insert(tag: Tag): Long

    suspend fun delete(tag: Tag)

    suspend fun getIdByIdentifier(identifier: String, uid: String): Long?

    fun getById(id: Long): Flow<Tag?>

}
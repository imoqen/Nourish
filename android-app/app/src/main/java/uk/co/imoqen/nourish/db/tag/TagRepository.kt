package uk.co.imoqen.nourish.db.tag

import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllForUser(uid: String): Flow<List<Tag>>

    suspend fun insert(tag: Tag): Long

    suspend fun getIdByIdentifier(identifier: String): Long?

    fun getById(id: Long): Flow<Tag?>
}
package uk.co.imoqen.nourish.db.tag

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
): TagRepository {
    override fun getAll(): Flow<List<Tag>> = tagDao.getAll()

    override suspend fun insert(tag: Tag): Long = tagDao.insert(tag)

    override suspend fun getIdByIdentifier(identifier: String): Long? = tagDao.getIdByIdentifier(identifier)

    override fun getById(id: Long): Flow<Tag?> = tagDao.getById(id)
}
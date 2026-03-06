package uk.co.imoqen.nourish.db.tag

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
): TagRepository {

    override fun getAllForUser(uid: String): Flow<List<Tag>> = tagDao.getAllForUser(uid)

    override suspend fun insert(tag: Tag): Long = tagDao.insert(tag)

    override suspend fun delete(tag: Tag) = tagDao.delete(tag)

    // gets per user, so different users cannot use each others tags as this wouldnt make sense.
    override suspend fun getIdByIdentifier(identifier: String, uid: String): Long? = tagDao.getIdByIdentifier(identifier, uid)

    override fun getById(id: Long): Flow<Tag?> = tagDao.getById(id)
}
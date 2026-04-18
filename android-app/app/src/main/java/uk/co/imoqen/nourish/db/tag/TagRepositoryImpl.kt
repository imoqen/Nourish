package uk.co.imoqen.nourish.db.tag

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * The TagRepositoryImpl, implementing the repository for Tag by
 * interacting with the required DAO.
 * Doc comments are not provided for most methods, because most are trivial. For those which are non
 * -trivial, a doc comment is present.
 * @param tagDao - the Tag DAO
 */
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
): TagRepository {

    override fun getAllForUser(uid: String): Flow<List<Tag>> = tagDao.getAllForUser(uid)

    override suspend fun insert(tag: Tag): Long = tagDao.insert(tag)

    override suspend fun delete(tag: Tag) = tagDao.delete(tag)

    /**
     * Obtains per user, so that different users cannot use or see each other's tags.
     * @param identifier - The identifier of the tag to find
     * @param uid - the uid of the user whose tags to search
     * @return Long id of the identified tag, or null if none identified.
     */
    override suspend fun getIdByIdentifier(
        identifier: String,
        uid: String
    ): Long? = tagDao.getIdByIdentifier(identifier, uid)

    override fun getById(id: Long): Flow<Tag?> = tagDao.getById(id)
}
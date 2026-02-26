package co.uk.imoqen.testfixtures.repository

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository

class FakeTagRepository(): TagRepository {
    override fun getAll(): Flow<List<Tag>> {
        TODO("Not yet implemented")
    }

    override suspend fun insert(tag: Tag): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getIdByIdentifier(identifier: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Flow<Tag?> {
        TODO("Not yet implemented")
    }
}
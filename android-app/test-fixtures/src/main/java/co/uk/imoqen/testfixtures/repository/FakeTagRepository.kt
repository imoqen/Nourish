package co.uk.imoqen.testfixtures.repository

import co.uk.imoqen.testfixtures.common.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository

class FakeTagRepository(): TagRepository {

    override fun getAllForUser(uid: String): Flow<List<Tag>> {
        return flowOf(listOf(
            TestData.tag_lowcalorie, TestData.tag_vegan
        ))
    }

    override suspend fun insert(tag: Tag): Long {
        return 2 // successful insertion with id 2
    }

    override suspend fun delete(tag: Tag) {
        // do nothing to simulate successful tag deletion
    }

    override suspend fun getIdByIdentifier(
        identifier: String,
        uid: String
    ): Long? {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Flow<Tag?> {
        TODO("Not yet implemented")
    }
}
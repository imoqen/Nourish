package co.uk.imoqen.testfixtures.repository

import co.uk.imoqen.testfixtures.common.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
import kotlin.collections.listOf

/**
 * Mock TagRepository for tests.
 *
 * @author Imogen Pemberton, 2026
 */
class FakeTagRepository(): TagRepository {

    var lastCalled = ""

    override fun getAllForUser(uid: String): Flow<List<Tag>> {
        if (uid == "user_without_tags") {
            return flowOf(listOf())
        }
        return flowOf(listOf(
            TestData.tag_lowcalorie, TestData.tag_vegan
        ))
    }

    override suspend fun insert(tag: Tag): Long {
        lastCalled = "insert"
        return 2 // successful insertion with id 2
    }

    override suspend fun delete(tag: Tag) {
        // do nothing to simulate successful tag deletion
    }

    override suspend fun getIdByIdentifier(
        identifier: String,
        uid: String
    ): Long? {
        return 0
    }

    override fun getById(id: Long): Flow<Tag?> {
        if (id == 10L) {
            return flowOf(null)
        }
        return flowOf( TestData.tag_vegan )
    }
}
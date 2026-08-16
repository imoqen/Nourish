package co.uk.imoqen.testfixtures.firebase

import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager

/**
 * Mock FirestoreManager for tests.
 *
 * @author Imogen Pemberton, 2026
 */
class FakeFirestoreManager(): FirestoreManager {

    override suspend fun addUserToFirestoreIfNotExists(uid: String, email: String) {
    }

    override suspend fun deleteAllUserData(uid: String) {
    }

}
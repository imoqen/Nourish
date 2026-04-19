package co.uk.imoqen.testfixtures.firebase

import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager

class FakeFirestoreManager(): FirestoreManager {

    override suspend fun addUserToFirestoreIfNotExists(uid: String, email: String) {
    }

    override suspend fun deleteAllUserData(uid: String) {
    }

}
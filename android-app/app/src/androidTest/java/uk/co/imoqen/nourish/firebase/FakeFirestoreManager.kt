package uk.co.imoqen.nourish.firebase

import com.google.firebase.auth.FirebaseUser
import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager

class FakeFirestoreManager(): FirestoreManager {

    override suspend fun addUserToFirestoreIfNotExists(user: FirebaseUser) {
        TODO("Not yet implemented")
    }

}
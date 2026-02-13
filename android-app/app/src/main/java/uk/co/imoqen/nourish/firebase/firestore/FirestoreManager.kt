package uk.co.imoqen.nourish.firebase.firestore

interface FirestoreManager {
    suspend fun addUserToFirestoreIfNotExists(uid: String, email: String)

    suspend fun deleteAllUserData(uid: String)
}
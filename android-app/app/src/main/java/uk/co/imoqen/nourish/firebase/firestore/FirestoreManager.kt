package uk.co.imoqen.nourish.firebase.firestore

/**
 * The FirestoreManager, used to communicate with Firebase Firestore for user token limiting.
 */
interface FirestoreManager {
    suspend fun addUserToFirestoreIfNotExists(uid: String, email: String)

    suspend fun deleteAllUserData(uid: String)
}
package uk.co.imoqen.nourish.firebase.firestore

/**
 * The FirestoreManager, used to communicate with Firebase Firestore for user token limiting.
 *
 * @author Imogen Pemberton, 2026
 */
interface FirestoreManager {
    suspend fun addUserToFirestoreIfNotExists(uid: String, email: String)

    suspend fun deleteAllUserData(uid: String)
}
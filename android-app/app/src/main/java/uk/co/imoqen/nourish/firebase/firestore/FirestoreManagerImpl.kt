package uk.co.imoqen.nourish.firebase.firestore

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation for the FirestoreManager
 * @param db - the external Firebase Firestore database.
 */
@Singleton
class FirestoreManagerImpl @Inject constructor(
    private val db: FirebaseFirestore
): FirestoreManager {

    /**
     * Adds a user to the Firebase Firestore if they are not already in it, to ensure the Firestore
     * is always kept up to date with all user data.
     * Called by addIfNotExists() in the AuthenticationManager upon user login.
     * @param uid - the uid of the user to add to Firestore.
     * @param email - the email of the user to add to Firestore.
     */
    override suspend fun addUserToFirestoreIfNotExists(uid: String, email: String) {
        // Check if document with uid already exists; if not, add.
        val result = db.collection("users").document(uid).get().await()
        if (!result.exists()) {
            addUserToFirestore(uid, email)
        }
    }

    /**
     * Private auxiliary function to deletes all of a user's data from the Firebase Firestore.
     * This is to be performed when a user's account is deleted.
     * @param uid - the uid of the user to delete the data of.
     */
    override suspend fun deleteAllUserData(uid: String) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(uid)

        // Recursively delete all of the user data
        val tokenUsages = userRef.collection("token-usage").get().await().documents
        tokenUsages.forEach{ tokenUsage ->
            tokenUsage.reference.delete().await()
        }
        userRef.delete().await()
    }

    /**
     * Private auxiliary function to add the user's email to the Firebase Firestore users collection,
     * in a document titled their uid and containing their email address.
     * Adding data to firestore: https://firebase.google.com/docs/firestore/quickstart#android
     * @param user - the user to add to Firestore
     */
    private fun addUserToFirestore(uid: String, email: String) {
        val userData = hashMapOf(
            "email" to email,
        )

        // adds the uid to the users and updates if already exists
        db.collection("users")
            .document(uid) // adds new doc with uid title
            .set(userData) // sets the email of the user in it
    }

}
package uk.co.imoqen.nourish.firebase.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreManagerImpl @Inject constructor(
    private val db: FirebaseFirestore
): FirestoreManager {

    /**
     * Adds a user to the Firebase Firestore if they are not already in it, to ensure the Firestore
     * is always kept up to date with all user data.
     * Called by addIfNotExists() in the AuthenticationManager upon user login.
     * @param user - the user to add to Firestore.
     */
    override suspend fun addUserToFirestoreIfNotExists(user: FirebaseUser) {
        // Check if document with uid already exists; if not, add.
        val result = db.collection("users").document(user.uid).get().await()
        if (!result.exists()) {
            addUserToFirestore(user)
        }
    }

    /**
     * Private auxiliary function to add the user's email to the Firebase Firestore users collection,
     * in a document titled their uid and containing their email address.
     * Adding data to firestore: https://firebase.google.com/docs/firestore/quickstart#android
     * @param user - the user to add to Firestore
     */
    private fun addUserToFirestore(user: FirebaseUser) {
        val userData = hashMapOf(
            "email" to user.email,
        )

        // adds the uid to the users and updates if already exists
        db.collection("users")
            .document(user.uid) // adds new doc with uid title
            .set(userData) // sets the email of the user in it
            .addOnSuccessListener {
                Log.i("FIRESTORE", "User (${user.email}) with uid ${user.uid} successfully added to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "User could not be added to Firestore: $e")
            }
    }

}
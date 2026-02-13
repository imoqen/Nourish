package uk.co.imoqen.nourish.firebase.firestore

import com.google.firebase.auth.FirebaseUser

interface FirestoreManager {
    suspend fun addUserToFirestoreIfNotExists(user: FirebaseUser)
}
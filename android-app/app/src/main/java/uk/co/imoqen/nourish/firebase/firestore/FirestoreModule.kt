package uk.co.imoqen.nourish.firebase.firestore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module to bind the Firestore manager, providing the dependency via Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreModule {

    @Binds
    @Singleton
    abstract fun bindFirestoreManager(
        firestoreManagerImpl: FirestoreManagerImpl
    ): FirestoreManager

}
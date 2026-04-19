package uk.co.imoqen.nourish.firebase.authentication

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module to bind the authentication manager, providing the dependency via Hilt.
 *
 * @author Imogen Pemberton, 2026
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {

    @Binds
    @Singleton
    abstract fun bindAuthenticationManager(
        authenticationManagerImpl: AuthenticationManagerImpl
    ): AuthenticationManager

}
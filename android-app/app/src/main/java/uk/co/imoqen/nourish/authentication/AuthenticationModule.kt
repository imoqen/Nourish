package uk.co.imoqen.nourish.authentication

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Module to bind the authentication manager, providing the dependency via Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {

    @Binds
    abstract fun bindAuthenticationManager(
        authenticationManagerImpl: AuthenticationManagerImpl
    ): AuthenticationManager

}
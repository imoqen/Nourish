package uk.co.imoqen.nourish.authentication

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {

    @Binds
    abstract fun bindAuthenticationManager(
        authenticationManagerImpl: AuthenticationManagerImpl
    ): AuthenticationManager

}
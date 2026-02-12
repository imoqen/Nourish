package uk.co.imoqen.nourish.authentication

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Module to bind the fake authentication manager, for use in tests.
 */
/*@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AuthenticationModule::class]
)*/
abstract class FakeAuthenticationModule {

    /*@Singleton
    @Binds
    abstract fun bindAuthenticationManager(
        fakeAuthenticationManager: FakeAuthenticationManager
    ): AuthenticationManager*/

}
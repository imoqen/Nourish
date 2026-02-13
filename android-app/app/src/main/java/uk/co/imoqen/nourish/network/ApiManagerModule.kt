package uk.co.imoqen.nourish.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module to bind the api manager, providing the dependency via Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ApiManagerModule {

    @Binds
    @Singleton
    abstract fun bindApiManager(
        apiManagerImpl: ApiManagerImpl
    ): ApiManager

}
package uk.co.imoqen.nourish.common.uievent

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module to bind the UiEventBus, providing the dependency via Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UiEventBusModule {

    @Binds
    @Singleton
    abstract fun bindUiEventBus(
        uiEventBusImpl: UiEventBusImpl
    ): UiEventBus

}
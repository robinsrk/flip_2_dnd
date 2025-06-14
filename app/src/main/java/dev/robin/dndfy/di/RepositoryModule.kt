package dev.robin.dndfy.di

import dev.robin.dndfy.data.repository.DndRepositoryImpl
import dev.robin.dndfy.data.repository.FeedbackRepositoryImpl
import dev.robin.dndfy.data.repository.OrientationRepositoryImpl
import dev.robin.dndfy.data.repository.ScreenStateRepositoryImpl
import dev.robin.dndfy.data.repository.SettingsRepositoryImpl
import dev.robin.dndfy.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDndRepository(
        dndRepositoryImpl: DndRepositoryImpl
    ): DndRepository

    @Binds
    @Singleton
    abstract fun bindScreenStateRepository(
        screenStateRepositoryImpl: ScreenStateRepositoryImpl
    ): ScreenStateRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindOrientationRepository(
        orientationRepositoryImpl: OrientationRepositoryImpl
    ): OrientationRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        feedbackRepositoryImpl: FeedbackRepositoryImpl
    ): FeedbackRepository
}

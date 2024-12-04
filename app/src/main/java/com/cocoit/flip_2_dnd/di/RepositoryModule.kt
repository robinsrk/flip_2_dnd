package com.cocoit.flip_2_dnd.di

import com.cocoit.flip_2_dnd.data.repository.DndRepositoryImpl
import com.cocoit.flip_2_dnd.data.repository.FeedbackRepositoryImpl
import com.cocoit.flip_2_dnd.data.repository.OrientationRepositoryImpl
import com.cocoit.flip_2_dnd.data.repository.ScreenStateRepositoryImpl
import com.cocoit.flip_2_dnd.data.repository.SettingsRepositoryImpl
import com.cocoit.flip_2_dnd.domain.repository.DndRepository
import com.cocoit.flip_2_dnd.domain.repository.FeedbackRepository
import com.cocoit.flip_2_dnd.domain.repository.OrientationRepository
import com.cocoit.flip_2_dnd.domain.repository.ScreenStateRepository
import com.cocoit.flip_2_dnd.domain.repository.SettingsRepository
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
    abstract fun bindOrientationRepository(
        orientationRepositoryImpl: OrientationRepositoryImpl
    ): OrientationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        feedbackRepositoryImpl: FeedbackRepositoryImpl
    ): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindScreenStateRepository(
        screenStateRepositoryImpl: ScreenStateRepositoryImpl
    ): ScreenStateRepository
}

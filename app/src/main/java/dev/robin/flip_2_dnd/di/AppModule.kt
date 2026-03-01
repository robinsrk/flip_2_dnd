package dev.robin.flip_2_dnd.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.robin.flip_2_dnd.data.local.AppDatabase
import dev.robin.flip_2_dnd.data.local.dao.HistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flip2dnd_db"
        ).build()
    }

    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideSoundController(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.SoundController {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getSoundController(context)
    }

    @Provides
    @Singleton
    fun provideFlashController(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.FlashController {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getFlashController(context)
    }

    @Provides
    @Singleton
    fun providePowerController(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.PowerController {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getPowerController(context)
    }

    @Provides
    @Singleton
    fun provideScheduleManager(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.ScheduleManager {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getScheduleManager(context)
    }

    @Provides
    @Singleton
    fun provideDetectionManager(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.DetectionManager {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getDetectionManager(context)
    }

    @Provides
    @Singleton
    fun provideSoundPicker(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.SoundPicker {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getSoundPicker(context)
    }

    @Provides
    @Singleton
    fun provideFeatureManager(@ApplicationContext context: Context): dev.robin.flip_2_dnd.core.ProFeatureManager {
        return dev.robin.flip_2_dnd.core.ServiceLocator.getFeatureManager(context)
    }
}

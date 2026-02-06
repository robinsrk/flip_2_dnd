package dev.robin.flip_2_dnd.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.robin.flip_2_dnd.data.local.dao.HistoryDao
import dev.robin.flip_2_dnd.data.local.entity.HistoryEntity

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}

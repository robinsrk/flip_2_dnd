package dev.robin.flip_2_dnd.data.repository

import dev.robin.flip_2_dnd.data.local.dao.HistoryDao
import dev.robin.flip_2_dnd.data.local.entity.HistoryEntity
import dev.robin.flip_2_dnd.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {
    override fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    override suspend fun addHistory(isEnabled: Boolean, dndMode: Int) {
        val history = HistoryEntity(
            timestamp = System.currentTimeMillis(),
            isEnabled = isEnabled,
            dndMode = dndMode
        )
        historyDao.insertHistory(history)
        historyDao.trimHistory() // Keep only last 100 entries
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}

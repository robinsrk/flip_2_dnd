package dev.robin.flip_2_dnd.domain.repository

import dev.robin.flip_2_dnd.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryEntity>>
    suspend fun addHistory(isEnabled: Boolean, dndMode: Int)
    suspend fun clearHistory()
}

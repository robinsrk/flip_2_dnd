package dev.robin.flip_2_dnd.domain.repository

import kotlinx.coroutines.flow.Flow

interface DndRepository {
    fun isDndEnabled(): Flow<Boolean>
    fun getDndMode(): Flow<String>
    suspend fun setDndEnabled(enabled: Boolean)
    suspend fun toggleDnd()
    fun onCleared()
}

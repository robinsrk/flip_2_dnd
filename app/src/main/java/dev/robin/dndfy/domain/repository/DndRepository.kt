package dev.robin.dndfy.domain.repository

import kotlinx.coroutines.flow.Flow

interface DndRepository {
    fun isDndEnabled(): Flow<Boolean>
    fun getDndMode(): Flow<String>
    suspend fun setDndEnabled(enabled: Boolean)
    suspend fun toggleDnd()
    fun onCleared()
}

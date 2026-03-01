package dev.robin.flip_2_dnd.core

import kotlinx.coroutines.flow.Flow

interface DndRepository {
    fun isActivated(): Flow<Boolean>
    fun getDndMode(): Flow<Int>
    suspend fun setActivated(enabled: Boolean)
    suspend fun toggle()
    fun onCleared()
}

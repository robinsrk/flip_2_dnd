package com.cocoit.flip_2_dnd.domain.repository

import kotlinx.coroutines.flow.Flow

interface DndRepository {
    fun isDndEnabled(): Flow<Boolean>
    suspend fun setDndEnabled(enabled: Boolean)
    suspend fun toggleDnd()
}

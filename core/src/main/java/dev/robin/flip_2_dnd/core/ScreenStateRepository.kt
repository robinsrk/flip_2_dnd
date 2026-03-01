package dev.robin.flip_2_dnd.core

import kotlinx.coroutines.flow.Flow

interface ScreenStateRepository {
    fun isScreenOff(): Flow<Boolean>
    fun startMonitoring()
    fun stopMonitoring()
}

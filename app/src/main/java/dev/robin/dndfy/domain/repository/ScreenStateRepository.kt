package dev.robin.dndfy.domain.repository

import kotlinx.coroutines.flow.Flow

interface ScreenStateRepository {
    fun isScreenOff(): Flow<Boolean>
    fun startMonitoring()
    fun stopMonitoring()
}

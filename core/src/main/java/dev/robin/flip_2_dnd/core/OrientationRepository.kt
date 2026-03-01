package dev.robin.flip_2_dnd.core

import kotlinx.coroutines.flow.Flow

interface OrientationRepository {
    fun getOrientation(): Flow<PhoneOrientation>
    suspend fun startMonitoring()
    suspend fun stopMonitoring()
}

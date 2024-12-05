package dev.robin.flip_2_dnd.domain.repository

import dev.robin.flip_2_dnd.domain.model.PhoneOrientation
import kotlinx.coroutines.flow.Flow

interface OrientationRepository {
    fun getOrientation(): Flow<PhoneOrientation>
    suspend fun startMonitoring()
    suspend fun stopMonitoring()
}

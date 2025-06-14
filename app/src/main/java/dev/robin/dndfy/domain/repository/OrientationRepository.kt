package dev.robin.dndfy.domain.repository

import dev.robin.dndfy.domain.model.PhoneOrientation
import kotlinx.coroutines.flow.Flow

interface OrientationRepository {
    fun getOrientation(): Flow<PhoneOrientation>
    suspend fun startMonitoring()
    suspend fun stopMonitoring()
}

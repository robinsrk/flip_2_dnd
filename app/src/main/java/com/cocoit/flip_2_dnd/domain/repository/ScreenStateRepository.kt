package com.cocoit.flip_2_dnd.domain.repository

import kotlinx.coroutines.flow.Flow

interface ScreenStateRepository {
    fun isScreenOff(): Flow<Boolean>
    fun startMonitoring()
    fun stopMonitoring()
}

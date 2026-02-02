package dev.robin.flip_2_dnd.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import dev.robin.flip_2_dnd.domain.repository.ScreenStateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenStateRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ScreenStateRepository {
    private val _isScreenOff = MutableStateFlow(false)
    private var isMonitoring = false

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen turned OFF")
                    _isScreenOff.value = true
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen turned ON")
                    _isScreenOff.value = false
                }
            }
        }
    }

    init {
        // Initialize screen state
        _isScreenOff.value = !powerManager.isInteractive
    }

    override fun isScreenOff(): Flow<Boolean> = _isScreenOff

    override fun startMonitoring() {
        if (!isMonitoring) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            context.registerReceiver(screenStateReceiver, filter)
            isMonitoring = true
            Log.d(TAG, "Started screen state monitoring")
        }
    }

    override fun stopMonitoring() {
        if (isMonitoring) {
            context.unregisterReceiver(screenStateReceiver)
            isMonitoring = false
            Log.d(TAG, "Stopped screen state monitoring")
        }
    }

    companion object {
        private const val TAG = "ScreenStateRepository"
    }
}

package com.cocoit.flip_2_dnd.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val _onlyWhenScreenOff = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_ONLY_WHEN_SCREEN_OFF, false)
    )
    val onlyWhenScreenOff: StateFlow<Boolean> = _onlyWhenScreenOff.asStateFlow()

    fun setOnlyWhenScreenOff(enabled: Boolean) {
        try {
            sharedPreferences.edit()
                .putBoolean(KEY_ONLY_WHEN_SCREEN_OFF, enabled)
                .apply()
            _onlyWhenScreenOff.value = enabled
            Log.d(TAG, "Updated only when screen off setting to: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating only when screen off setting: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "SettingsManager"
        private const val PREFERENCES_NAME = "flip_2_dnd_preferences"
        private const val KEY_ONLY_WHEN_SCREEN_OFF = "only_when_screen_off"
    }
}

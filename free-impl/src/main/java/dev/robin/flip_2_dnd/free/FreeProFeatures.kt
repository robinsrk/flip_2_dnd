package dev.robin.flip_2_dnd.free

import android.widget.Toast
import android.content.Context
import dev.robin.flip_2_dnd.core.ProFeatureManager
import dev.robin.flip_2_dnd.core.UpdateResponse
import dev.robin.flip_2_dnd.core.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FreeProFeatures(context: Context) : ProFeatureManager {
    override fun autoStartEnabled() = false
    override fun advancedSensitivityEnabled() = false
    override fun delayCustomizationEnabled() = false
    override fun scheduleEnabled() = false
    override fun customSoundEnabled() = false
    override fun batterySaverSyncEnabled() = false
    override fun detectionFiltersEnabled() = false
    override fun telegramSupportEnabled() = false
    override fun flashlightFeedbackEnabled() = false
    override fun isPro() = false

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.None)
    override fun getUpdateState() = _updateState.asStateFlow()
    override fun checkForUpdate(manual: Boolean) {
        // No-op
    }
    
    override fun downloadAndInstall(context: Context, update: UpdateResponse) {
        Toast.makeText(context, "Feature not available in Free version", Toast.LENGTH_SHORT).show()
    }
}

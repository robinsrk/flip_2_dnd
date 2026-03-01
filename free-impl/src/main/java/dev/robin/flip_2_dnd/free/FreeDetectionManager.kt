package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.DetectionManager

class FreeDetectionManager(context: Context) : DetectionManager {
    override fun isMediaPlaying() = false
    override fun areHeadphonesConnected() = false
    override fun isProximityCovered() = false
    override fun registerProximityListener(callback: (Boolean) -> Unit) {}
    override fun unregisterProximityListener() {}
}

package dev.robin.flip_2_dnd.core

interface DetectionManager {
    fun isMediaPlaying(): Boolean
    fun areHeadphonesConnected(): Boolean
    fun isProximityCovered(): Boolean
    fun registerProximityListener(callback: (Boolean) -> Unit)
    fun unregisterProximityListener()
}

package dev.robin.flip_2_dnd.core

import android.content.Context
import android.util.Log

object ServiceLocator {
    private const val TAG = "ServiceLocator"
    private const val PRO_PACKAGE = "dev.robin.flip_2_dnd.pro"
    private const val FREE_PACKAGE = "dev.robin.flip_2_dnd.free"

    private var _featureManager: ProFeatureManager? = null
    private var _flashController: FlashController? = null
    private var _powerController: PowerController? = null
    private var _scheduleManager: ScheduleManager? = null
    private var _detectionManager: DetectionManager? = null
    private var _sensorManagerPro: SensorManagerPro? = null
    private var _soundController: SoundController? = null
    private var _soundPicker: SoundPicker? = null

    fun getFeatureManager(context: Context): ProFeatureManager {
        return _featureManager ?: loadImplementation<ProFeatureManager>(
            context, "RealProFeatures", "FreeProFeatures"
        ).also { _featureManager = it }
    }

    fun getFlashController(context: Context): FlashController {
        return _flashController ?: loadImplementation<FlashController>(
            context, "RealFlashController", "FreeFlashController"
        ).also { _flashController = it }
    }

    fun getPowerController(context: Context): PowerController {
        return _powerController ?: loadImplementation<PowerController>(
            context, "RealPowerController", "FreePowerController"
        ).also { _powerController = it }
    }

    fun getScheduleManager(context: Context): ScheduleManager {
        // Schedule manager might not need context, but keeping signature consistent
        return _scheduleManager ?: loadImplementation<ScheduleManager>(
            context, "RealScheduleManager", "FreeScheduleManager"
        ).also { _scheduleManager = it }
    }

    fun getDetectionManager(context: Context): DetectionManager {
        return _detectionManager ?: loadImplementation<DetectionManager>(
            context, "RealDetectionManager", "FreeDetectionManager"
        ).also { _detectionManager = it }
    }

    fun getSensorManagerPro(context: Context): SensorManagerPro {
        return _sensorManagerPro ?: loadImplementation<SensorManagerPro>(
            context, "RealSensorManagerPro", "FreeSensorManagerPro"
        ).also { _sensorManagerPro = it }
    }

    fun getSoundController(context: Context): SoundController {
        return _soundController ?: loadImplementation<SoundController>(
            context, "RealSoundController", "FreeSoundController"
        ).also { _soundController = it }
    }

    fun getSoundPicker(context: Context): SoundPicker {
        return _soundPicker ?: loadImplementation<SoundPicker>(
            context, "RealSoundPicker", "FreeSoundPicker"
        ).also { _soundPicker = it }
    }

    private inline fun <reified T> loadImplementation(
        context: Context,
        proClassName: String,
        freeClassName: String
    ): T {
        return try {
            val clazz = Class.forName("$PRO_PACKAGE.$proClassName")
            val constructor = clazz.getConstructor(Context::class.java)
            constructor.newInstance(context) as T
        } catch (e: Exception) {
            Log.d(TAG, "Pro implementation $proClassName not found or failed to load, falling back to $freeClassName. Error: ${e.message}")
            try {
                val clazz = Class.forName("$FREE_PACKAGE.$freeClassName")
                val constructor = clazz.getConstructor(Context::class.java)
                constructor.newInstance(context) as T
            } catch (ex: Exception) {
                throw RuntimeException("Failed to load both Pro and Free implementations for ${T::class.java.simpleName}", ex)
            }
        }
    }
}

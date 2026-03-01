package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.ScheduleManager

class FreeScheduleManager(context: Context) : ScheduleManager {
    override fun isWithinSchedule(startTime: String, endTime: String, days: Set<Int>): Boolean {
        return true // Default to true, gating happens at UI/FeatureManager level
    }
}

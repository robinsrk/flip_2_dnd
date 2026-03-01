package dev.robin.flip_2_dnd.core

interface ScheduleManager {
    /**
     * Checks if the current time is within the specified schedule.
     * Handles overnight schedules (e.g., 22:00 to 07:00).
     */
    fun isWithinSchedule(startTime: String, endTime: String, days: Set<Int>): Boolean
}

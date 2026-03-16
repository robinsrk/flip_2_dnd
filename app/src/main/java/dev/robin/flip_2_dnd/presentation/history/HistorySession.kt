package dev.robin.flip_2_dnd.presentation.history

data class HistorySession(
    val id: Int,
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val dndMode: Int,
    val isOngoing: Boolean
) {
    val durationMillis: Long?
        get() = endTimestamp?.let { it - startTimestamp }
}
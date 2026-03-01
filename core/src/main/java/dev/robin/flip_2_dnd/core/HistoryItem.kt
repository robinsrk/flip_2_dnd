package dev.robin.flip_2_dnd.core

data class HistoryItem(
    val id: Int = 0,
    val timestamp: Long,
    val isEnabled: Boolean,
    val dndMode: Int
)

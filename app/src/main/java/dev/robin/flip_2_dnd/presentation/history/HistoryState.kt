package dev.robin.flip_2_dnd.presentation.history

import dev.robin.flip_2_dnd.core.HistoryItem

data class HistoryState(
    val historyItems: List<HistoryItem> = emptyList()
)

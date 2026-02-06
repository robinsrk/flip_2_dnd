package dev.robin.flip_2_dnd.presentation.history

import dev.robin.flip_2_dnd.data.local.entity.HistoryEntity

data class HistoryState(
    val historyItems: List<HistoryEntity> = emptyList()
)

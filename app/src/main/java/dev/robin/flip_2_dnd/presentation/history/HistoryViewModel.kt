package dev.robin.flip_2_dnd.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.robin.flip_2_dnd.core.HistoryItem
import dev.robin.flip_2_dnd.core.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.getAllHistory().collect { items ->
                val sessions = processToSessions(items)
                _state.update { it.copy(historySessions = sessions) }
            }
        }
    }

    private fun processToSessions(items: List<HistoryItem>): List<HistorySession> {
        if (items.isEmpty()) return emptyList()

        val sortedItems = items.sortedBy { it.timestamp }
        val sessions = mutableListOf<HistorySession>()
        
        var sessionId = 0
        var i = 0
        
        while (i < sortedItems.size) {
            val item = sortedItems[i]
            if (item.isEnabled) {
                val startTimestamp = item.timestamp
                val dndMode = item.dndMode
                
                val endTimestamp = if (i + 1 < sortedItems.size && !sortedItems[i + 1].isEnabled) {
                    sortedItems[i + 1].timestamp
                } else {
                    null
                }
                
                sessions.add(
                    HistorySession(
                        id = sessionId++,
                        startTimestamp = startTimestamp,
                        endTimestamp = endTimestamp,
                        dndMode = dndMode,
                        isOngoing = endTimestamp == null
                    )
                )
            }
            i++
        }
        
        return sessions.reversed()
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
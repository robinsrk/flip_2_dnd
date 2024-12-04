package com.cocoit.flip_2_dnd.domain.usecase

import com.cocoit.flip_2_dnd.domain.repository.DndRepository
import javax.inject.Inject

class ToggleDndUseCase @Inject constructor(
    private val dndRepository: DndRepository
) {
    suspend operator fun invoke() {
        dndRepository.toggleDnd()
    }
}

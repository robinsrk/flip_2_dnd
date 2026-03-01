package dev.robin.flip_2_dnd.domain.usecase

import dev.robin.flip_2_dnd.core.DndRepository
import javax.inject.Inject

class ToggleDndUseCase @Inject constructor(
    private val dndRepository: DndRepository
) {
    suspend operator fun invoke() {
        dndRepository.toggle()
    }
}

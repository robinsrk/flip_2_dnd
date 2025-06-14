package dev.robin.dndfy.domain.usecase

import dev.robin.dndfy.domain.repository.DndRepository
import javax.inject.Inject

class ToggleDndUseCase @Inject constructor(
    private val dndRepository: DndRepository
) {
    suspend operator fun invoke() {
        dndRepository.toggleDnd()
    }
}

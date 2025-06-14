package dev.robin.dndfy.domain.usecase

import dev.robin.dndfy.domain.model.PhoneOrientation
import dev.robin.dndfy.domain.repository.OrientationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrientationUseCase @Inject constructor(
    private val orientationRepository: OrientationRepository
) {
    suspend operator fun invoke(): Flow<PhoneOrientation> {
        orientationRepository.startMonitoring()
        return orientationRepository.getOrientation()
    }
}

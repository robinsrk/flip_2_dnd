package dev.robin.flip_2_dnd.domain.usecase

import dev.robin.flip_2_dnd.domain.model.PhoneOrientation
import dev.robin.flip_2_dnd.domain.repository.OrientationRepository
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

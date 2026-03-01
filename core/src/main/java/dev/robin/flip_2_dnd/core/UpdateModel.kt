package dev.robin.flip_2_dnd.core

data class UpdateResponse(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val token: String
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val update: UpdateResponse) : UpdateState()
    object None : UpdateState()
    data class Error(val message: String) : UpdateState()
}

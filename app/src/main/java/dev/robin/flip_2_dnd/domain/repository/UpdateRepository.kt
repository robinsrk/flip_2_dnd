package dev.robin.flip_2_dnd.domain.repository

import dev.robin.flip_2_dnd.data.model.GitHubRelease

interface UpdateRepository {
    suspend fun checkForUpdates(): Result<GitHubRelease>
    fun getCurrentVersion(): String
    fun getGitHubReleaseUrl(): String
}

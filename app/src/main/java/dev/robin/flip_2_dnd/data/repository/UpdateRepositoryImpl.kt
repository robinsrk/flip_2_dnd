package dev.robin.flip_2_dnd.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dev.robin.flip_2_dnd.BuildConfig
import dev.robin.flip_2_dnd.data.api.GitHubApi
import dev.robin.flip_2_dnd.data.model.GitHubRelease
import dev.robin.flip_2_dnd.domain.repository.UpdateRepository
import java.net.UnknownHostException
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val context: Context,
    private val gitHubApi: GitHubApi
) : UpdateRepository {

    companion object {
        private const val GITHUB_OWNER = "robinsrk"
        private const val GITHUB_REPO = "Flip_2_DND"
        private const val TAG = "UpdateRepository"
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val v1Parts = v1.removePrefix("v").split(".")
        val v2Parts = v2.removePrefix("v").split(".")

        for (i in 0 until maxOf(v1Parts.size, v2Parts.size)) {
            val v1Part = v1Parts.getOrNull(i)?.toIntOrNull() ?: 0
            val v2Part = v2Parts.getOrNull(i)?.toIntOrNull() ?: 0
            
            when {
                v1Part > v2Part -> return 1
                v1Part < v2Part -> return -1
            }
        }
        return 0
    }

    override suspend fun checkForUpdates(): Result<GitHubRelease> {
        return try {
            if (!isNetworkAvailable()) {
                Log.e(TAG, "No network connection available")
                return Result.failure(Exception("No internet connection. Please check your network settings."))
            }

            Log.d(TAG, "Checking for updates...")
            try {
                val release = gitHubApi.getLatestRelease(GITHUB_OWNER, GITHUB_REPO)
                Log.d(TAG, "Got release: ${release.tagName}")
                
                if (!release.isDraft && !release.isPrerelease) {
                    Log.d(TAG, "Latest release: ${release.tagName}")
                    val currentVersion = getCurrentVersion()
                    Log.d(TAG, "Current version: $currentVersion")
                    
                    when {
                        compareVersions(release.tagName, currentVersion) > 0 -> {
                            Log.d(TAG, "New version available: ${release.tagName}")
                            release.message = "New version ${release.tagName} is available!"
                            Result.success(release)
                        }
                        compareVersions(release.tagName, currentVersion) < 0 -> {
                            Log.d(TAG, "Current version is newer than release")
                            release.message = "You're on a development version"
                            Result.success(release)
                        }
                        else -> {
                            Log.d(TAG, "Already on latest version")
                            release.message = "You're on the latest version"
                            Result.success(release)
                        }
                    }
                } else {
                    Log.e(TAG, "Release is either draft or prerelease")
                    Result.failure(Exception("No suitable release found"))
                }
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Unable to resolve host: api.github.com", e)
                Result.failure(Exception("Unable to connect to GitHub. Please check your internet connection."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            val errorMessage = when (e) {
                is UnknownHostException -> "Unable to connect to GitHub. Please check your internet connection."
                else -> e.message ?: "Unknown error occurred while checking for updates."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override fun getCurrentVersion(): String = BuildConfig.VERSION_NAME

    override fun getGitHubReleaseUrl(): String {
        return "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    }
}

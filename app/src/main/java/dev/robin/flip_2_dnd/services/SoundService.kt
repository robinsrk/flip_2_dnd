package dev.robin.flip_2_dnd.services

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import dev.robin.flip_2_dnd.data.repository.SettingsRepositoryImpl
import dev.robin.flip_2_dnd.presentation.settings.Sound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SoundService(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context)
    
    companion object {
        private const val TAG = "SoundService"
    }

    fun playDndSound(isEnabled: Boolean) {
        runBlocking {
            val isSoundEnabled = settingsRepository.getSoundEnabled().first()
            if (!isSoundEnabled) return@runBlocking

            try {
                // Release any existing MediaPlayer
                mediaPlayer?.release()

                // Get the appropriate sound based on DND state
                val sound = if (isEnabled) {
                    settingsRepository.getDndOnSound().first()
                } else {
                    settingsRepository.getDndOffSound().first()
                }

                // Don't play if NONE is selected
                if (sound == Sound.NONE) return@runBlocking

                // Create and play the new sound
                if (sound == Sound.CUSTOM) {
                    // For custom sounds, get the URI from settings
                    val uri = if (isEnabled) {
                        settingsRepository.getDndOnCustomSoundUri().first()
                    } else {
                        settingsRepository.getDndOffCustomSoundUri().first()
                    }

                    if (uri.isNullOrEmpty()) {
                        android.util.Log.d(TAG, "Custom sound selected but no URI available")
                        return@runBlocking
                    }
                    
                    try {
                        val parsedUri = android.net.Uri.parse(uri)
                        android.util.Log.d(TAG, "Attempting to play custom sound from URI: $parsedUri")
                        
                        // Check if we have permission for this URI
                        val persistedUriPermissions = context.contentResolver.persistedUriPermissions
                        val hasPermission = persistedUriPermissions.any { it.uri.toString() == parsedUri.toString() && it.isReadPermission }
                        
                        if (!hasPermission) {
                            android.util.Log.e(TAG, "No persisted permission for URI: $parsedUri")
                            // Try to take permission again as a recovery mechanism
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    parsedUri, 
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                                android.util.Log.d(TAG, "Successfully re-acquired permission for URI: $parsedUri")
                            } catch (e: SecurityException) {
                                android.util.Log.e(TAG, "Failed to re-acquire permission: ${e.message}", e)
                                // Continue anyway - we'll try to play the sound and see if it works
                                android.util.Log.d(TAG, "Attempting to play sound without explicit permission")
                            }
                        }
                        
                        // Validate the URI is accessible
                        try {
                            context.contentResolver.query(parsedUri, null, null, null, null)?.use { cursor ->
                                if (!cursor.moveToFirst()) {
                                    android.util.Log.e(TAG, "URI exists but content not accessible: $parsedUri")
                                }
                            } ?: run {
                                android.util.Log.e(TAG, "URI query returned null cursor: $parsedUri")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Error validating URI accessibility: ${e.message}", e)
                            // Continue anyway - we'll try the direct approach
                        }
                        
                        // Try to create the MediaPlayer with the URI
                        try {
                            // Create a new MediaPlayer instance
                            val player = MediaPlayer()
                            
                            // Set up the player with the URI
                            player.setDataSource(context, parsedUri)
                            player.prepare()
                            
                            // Assign to our class variable only after successful preparation
                            mediaPlayer = player
                            android.util.Log.d(TAG, "Successfully prepared MediaPlayer for custom sound")
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Error creating MediaPlayer: ${e.message}", e)
                            // If this fails, try an alternative approach with content resolver
                            try {
                                val fileDescriptor = context.contentResolver.openFileDescriptor(parsedUri, "r")?.fileDescriptor
                                if (fileDescriptor != null) {
                                    android.util.Log.d(TAG, "Using file descriptor approach for URI: $parsedUri")
                                    
                                    // Create a new MediaPlayer instance
                                    val player = MediaPlayer()
                                    
                                    // Set up the player with the file descriptor
                                    player.setDataSource(fileDescriptor)
                                    player.prepare()
                                    
                                    // Assign to our class variable only after successful preparation
                                    mediaPlayer = player
                                    android.util.Log.d(TAG, "Successfully prepared MediaPlayer using file descriptor")
                                } else {
                                    android.util.Log.e(TAG, "Failed to get file descriptor for URI: $parsedUri")
                                    return@runBlocking
                                }
                            } catch (e2: Exception) {
                                android.util.Log.e(TAG, "All approaches failed for URI: $parsedUri - ${e2.message}", e2)
                                return@runBlocking
                            }
                        }
                    } catch (e: SecurityException) {
                        android.util.Log.e(TAG, "Security exception accessing URI: ${e.message}", e)
                        return@runBlocking
                    } catch (e: IllegalStateException) {
                        android.util.Log.e(TAG, "IllegalStateException with MediaPlayer: ${e.message}", e)
                        return@runBlocking
                    } catch (e: IllegalArgumentException) {
                        android.util.Log.e(TAG, "IllegalArgumentException with URI: ${e.message}", e)
                        return@runBlocking
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Error setting up MediaPlayer for custom sound: ${e.message}", e)
                        return@runBlocking
                    }
                } else {
                    mediaPlayer = MediaPlayer.create(context, sound.soundResId)
                }
                
                // Set custom volume if enabled
                if (settingsRepository.getUseCustomVolume().first()) {
                    val volume = settingsRepository.getCustomVolume().first()
                    mediaPlayer?.setVolume(volume, volume)
                }

                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                mediaPlayer?.start()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error playing sound: ${e.message}", e)
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

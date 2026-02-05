package dev.robin.flip_2_dnd.presentation.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import dev.robin.flip_2_dnd.domain.repository.SettingsRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dev.robin.flip_2_dnd.R

@AndroidEntryPoint
class SoundPickerActivity : ComponentActivity() {
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    private val soundType: String by lazy {
        intent.getStringExtra(EXTRA_SOUND_TYPE) ?: DND_ON_SOUND
    }
    
    private val pickAudio = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Get the flags from the intent
                    val takeFlags = result.data?.flags?.and(Intent.FLAG_GRANT_READ_URI_PERMISSION) ?: Intent.FLAG_GRANT_READ_URI_PERMISSION
                    
                    // Log the URI and flags for debugging
                    android.util.Log.d(TAG, "Selected URI: $uri with flags: $takeFlags")
                    
                    // Check if the URI is valid and accessible
                    try {
                        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                android.util.Log.d(TAG, "URI is valid and accessible")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "URI is not accessible: ${e.message}", e)
                        showToast(getString(R.string.error_sound_not_accessible))
                        finish()
                        return@let
                    }
                    
                    // Take persistable permissions with the correct flags
                    try {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                        android.util.Log.d(TAG, "Requested persistable permission for URI: $uri")
                    } catch (e: SecurityException) {
                        android.util.Log.e(TAG, "Error taking persistable URI permission: ${e.message}", e)
                        showToast(getString(R.string.error_permission, e.localizedMessage ?: getString(R.string.error_could_not_get_permission)))
                        finish()
                        return@let
                    }
                    
                    // Verify we have the permission
                    val persistedUriPermissions = contentResolver.persistedUriPermissions
                    val hasPermission = persistedUriPermissions.any { it.uri.toString() == uri.toString() && it.isReadPermission }
                    
                    if (hasPermission) {
                        // Log success
                        android.util.Log.d(TAG, "Successfully took persistable permission for URI: $uri")
                        
                        // Save the URI to settings
                        saveUriToSettings(uri)
                    } else {
                        android.util.Log.e(TAG, "Failed to get persistable permission for URI: $uri")
                        showToast(getString(R.string.error_failed_to_get_permission))
                        finish()
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error processing selected audio: ${e.message}", e)
                    showToast(getString(R.string.error_processing_sound, e.localizedMessage ?: e.toString()))
                    finish()
                }
            } ?: run {
                android.util.Log.e(TAG, "No URI returned from picker")
                showToast(getString(R.string.error_no_sound_selected))
                finish()
            }
        } else {
            android.util.Log.d(TAG, "Sound picker cancelled or failed with result code: ${result.resultCode}")
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set an empty content view - we don't need a UI for this activity
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                // Empty content - no loading animation needed
            }
        }
        
        // Launch the audio picker only if this is the first time
        if (savedInstanceState == null) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                // Add flags to request persistable URI permissions
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            
            try {
                pickAudio.launch(intent)
            } catch (e: ActivityNotFoundException) {
                android.util.Log.e(TAG, "No activity found to handle audio picker", e)
                showToast(getString(R.string.error_opening_sound_picker))
                finish()
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun saveUriToSettings(uri: Uri) {
        val uriString = uri.toString()
        android.util.Log.d(TAG, "Saving URI: $uriString for sound type: $soundType")
        
        // Use lifecycleScope for coroutine
        lifecycleScope.launch {
            try {
                if (soundType == DND_ON_SOUND) {
                    settingsRepository.setDndOnCustomSoundUri(uriString)
                    settingsRepository.setDndOnSound(Sound.CUSTOM)
                    android.util.Log.d(TAG, "Successfully saved DND ON custom sound URI and set sound type to CUSTOM")
                    showToast(getString(R.string.dnd_on_sound_saved))
                } else {
                    settingsRepository.setDndOffCustomSoundUri(uriString)
                    settingsRepository.setDndOffSound(Sound.CUSTOM)
                    android.util.Log.d(TAG, "Successfully saved DND OFF custom sound URI and set sound type to CUSTOM")
                    showToast(getString(R.string.dnd_off_sound_saved))
                }
                
                // Verify the sound can be played in a background thread
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    verifySound(uri)
                }
                
                // Finish the activity to return to the previous screen
                finish()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error saving URI to settings: ${e.message}", e)
                showToast(getString(R.string.error_saving_sound))
                finish()
            }
        }
    }
    
    private fun verifySound(uri: Uri) {
        try {
            // Test that we can create a MediaPlayer with this URI
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                prepare() // Synchronous prepare to verify it works
                release() // Release immediately after testing
            }
            android.util.Log.d(TAG, "Successfully verified sound playback for URI: $uri")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error verifying sound playback: ${e.message}", e)
            // Don't show toast here as we've already saved the URI, just log the error
        }
    }
    
    companion object {
        private const val TAG = "SoundPickerActivity"
        const val EXTRA_SOUND_TYPE = "sound_type"
        const val DND_ON_SOUND = "dnd_on_sound"
        const val DND_OFF_SOUND = "dnd_off_sound"
    }
}
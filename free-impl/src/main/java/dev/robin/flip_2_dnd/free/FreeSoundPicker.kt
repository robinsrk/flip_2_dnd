package dev.robin.flip_2_dnd.free

import android.content.Context
import android.widget.Toast
import dev.robin.flip_2_dnd.core.SoundPicker

class FreeSoundPicker(private val context: Context) : SoundPicker {
    override fun launchPicker(context: Context, isDndOn: Boolean) {
        // No-op for free version
        Toast.makeText(context, "Pro feature required", Toast.LENGTH_SHORT).show()
    }
}

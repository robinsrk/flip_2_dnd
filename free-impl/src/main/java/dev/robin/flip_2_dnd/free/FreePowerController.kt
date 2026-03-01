package dev.robin.flip_2_dnd.free

import android.content.Context
import dev.robin.flip_2_dnd.core.PowerController

class FreePowerController(context: Context) : PowerController {
    override fun setBatterySaverEnabled(enabled: Boolean) {
        // No-Op in Free version
    }
}

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun copyAddressToClipboard(context: Context, address: String) {
	val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	val clip = ClipData.newPlainText("Crypto Address", address)
	clipboardManager.setPrimaryClip(clip)
	Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
}
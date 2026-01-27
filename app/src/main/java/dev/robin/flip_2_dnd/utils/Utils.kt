package dev.robin.flip_2_dnd.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast

fun copyAddressToClipboard(context: Context, address: String) {
	val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	val clip = ClipData.newPlainText("Crypto Address", address)
	clipboardManager.setPrimaryClip(clip)
	Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    try {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("Utils", "Error getting file name from URI: ${e.message}")
    }
    
    if (fileName == null) {
        try {
            fileName = uri.path
            val cut = fileName?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                fileName = fileName?.substring(cut + 1)
            }
        } catch (e: Exception) {
            android.util.Log.e("Utils", "Error parsing URI path: ${e.message}")
        }
    }
    return fileName
}
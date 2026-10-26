package com.puneetfsn.kutumbcare.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Document photos (Aadhaar, PAN, passbook, FD receipt…) live as compressed
 * JPEGs in the app-private documents folder — never uploaded anywhere.
 * The database only keeps file names ([ChecklistItem.attachmentsJson]).
 */
object DocumentStore {

    fun dir(context: Context): File = File(context.filesDir, "documents").apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Copies a picked image in, downscaled to 1400px / JPEG 78. Returns the stored name. */
    fun importImage(context: Context, uri: Uri): String? = runCatching {
        val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null
        val scale = minOf(1f, 1400f / maxOf(source.width, source.height))
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true)
        } else source
        val name = "doc_${UUID.randomUUID().toString().take(12)}.jpg"
        FileOutputStream(file(context, name)).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 78, out) }
        name
    }.getOrNull()

    fun writeBytes(context: Context, bytes: ByteArray): String {
        val name = "doc_${UUID.randomUUID().toString().take(12)}.jpg"
        file(context, name).writeBytes(bytes)
        return name
    }

    fun readBytes(context: Context, name: String): ByteArray? =
        runCatching { file(context, name).readBytes() }.getOrNull()

    fun delete(context: Context, name: String) {
        runCatching { file(context, name).delete() }
    }

    /** Small preview bitmap for the editor thumbnails. */
    fun preview(context: Context, name: String, maxPx: Int = 240): Bitmap? = runCatching {
        val f = file(context, name)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxPx || bounds.outHeight / sample > maxPx) sample *= 2
        BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()
}

package com.puneetfsn.kutumbcare.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Document photos (Aadhaar, PAN, passbook, FD receipt…) live as compressed
 * JPEGs in the app-private documents folder — never uploaded anywhere.
 * The database only keeps file names ([ChecklistItem.attachmentsJson]).
 *
 * Every decode here is SAMPLED: a 50 MP camera photo decoded at full size
 * needs ~200 MB and simply returns null on many phones, which is how
 * "that photo could not be read" happens. We read the bounds first and
 * decode only as many pixels as the caller needs.
 */
object DocumentStore {

    /** Longest edge we ever keep for a stored document photo. */
    private const val STORAGE_MAX_PX = 1600

    /** Longest edge handed to the on-device text recognizer. */
    private const val OCR_MAX_PX = 2200

    fun dir(context: Context): File = File(context.filesDir, "documents").apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Copies a picked image in, downscaled to 1600px / JPEG 78. Returns the stored name. */
    fun importImage(context: Context, uri: Uri): String? = runCatching {
        val bitmap = decodeForStorage(context, uri) ?: return null
        val name = "doc_${UUID.randomUUID().toString().take(12)}.jpg"
        val buffer = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 78, buffer)
        file(context, name).writeBytes(VaultCrypto.encryptBytes(buffer.toByteArray()))
        name
    }.getOrNull()

    /**
     * Sampled decode of a picked photo, with the camera's EXIF rotation
     * applied so the stored copy is upright (text recognizers cannot read
     * sideways documents). Tries the stream twice (bounds, then pixels) and
     * falls back to the file descriptor, which some phones' galleries need.
     */
    private fun decodeForStorage(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        }
        val sample = ImageSampling.sampleSizeFor(bounds.outWidth, bounds.outHeight, STORAGE_MAX_PX)
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = runCatching {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }.getOrNull()
        if (bitmap == null) {
            bitmap = runCatching {
                resolver.openFileDescriptor(uri, "r")?.use {
                    BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, opts)
                }
            }.getOrNull()
        }
        if (bitmap == null) return null
        val scale = minOf(1f, STORAGE_MAX_PX.toFloat() / maxOf(bitmap.width, bitmap.height))
        var out = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else bitmap
        val degrees = runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                when (
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        }.getOrDefault(0)
        if (degrees != 0) {
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            out = Bitmap.createBitmap(out, 0, 0, out.width, out.height, matrix, true)
        }
        return out
    }

    fun writeBytes(context: Context, bytes: ByteArray): String {
        val name = "doc_${UUID.randomUUID().toString().take(12)}.jpg"
        file(context, name).writeBytes(VaultCrypto.encryptBytes(bytes))
        return name
    }

    /** Decrypted JPEG bytes (for PDF rendering). */
    fun readBytes(context: Context, name: String): ByteArray? =
        runCatching { VaultCrypto.decryptBytes(file(context, name).readBytes()) }.getOrNull()

    private fun rawBytes(context: Context, name: String): ByteArray? =
        runCatching { file(context, name).readBytes() }.getOrNull()

    /** Sampled decode of stored bytes; null when the bytes are not an image. */
    private fun decodeSampled(bytes: ByteArray, maxPx: Int): Bitmap? {
        if (bytes.isEmpty()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = ImageSampling.sampleSizeFor(bounds.outWidth, bounds.outHeight, maxPx)
        }
        return runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) }.getOrNull()
    }

    /**
     * Decrypted bitmap for the dossier PDF, with fallbacks: full decode,
     * then sampled decode, then the raw file (photos stored before
     * encryption existed, or if decryption ever fails open).
     */
    fun loadBitmap(context: Context, name: String): Bitmap? {
        val bytes = readBytes(context, name)
        if (bytes != null) {
            runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                .getOrNull()?.let { return it }
            decodeSampled(bytes, 2000)?.let { return it }
        }
        val raw = rawBytes(context, name)
        if (raw != null && !(bytes != null && raw.contentEquals(bytes))) {
            runCatching { BitmapFactory.decodeByteArray(raw, 0, raw.size) }
                .getOrNull()?.let { return it }
            decodeSampled(raw, 2000)?.let { return it }
        }
        return null
    }

    /**
     * Bitmap sized for on-device text recognition (max 2200px): large
     * enough for ML Kit, small enough to decode reliably on any phone.
     */
    fun loadForRecognition(context: Context, name: String): Bitmap? {
        readBytes(context, name)?.let { bytes ->
            decodeSampled(bytes, OCR_MAX_PX)?.let { return it }
            runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                .getOrNull()?.let { return it }
        }
        rawBytes(context, name)?.let { raw -> decodeSampled(raw, OCR_MAX_PX)?.let { return it } }
        return null
    }

    /** Deletes every stored document photo (Erase everything). */
    fun deleteAll(context: Context) {
        runCatching { dir(context).listFiles()?.forEach { it.delete() } }
    }

    fun delete(context: Context, name: String) {
        runCatching { file(context, name).delete() }
    }

    /** Small preview bitmap for the editor thumbnails. */
    fun preview(context: Context, name: String, maxPx: Int = 240): Bitmap? = runCatching {
        val bytes = readBytes(context, name) ?: return null
        decodeSampled(bytes, maxPx)
    }.getOrNull()
}

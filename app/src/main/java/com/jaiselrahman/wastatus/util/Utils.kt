package com.jaiselrahman.wastatus.util


import android.content.Context
import android.content.Intent
import android.net.Uri
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat


object Utils {
    fun getConvertedFile(folder: String, fileName: String): File {
        val f = File(folder)

        if (!f.exists())
            f.mkdirs()

        return File(f.path + File.separator + fileName)
    }

    fun writeResponseToFile(
        responseBody: ResponseBody,
        dest: File,
        progressListener: ProgressListener
    ) {
        var percentage = 0
        var fileSizeDownloaded: Long = 0
        val fileSize = responseBody.contentLength()
        val bytes = ByteArray(4096)

        responseBody.byteStream().use { inputStream ->
            FileOutputStream(dest).use { outputStream ->
                while (true) {
                    val read = inputStream.read(bytes)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(bytes, 0, read)
                    fileSizeDownloaded += read.toLong()
                    val currentPercentage = (fileSizeDownloaded * 100f / fileSize).toInt()
                    if (currentPercentage > percentage) {
                        percentage = currentPercentage
                        progressListener(currentPercentage)
                    }
                }
                outputStream.flush()
                return
            }
        }
    }


    fun refreshGallery(path: String, context: Context) {
        val file = File(path)
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

internal typealias ProgressListener = (progress: Int) -> Unit

fun Long?.compactString(): String {
    if (this == null) return "0"
    if (this < 1000) return toString()
    val exp = (Math.log10(toDouble()) / Math.log10(1000.0)).toInt()
    val value = this / Math.pow(1000.0, exp.toDouble())
    return decimalFormat.format(value) + "kMGTPE"[exp - 1]
}

private val decimalFormat by lazy {
    val decimalFormat = DecimalFormat("#,##0.#")
    decimalFormat.roundingMode = RoundingMode.FLOOR
    decimalFormat
}

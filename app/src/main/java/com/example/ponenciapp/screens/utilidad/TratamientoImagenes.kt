package com.example.ponenciapp.screens.utilidad

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ponenciapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun generarFirma(params: Map<String, String>, apiSecret: String): String {
    // Ordenar los parámetros por key
    val sorted = params.toSortedMap()
    val toSign = sorted.entries.joinToString("&") { "${it.key}=${it.value}" }
    val signature = MessageDigest.getInstance("SHA-1")
        .digest("$toSign$apiSecret".toByteArray())
        .joinToString("") { "%02x".format(it) }
    return signature
}

// Función para subir foto de perfil a Cloudinary
fun subirImagenCloudinary(
    context: Context,
    imageUri: Uri,
    uid: String,
    onSuccess: (String) -> Unit
) {
    val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    val apiKey = BuildConfig.CLOUDINARY_API_KEY
    val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

    val timestamp = (System.currentTimeMillis() / 1000).toString()
    val folder = "profile_pictures"
    val publicId = uid

    val params = mapOf(
        "folder" to folder,
        "public_id" to publicId,
        "overwrite" to "true",
        "timestamp" to timestamp
    )

    val signature = generarFirma(params, apiSecret)

    MediaManager.get().upload(imageUri)
        .option("folder", folder)
        .option("public_id", publicId)
        .option("overwrite", true)
        .option("timestamp", timestamp.toLong())
        .option("api_key", apiKey)
        .option("signature", signature)
        .option("cloud_name", cloudName)
        .callback(object : UploadCallback {
            override fun onStart(requestId: String?) {}
            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                val url = resultData?.get("secure_url") as? String ?: return
                onSuccess(url)
                Toast.makeText(context, "Imagen actualizada correctamente", Toast.LENGTH_SHORT).show()
            }
            override fun onError(requestId: String?, error: ErrorInfo?) {
                Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        })
        .dispatch()
}

suspend fun getFotoMicrosoft(accessToken: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://graph.microsoft.com/v1.0/me/photo/\$value")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.connect()

            if (conn.responseCode == 200) {
                conn.inputStream.readBytes()
            } else {
                Log.d("MicrosoftPhoto", "Error HTTP: ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.d("MicrosoftPhoto", "Error: ${e.message}")
            null
        }
    }
}

// Función para guardar bytes en un archivo temporal
fun saveBytesToTempUri(context: Context, bytes: ByteArray, filename: String): Uri {
    val file = File(context.cacheDir, filename)
    FileOutputStream(file).use { it.write(bytes) }
    return Uri.fromFile(file)
}
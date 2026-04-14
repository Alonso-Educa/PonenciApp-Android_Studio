package com.example.ponenciapp.screens.utilidad

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

// Función para generar un QR a partir de un contenido
fun generarQRBitmap(contenido: String, tamano: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano, hints)
    val bitmap = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.RGB_565)
    for (x in 0 until tamano) {
        for (y in 0 until tamano) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
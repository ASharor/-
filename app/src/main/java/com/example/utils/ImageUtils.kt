package com.example.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun compressBitmapToBase64(bitmap: Bitmap, maxDimension: Int = 300, quality: Int = 70): String {
        var width = bitmap.width
        var height = bitmap.height

        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            if (ratio > 1) {
                width = maxDimension
                height = (maxDimension / ratio).toInt()
            } else {
                height = maxDimension
                width = (maxDimension * ratio).toInt()
            }
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val cleanBase64 = if (base64Str.contains(",")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}

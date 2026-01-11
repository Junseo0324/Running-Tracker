package com.devhjs.runningtracker.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * 바이트 배열(ByteArray) 데이터를 비트맵(Bitmap) 이미지로 변환합니다.
     * 데이터베이스 등에서 불러온 이미지 데이터를 표시할 때 사용합니다.
     */
    fun bytesToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * 비트맵(Bitmap) 이미지를 바이트 배열(ByteArray)로 변환합니다.
     * 이미지를 데이터베이스에 저장하거나 전송하기 위해 직렬화할 때 사용합니다.
     */
    fun bitmapToBytes(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}

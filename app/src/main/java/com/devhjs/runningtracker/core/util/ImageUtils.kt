package com.devhjs.runningtracker.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * PNG 바이트를 표시 크기에 맞춰 축소 디코딩합니다.
     *
     * 원본을 그대로 디코딩하면 800x800 ARGB_8888 기준 약 2.5MB 가 잡히는데,
     * 목록의 썸네일은 100dp 남짓이라 대부분이 낭비입니다. 두 단계로 줄입니다.
     *
     * - [BitmapFactory.Options.inSampleSize] 로 요청 크기까지 축소
     * - 경로 이미지는 검정 배경 + 단색 선이라 알파가 필요 없으므로 RGB_565 사용
     *   (픽셀당 4바이트 -> 2바이트)
     *
     * @return 디코딩된 비트맵. 바이트가 유효한 이미지가 아니면 null.
     */
    fun decodeSampledBitmap(bytes: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        // 1) 실제 픽셀은 읽지 않고 크기만 확인한다.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // 2) 축소 비율을 정해 실제로 디코딩한다.
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(
                width = bounds.outWidth,
                height = bounds.outHeight,
                reqWidth = reqWidth,
                reqHeight = reqHeight
            )
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    /**
     * 요청 크기보다 작아지지 않는 선에서 가장 큰 축소 비율(2의 거듭제곱)을 구합니다.
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        if (reqWidth <= 0 || reqHeight <= 0) return 1

        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 비트맵을 PNG 바이트로 압축합니다. DB 저장용입니다.
     */
    fun bitmapToBytes(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}

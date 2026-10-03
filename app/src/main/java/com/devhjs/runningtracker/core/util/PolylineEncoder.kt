package com.devhjs.runningtracker.core.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToLong

/**
 * Google 의 Encoded Polyline Algorithm 으로 경로를 문자열로 압축/복원한다.
 * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 *
 * 좌표를 소수점 5자리(약 1m)로 반올림해 직전 좌표와의 차이만 가변 길이로 기록하므로
 * 좌표 하나가 보통 4~6자다. (JSON 으로 저장하면 40자 안팎)
 */
object PolylineEncoder {

    /** 구간 구분자. 인코딩 결과는 '?'(63) ~ '~'(126) 범위의 문자만 쓰므로 공백과 겹치지 않는다. */
    private const val SEGMENT_SEPARATOR = " "

    /** 일시정지로 끊긴 여러 구간을 하나의 문자열로 만든다. 빈 구간은 버린다. */
    fun encodePath(path: List<List<LatLng>>): String =
        path.filter { it.isNotEmpty() }.joinToString(SEGMENT_SEPARATOR) { encode(it) }

    fun decodePath(encoded: String): List<List<LatLng>> =
        encoded.split(SEGMENT_SEPARATOR).filter { it.isNotEmpty() }.map { decode(it) }

    fun encode(points: List<LatLng>): String {
        val result = StringBuilder()
        var prevLat = 0L
        var prevLng = 0L
        for (point in points) {
            val lat = (point.latitude * 1e5).roundToLong()
            val lng = (point.longitude * 1e5).roundToLong()
            encodeValue(lat - prevLat, result)
            encodeValue(lng - prevLng, result)
            prevLat = lat
            prevLng = lng
        }
        return result.toString()
    }

    fun decode(encoded: String): List<LatLng> {
        val points = mutableListOf<LatLng>()
        var index = 0
        var lat = 0L
        var lng = 0L
        while (index < encoded.length) {
            val (dLat, nextIndex) = decodeValue(encoded, index)
            val (dLng, afterLng) = decodeValue(encoded, nextIndex)
            index = afterLng
            lat += dLat
            lng += dLng
            points.add(LatLng(lat / 1e5, lng / 1e5))
        }
        return points
    }

    private fun encodeValue(value: Long, out: StringBuilder) {
        var v = if (value < 0) (value shl 1).inv() else value shl 1
        while (v >= 0x20) {
            out.append(((0x20 or (v and 0x1f).toInt()) + 63).toChar())
            v = v shr 5
        }
        out.append((v + 63).toInt().toChar())
    }

    /** @return 복원한 값과 다음 읽을 위치 */
    private fun decodeValue(encoded: String, start: Int): Pair<Long, Int> {
        var index = start
        var result = 0L
        var shift = 0
        var b: Int
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f).toLong() shl shift)
            shift += 5
        } while (b >= 0x20)
        val value = if (result and 1L != 0L) (result shr 1).inv() else result shr 1
        return value to index
    }
}

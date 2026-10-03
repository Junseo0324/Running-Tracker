package com.devhjs.runningtracker.presentation.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 기록 숫자(거리 등)는 앱 언어와 상관없이 일반 숫자(0-9)와 '.' 소수점으로 표시한다.
 * 기본 로케일로 포맷하면 아랍어에서 5.26 이 ٥٫٢٦ 로 바뀌어 페이스 · 시간 표기와 섞인다.
 */
fun formatDistanceKm(distanceInMeters: Float, fractionDigits: Int): String =
    String.format(Locale.US, "%.${fractionDigits}f", distanceInMeters / 1000f)

/**
 * 기록 날짜를 언어별 표준 순서로 표시한다. (ko: 10월 3일 오후 8:49, en: Oct 3, 8:49 PM, ja: 10月3日 20:49)
 * 숫자는 거리 표기와 맞춰 일반 숫자로 고정한다.
 */
fun formatRunDate(timestamp: Long, locale: Locale, withYear: Boolean): String {
    val skeleton = if (withYear) "yMMMd jmm" else "MMMd jmm"
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    val latinDigitsLocale = Locale.Builder()
        .setLocale(locale)
        .setUnicodeLocaleKeyword("nu", "latn")
        .build()
    return SimpleDateFormat(pattern, latinDigitsLocale).format(timestamp)
}

/**
 * 페이스(6'31")처럼 기호가 섞인 값을 왼쪽→오른쪽으로 고정한다.
 * 아랍어 같은 RTL 화면에서는 ' 와 " 가 앞뒤로 재배치되어 "31'6 처럼 보인다.
 * 유니코드 LTR 격리 문자(LRI ... PDI)로 감싸 주변 방향과 상관없이 그대로 보이게 한다.
 */
fun ltrIsolate(text: String): String = "\u2066$text\u2069"

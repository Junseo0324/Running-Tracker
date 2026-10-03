package com.devhjs.runningtracker.presentation.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import timber.log.Timber

/**
 * 외부 브라우저(또는 쿠팡 앱)로 링크를 연다.
 * 앱 내 WebView 로 열면 제휴 쿠키가 제대로 잡히지 않을 수 있어 외부로 넘긴다.
 */
fun Context.openExternalUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "링크를 열 수 있는 앱이 없습니다: $url")
    }
}

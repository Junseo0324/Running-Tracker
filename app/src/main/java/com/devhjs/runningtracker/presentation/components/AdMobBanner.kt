package com.devhjs.runningtracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.devhjs.runningtracker.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * 배너 광고.
 *
 * [AdView] 는 내부적으로 WebView 를 들고 있고 자체 새로고침 타이머를 돌린다.
 * destroy() 를 호출하지 않으면 화면을 벗어난 뒤에도 WebView 와 그 객체 그래프가
 * 계속 살아있고 광고를 계속 받아온다. 화면을 오갈 때마다 하나씩 쌓이므로
 * 컴포지션에서 벗어날 때 반드시 정리해야 한다.
 *
 * 또한 앱이 백그라운드로 내려가면 새로고침을 멈추도록 라이프사이클에 연결한다.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adId: String = BuildConfig.ADMOB_BANNER_ID
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 리컴포지션마다 새로 만들지 않도록 한 번만 생성한다.
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = adId
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { adView }
    )
}

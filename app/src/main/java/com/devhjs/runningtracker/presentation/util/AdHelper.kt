package com.devhjs.runningtracker.presentation.util

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devhjs.runningtracker.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.first
import timber.log.Timber

val Context.dataStore by preferencesDataStore(name = "ad_prefs_datastore")

object AdHelper {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    /**
     * 전면 광고를 미리 불러온다.
     *
     * 로드된 [InterstitialAd] 는 static 필드에 보관되므로, Activity 로 로드하면
     * 광고가 표시될 때까지(끝내 표시되지 않으면 영원히) 그 Activity 가 붙잡힌다.
     * 표시에는 Activity 가 필요하지만 로드에는 필요 없으므로 applicationContext 를 쓴다.
     */
    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.e("Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.d("Ad loaded successfully")
                    interstitialAd = ad
                    isAdLoading = false
                }
            }
        )
    }

    fun showInterstitial(context: Context, onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null && context is Activity) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Timber.d("Ad dismissed")
                    ad.fullScreenContentCallback = null
                    interstitialAd = null
                    onAdDismissed()
                    // 다음 광고는 Activity 가 아닌 application context 로 미리 받아둔다.
                    loadInterstitial(context.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Timber.e("Ad failed to show: ${p0.message}")
                    ad.fullScreenContentCallback = null
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            ad.show(context)
        } else {
            // If ad is not ready or context is not Activity, just proceed
             Timber.d("Ad not ready or context is not Activity. Ad ready: ${interstitialAd != null}")
             // If ad was null, try loading it for next time
             if(interstitialAd == null) loadInterstitial(context)
             onAdDismissed()
        }
    }

    private val HISTORY_AD_COUNT_KEY = intPreferencesKey("history_ad_count")

    suspend fun showInterstitialForHistory(context: Context, frequency: Int = 3, onAdDismissed: () -> Unit = {}) {
        val prefs = context.dataStore.data.first()
        val currentCount = (prefs[HISTORY_AD_COUNT_KEY] ?: 0) + 1
        
        context.dataStore.edit { settings ->
            settings[HISTORY_AD_COUNT_KEY] = currentCount
        }

        if (currentCount % frequency == 0) {
            showInterstitial(context, onAdDismissed)
        } else {
            Timber.d("Ad skipped due to frequency counter ($currentCount / $frequency)")
            if (interstitialAd == null) loadInterstitial(context)
            onAdDismissed()
        }
    }
}

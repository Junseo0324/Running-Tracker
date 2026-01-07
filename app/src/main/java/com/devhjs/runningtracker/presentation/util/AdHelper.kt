package com.devhjs.runningtracker.presentation.util

import android.app.Activity
import android.content.Context
import com.devhjs.runningtracker.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import timber.log.Timber

object AdHelper {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
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
        if (interstitialAd != null && context is Activity) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Timber.d("Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    loadInterstitial(context) // Preload next ad
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Timber.e("Ad failed to show: ${p0.message}")
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(context)
        } else {
            // If ad is not ready or context is not Activity, just proceed
             Timber.d("Ad not ready or context is not Activity. Ad ready: ${interstitialAd != null}")
             // If ad was null, try loading it for next time
             if(interstitialAd == null) loadInterstitial(context)
             onAdDismissed()
        }
    }
}

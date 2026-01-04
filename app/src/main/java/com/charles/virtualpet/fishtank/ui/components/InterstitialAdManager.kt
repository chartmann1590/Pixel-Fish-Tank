package com.charles.virtualpet.fishtank.ui.components

import android.content.Context
import android.util.Log
import com.charles.virtualpet.fishtank.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(
    private val context: Context,
    private val onAdFailedToLoad: ((String) -> Unit)? = null
) {
    private var interstitialAd: InterstitialAd? = null
    private var onAdDismissedCallback: (() -> Unit)? = null
    private val adUnitId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
    
    companion object {
        private const val TAG = "InterstitialAd"
    }
    
    fun loadAd() {
        if (interstitialAd != null) {
            return // Ad already loaded
        }
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            interstitialAd = null
                            // Call the callback if set
                            onAdDismissedCallback?.invoke()
                            onAdDismissedCallback = null
                            // Load next ad
                            loadAd()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                            Log.e(TAG, "Ad failed to show: ${p0.message}")
                            interstitialAd = null
                            // Call the callback even if ad failed to show
                            onAdDismissedCallback?.invoke()
                            onAdDismissedCallback = null
                            onAdFailedToLoad?.invoke(p0.message)
                            // Load next ad
                            loadAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed full screen content")
                        }
                    }
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    onAdFailedToLoad?.invoke(loadAdError.message)
                }
            }
        )
    }
    
    fun showAd(onDismissed: () -> Unit = {}) {
        interstitialAd?.let { ad ->
            onAdDismissedCallback = onDismissed
            ad.show(context as android.app.Activity)
        } ?: run {
            Log.w(TAG, "Ad not loaded, continuing without showing")
            // If ad is not ready, just continue without showing
            onDismissed()
        }
    }
    
    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }
}


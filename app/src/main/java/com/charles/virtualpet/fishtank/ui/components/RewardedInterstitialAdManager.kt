package com.charles.virtualpet.fishtank.ui.components

import android.content.Context
import android.util.Log
import com.charles.virtualpet.fishtank.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class RewardedInterstitialAdManager(
    private val context: Context,
    private val onRewardEarned: () -> Unit,
    private val onAdFailedToLoad: (String) -> Unit,
    private val onAdDismissed: () -> Unit
) {
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private val adUnitId = BuildConfig.ADMOB_REWARDED_INTERSTITIAL_AD_UNIT_ID
    
    companion object {
        private const val TAG = "RewardedInterstitialAd"
    }
    
    fun loadAd() {
        if (rewardedInterstitialAd != null) {
            return // Ad already loaded
        }
        
        val adRequest = AdRequest.Builder().build()
        
        RewardedInterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded interstitial ad loaded")
                    rewardedInterstitialAd = ad
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            rewardedInterstitialAd = null
                            onAdDismissed()
                            // Load next ad
                            loadAd()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                            Log.e(TAG, "Ad failed to show: ${p0.message}")
                            rewardedInterstitialAd = null
                            onAdFailedToLoad(p0.message)
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
                    rewardedInterstitialAd = null
                    onAdFailedToLoad(loadAdError.message)
                }
            }
        )
    }
    
    fun showAd() {
        rewardedInterstitialAd?.let { ad ->
            ad.show(context as android.app.Activity) { rewardItem ->
                // User earned reward
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } ?: run {
            Log.w(TAG, "Ad not loaded, attempting to load")
            loadAd()
            onAdFailedToLoad("Ad not ready. Please wait a moment and try again.")
        }
    }
    
    fun isAdLoaded(): Boolean {
        return rewardedInterstitialAd != null
    }
}


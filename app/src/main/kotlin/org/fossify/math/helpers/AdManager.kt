package org.fossify.math.helpers

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import org.fossify.math.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "CalcovaAdManager"
    private const val INTERSTITIAL_COOLDOWN_MS = 120_000L

    private val isMobileAdsInitialized = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var lastInterstitialShownAt = 0L
    private var navigationActionCount = 0

    fun requestConsentAndInit(activity: Activity, onAdsReady: () -> Unit = {}) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "UMP consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds() || BuildConfig.DEBUG) {
                        initializeMobileAds(activity.applicationContext, onAdsReady)
                    }
                }
            },
            { requestError ->
                Log.w(TAG, "UMP consent info update failed: ${requestError.message}")
                if (consentInformation.canRequestAds() || BuildConfig.DEBUG ||
                    consentInformation.consentStatus == ConsentInformation.ConsentStatus.NOT_REQUIRED ||
                    consentInformation.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN
                ) {
                    initializeMobileAds(activity.applicationContext, onAdsReady)
                }
            }
        )

        if (consentInformation.canRequestAds()) {
            initializeMobileAds(activity.applicationContext, onAdsReady)
        }
    }

    fun isPrivacyOptionsRequired(context: Context): Boolean {
        val consentInfo = UserMessagingPlatform.getConsentInformation(context)
        return consentInfo.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Privacy options form error: ${formError.message}")
            }
            onDismissed()
        }
    }

    private fun initializeMobileAds(context: Context, onReady: () -> Unit) {
        if (isMobileAdsInitialized.compareAndSet(false, true)) {
            MobileAds.initialize(context) {
                preloadInterstitial(context)
                onReady()
            }
        } else {
            preloadInterstitial(context)
            onReady()
        }
    }

    fun loadAdaptiveBanner(activity: Activity, container: FrameLayout): AdView? {
        if (!isMobileAdsInitialized.get()) {
            val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
            if (!consentInfo.canRequestAds() && !BuildConfig.DEBUG) {
                return null
            }
        }

        container.removeAllViews()
        val adView = AdView(activity).apply {
            adUnitId = BuildConfig.ADMOB_BANNER_ID
            setAdSize(getAdaptiveBannerSize(activity, container))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
        return adView
    }

    private fun getAdaptiveBannerSize(activity: Activity, container: FrameLayout): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = if (container.width > 0) {
            container.width.toFloat()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.windowManager.currentWindowMetrics.bounds.width().toFloat()
        } else {
            displayMetrics.widthPixels.toFloat()
        }
        val density = displayMetrics.density
        val adWidthDp = (adWidthPixels / density).toInt().coerceAtLeast(320)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidthDp)
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return
        if (!isMobileAdsInitialized.get() && !BuildConfig.DEBUG) return

        isLoadingInterstitial = true
        InterstitialAd.load(
            context.applicationContext,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoadingInterstitial = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingInterstitial = false
                    interstitialAd = null
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun maybeShowInterstitial(activity: Activity, onContinue: () -> Unit) {
        navigationActionCount++
        val now = System.currentTimeMillis()
        val ad = interstitialAd
        val withinCooldown = (now - lastInterstitialShownAt) < INTERSTITIAL_COOLDOWN_MS

        if (ad == null || navigationActionCount < 2 || withinCooldown) {
            preloadInterstitial(activity.applicationContext)
            onContinue()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastInterstitialShownAt = System.currentTimeMillis()
                preloadInterstitial(activity.applicationContext)
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                preloadInterstitial(activity.applicationContext)
                onContinue()
            }
        }
        ad.show(activity)
    }
}

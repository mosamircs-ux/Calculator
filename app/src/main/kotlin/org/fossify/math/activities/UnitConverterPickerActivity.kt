package org.fossify.math.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.google.android.gms.ads.AdView
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.views.AutoGridLayoutManager
import org.fossify.math.R
import org.fossify.math.adapters.UnitTypesAdapter
import org.fossify.math.databinding.ActivityUnitConverterPickerBinding
import org.fossify.math.extensions.config
import org.fossify.math.helpers.AdManager
import org.fossify.math.helpers.CalcovaThemeManager
import org.fossify.math.helpers.converters.Converter

class UnitConverterPickerActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityUnitConverterPickerBinding::inflate)
    private var bannerAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupEdgeToEdge(
            padTopSystem = listOf(binding.unitConverterPickerAppbar),
            padBottomSystem = listOf(binding.adBannerContainer)
        )

        binding.unitTypesGrid.layoutManager =
            AutoGridLayoutManager(this, resources.getDimensionPixelSize(R.dimen.unit_type_size))
        binding.unitTypesGrid.adapter = UnitTypesAdapter(this, Converter.ALL) {
            Intent(this, UnitConverterActivity::class.java).apply {
                putExtra(UnitConverterActivity.EXTRA_CONVERTER_ID, it)
                startActivity(this)
            }
        }

        binding.unitConverterPickerToolbar.setTitle(R.string.unit_converter)
        bannerAdView = AdManager.loadAdaptiveBanner(this, binding.adBannerContainer)
    }

    override fun onResume() {
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        super.onResume()

        setupTopAppBar(binding.unitConverterPickerAppbar, NavigationIcon.Arrow)
        CalcovaThemeManager.applyWindowStyling(this, binding.root)
        binding.unitConverterPickerAppbar.setBackgroundColor(CalcovaThemeManager.bgColor(this))
        CalcovaThemeManager.styleToolbar(this, binding.unitConverterPickerToolbar)

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        bannerAdView?.resume()
    }

    override fun onPause() {
        bannerAdView?.pause()
        super.onPause()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        bannerAdView = null
        super.onDestroy()
    }
}

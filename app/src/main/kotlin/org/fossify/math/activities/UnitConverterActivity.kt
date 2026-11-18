package org.fossify.math.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdView
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.performHapticFeedback
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.math.R
import org.fossify.math.databinding.ActivityUnitConverterBinding
import org.fossify.math.extensions.config
import org.fossify.math.helpers.AdManager
import org.fossify.math.helpers.CONVERTER_STATE
import org.fossify.math.helpers.CalcovaThemeManager
import org.fossify.math.helpers.converters.Converter
import org.fossify.math.helpers.converters.TemperatureConverter
import org.fossify.math.helpers.getDecimalSeparator
import org.fossify.math.views.ConverterView

class UnitConverterActivity : SimpleActivity(), ConverterView.OnUnitChangedListener {
    companion object {
        const val EXTRA_CONVERTER_ID = "converter_id"
    }

    private val binding by viewBinding(ActivityUnitConverterBinding::inflate)
    private var vibrateOnButtonPress = true
    private lateinit var converter: Converter
    private var bannerAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.unitConverterToolbar.inflateMenu(R.menu.converter_menu)
            setupOptionsMenu()
        }

        setupEdgeToEdge(
            padTopSystem = listOf(binding.unitConverterAppbar),
            padBottomSystem = listOf(binding.adBannerContainer)
        )

        val converter = Converter.ALL.getOrNull(intent.getIntExtra(EXTRA_CONVERTER_ID, 0))

        if (converter == null) {
            finish()
            return
        }
        this.converter = converter

        binding.viewUnitConverter.btnClear.setVibratingOnClickListener {
            binding.viewUnitConverter.viewConverter.root.deleteCharacter()
        }
        binding.viewUnitConverter.btnClear.setOnLongClickListener {
            binding.viewUnitConverter.viewConverter.root.clear()
            true
        }

        binding.viewUnitConverter.run {
            arrayOf(
                btnDecimal, btnPlusMinus, btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9
            ).forEach {
                it.setVibratingOnClickListener { view ->
                    binding.viewUnitConverter.viewConverter.root.numpadClicked(view.id)
                }
            }
        }

        binding.viewUnitConverter.viewConverter.root.setOnUnitChangedListener(this)
        binding.viewUnitConverter.viewConverter.root.setConverter(converter)
        binding.unitConverterToolbar.setTitle(converter.nameResId)

        if (savedInstanceState != null) {
            savedInstanceState.getBundle(CONVERTER_STATE)?.also {
                binding.viewUnitConverter.viewConverter.root.restoreFromSavedState(it)
            }
        } else {
            val storedState = config.getLastConverterUnits(converter)
            if (storedState != null) {
                binding.viewUnitConverter.viewConverter.root.updateUnits(
                    newTopUnit = storedState.topUnit,
                    newBottomUnit = storedState.bottomUnit
                )
            }
        }

        bannerAdView = AdManager.loadAdaptiveBanner(this, binding.adBannerContainer)
    }

    private fun setupOptionsMenu() {
        binding.unitConverterToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.swap_units -> binding.viewUnitConverter.viewConverter.root.switch()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun onResume() {
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        super.onResume()

        setupTopAppBar(binding.unitConverterAppbar, NavigationIcon.Arrow)
        CalcovaThemeManager.applyWindowStyling(this, binding.root)
        binding.unitConverterAppbar.setBackgroundColor(CalcovaThemeManager.bgColor(this))
        CalcovaThemeManager.styleToolbar(this, binding.unitConverterToolbar)

        binding.viewUnitConverter.viewConverter.root.updateColors()

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setupDecimalButton()
        vibrateOnButtonPress = config.vibrateOnButtonPress

        val digitBg = CalcovaThemeManager.digitKeyDrawableRes(this)
        val clearBg = CalcovaThemeManager.clearKeyDrawableRes(this)
        val fnBg = CalcovaThemeManager.fnKeyDrawableRes(this)
        val digitText = CalcovaThemeManager.textPrimaryColor(this)
        val clearText = CalcovaThemeManager.clearTextColor(this)
        val fnText = CalcovaThemeManager.fnTextColor(this)

        binding.viewUnitConverter.apply {
            btnClear.background = ContextCompat.getDrawable(this@UnitConverterActivity, clearBg)
            btnClear.setTextColor(clearText)

            btnDecimal.background = ContextCompat.getDrawable(this@UnitConverterActivity, digitBg)
            btnDecimal.setTextColor(digitText)

            arrayOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9).forEach {
                it.background = ContextCompat.getDrawable(this@UnitConverterActivity, digitBg)
                it.setTextColor(digitText)
            }

            if (plusMinusLayout.isVisible) {
                btnPlusMinus.background = ContextCompat.getDrawable(this@UnitConverterActivity, fnBg)
                btnPlusMinus.setTextColor(fnText)
            }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            CONVERTER_STATE,
            binding.viewUnitConverter.viewConverter.root.saveState()
        )
    }

    override fun onUnitsChanged(topUnit: Converter.Unit?, bottomUnit: Converter.Unit?) {
        val isTemperatureConverter = converter.key == TemperatureConverter.key
        val shouldShowNegativeButton =
            isTemperatureConverter && when (topUnit?.key) {
                TemperatureConverter.Unit.Kelvin.key,
                TemperatureConverter.Unit.Rankine.key -> false

                else -> true
            }

        binding.viewUnitConverter.plusMinusLayout.beVisibleIf(shouldShowNegativeButton)
        if (shouldShowNegativeButton) {
            binding.viewUnitConverter.btnPlusMinus.background =
                ContextCompat.getDrawable(this, CalcovaThemeManager.fnKeyDrawableRes(this))
            binding.viewUnitConverter.btnPlusMinus.setTextColor(CalcovaThemeManager.fnTextColor(this))
        }
    }

    private fun checkHaptic(view: View) {
        if (vibrateOnButtonPress) {
            view.performHapticFeedback()
        }
    }

    private fun View.setVibratingOnClickListener(callback: (view: View) -> Unit) {
        setOnClickListener {
            callback(it)
            checkHaptic(it)
        }
    }

    private fun setupDecimalButton() {
        binding.viewUnitConverter.btnDecimal.text = getDecimalSeparator()
    }
}

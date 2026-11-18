package org.fossify.math.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdView
import me.grantland.widget.AutofitHelper
import org.fossify.commons.extensions.copyToClipboard
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.extensions.performHapticFeedback
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.value
import org.fossify.commons.extensions.viewBinding
import org.fossify.math.R
import org.fossify.math.databases.CalculatorDatabase
import org.fossify.math.databinding.ActivityMainBinding
import org.fossify.math.dialogs.HistoryDialog
import org.fossify.math.extensions.config
import org.fossify.math.helpers.AdManager
import org.fossify.math.helpers.CALCULATOR_STATE
import org.fossify.math.helpers.CalcovaThemeManager
import org.fossify.math.helpers.Calculator
import org.fossify.math.helpers.CalculatorImpl
import org.fossify.math.helpers.DIVIDE
import org.fossify.math.helpers.HistoryHelper
import org.fossify.math.helpers.MINUS
import org.fossify.math.helpers.MULTIPLY
import org.fossify.math.helpers.PERCENT
import org.fossify.math.helpers.PLUS
import org.fossify.math.helpers.POWER
import org.fossify.math.helpers.ROOT
import org.fossify.math.helpers.getDecimalSeparator

class MainActivity : SimpleActivity(), Calculator {
    private var vibrateOnButtonPress = true
    private var saveCalculatorState: String = ""
    private lateinit var calc: CalculatorImpl
    private var bannerAdView: AdView? = null

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        setupEdgeToEdge(
            padTopSystem = listOf(binding.mainAppbar),
            padBottomSystem = listOf(binding.adBannerContainer)
        )

        if (savedInstanceState != null) {
            saveCalculatorState = savedInstanceState.getCharSequence(CALCULATOR_STATE) as? String ?: ""
        }

        calc = CalculatorImpl(
            calculator = this,
            context = applicationContext,
            calculatorState = saveCalculatorState
        )
        binding.btnPlus?.setOnClickOperation(PLUS)
        binding.btnMinus?.setOnClickOperation(MINUS)
        binding.btnMultiply?.setOnClickOperation(MULTIPLY)
        binding.btnDivide?.setOnClickOperation(DIVIDE)
        binding.btnPercent?.setOnClickOperation(PERCENT)
        binding.btnPower?.setOnClickOperation(POWER)
        binding.btnRoot?.setOnClickOperation(ROOT)
        binding.btnMinus?.setOnLongClickListener { calc.turnToNegative() }
        binding.btnClear?.setVibratingOnClickListener { calc.handleClear() }
        binding.btnClear?.setOnLongClickListener {
            calc.handleReset()
            true
        }

        getButtonIds().forEach {
            it?.setVibratingOnClickListener { view ->
                calc.numpadClicked(view.id)
            }
        }

        binding.btnEquals?.setVibratingOnClickListener { calc.handleEquals() }
        binding.formula?.setOnLongClickListener { copyToClipboard(false) }
        binding.result?.setOnLongClickListener { copyToClipboard(true) }
        AutofitHelper.create(binding.result)
        AutofitHelper.create(binding.formula)
        setupDecimalButton()
        applyCalcovaDesign()

        AdManager.requestConsentAndInit(this) {
            runOnUiThread {
                if (!isFinishing && !isDestroyed && bannerAdView == null) {
                    bannerAdView = AdManager.loadAdaptiveBanner(this, binding.adBannerContainer)
                }
            }
        }
    }

    override fun onResume() {
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        super.onResume()
        applyCalcovaDesign()

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setupDecimalButton()
        vibrateOnButtonPress = config.vibrateOnButtonPress
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
        if (!isChangingConfigurations) {
            CalculatorDatabase.destroyInstance()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CALCULATOR_STATE, calc.getCalculatorStateJson().toString())
    }

    private fun applyCalcovaDesign() {
        CalcovaThemeManager.applyWindowStyling(this, binding.root)
        binding.mainAppbar.setBackgroundColor(CalcovaThemeManager.bgColor(this))
        binding.mainToolbar.title = getString(R.string.calcova_brand_title)
        CalcovaThemeManager.styleToolbar(this, binding.mainToolbar, showNavTint = false)

        val displayCardBg = ContextCompat.getDrawable(this, CalcovaThemeManager.displayCardDrawableRes(this))
        binding.displayCard?.background = displayCardBg
        binding.formula?.setTextColor(CalcovaThemeManager.textSecondaryColor(this))
        binding.result?.setTextColor(CalcovaThemeManager.textPrimaryColor(this))

        val digitTextColor = CalcovaThemeManager.textPrimaryColor(this)
        val fnTextColor = CalcovaThemeManager.fnTextColor(this)
        val opTextColor = CalcovaThemeManager.opTextColor(this)
        val clearTextColor = CalcovaThemeManager.clearTextColor(this)

        val digitBgRes = CalcovaThemeManager.digitKeyDrawableRes(this)
        val fnBgRes = CalcovaThemeManager.fnKeyDrawableRes(this)
        val opBgRes = CalcovaThemeManager.opKeyDrawableRes(this)
        val clearBgRes = CalcovaThemeManager.clearKeyDrawableRes(this)

        binding.apply {
            arrayOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btnDecimal).forEach {
                it?.background = ContextCompat.getDrawable(this@MainActivity, digitBgRes)
                it?.setTextColor(digitTextColor)
            }

            arrayOf(btnPercent, btnPower, btnRoot).forEach {
                it?.background = ContextCompat.getDrawable(this@MainActivity, fnBgRes)
                it?.setTextColor(fnTextColor)
            }

            arrayOf(btnDivide, btnMultiply, btnMinus, btnPlus).forEach {
                it?.background = ContextCompat.getDrawable(this@MainActivity, opBgRes)
                it?.setTextColor(opTextColor)
            }

            arrayOf(btnClear, btnReset).forEach {
                it?.background = ContextCompat.getDrawable(this@MainActivity, clearBgRes)
                it?.setTextColor(clearTextColor)
            }

            btnEquals?.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_key_equals)
            btnEquals?.setTextColor(CalcovaThemeManager.equalsTextColor())
        }
    }

    private fun setupOptionsMenu() {
        binding.mainToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.history -> showHistory()
                R.id.unit_converter -> launchUnitConverter()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun checkHaptic(view: View) {
        if (vibrateOnButtonPress) {
            view.performHapticFeedback()
        }
    }

    private fun showHistory() {
        HistoryHelper(this).getHistory { history ->
            if (history.isEmpty()) {
                toast(R.string.history_empty)
            } else {
                AdManager.maybeShowInterstitial(this) {
                    HistoryDialog(this, history, calc)
                }
            }
        }
    }

    private fun launchUnitConverter() {
        hideKeyboard()
        AdManager.maybeShowInterstitial(this) {
            startActivity(Intent(applicationContext, UnitConverterPickerActivity::class.java))
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        hideKeyboard()
        startActivity(Intent(applicationContext, AboutActivity::class.java))
    }

    private fun getButtonIds() = binding.run {
        arrayOf(btnDecimal, btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
    }

    private fun copyToClipboard(copyResult: Boolean): Boolean {
        var value = binding.formula?.value
        if (copyResult) {
            value = binding.result?.value
        }

        return if (value.isNullOrEmpty()) {
            false
        } else {
            copyToClipboard(value)
            true
        }
    }

    override fun showNewResult(value: String, context: Context) {
        binding.result?.text = value
    }

    override fun showNewFormula(value: String, context: Context) {
        binding.formula?.text = value
    }

    private fun setupDecimalButton() {
        binding.btnDecimal?.text = getDecimalSeparator()
    }

    private fun View.setVibratingOnClickListener(callback: (view: View) -> Unit) {
        setOnClickListener {
            callback(it)
            checkHaptic(it)
        }
    }

    private fun View.setOnClickOperation(operation: String) {
        setVibratingOnClickListener {
            calc.handleOperation(operation)
        }
    }
}

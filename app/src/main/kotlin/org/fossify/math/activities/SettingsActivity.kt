package org.fossify.math.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fossify.commons.compose.extensions.enableEdgeToEdgeSimple
import org.fossify.commons.helpers.IS_CUSTOMIZING_COLORS
import org.fossify.math.compose.SettingsScreen
import org.fossify.math.extensions.config
import org.fossify.math.extensions.launchChangeAppLanguageIntent
import org.fossify.math.helpers.AdManager
import org.fossify.math.helpers.CalcovaThemeManager
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private val preferences by lazy { config }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            var themeMode by remember { mutableIntStateOf(preferences.calcovaThemeMode) }
            val isDark = when (themeMode) {
                CalcovaThemeManager.THEME_DARK -> true
                CalcovaThemeManager.THEME_LIGHT -> false
                else -> CalcovaThemeManager.isDarkMode(this)
            }
            CalcovaThemeManager.applyWindowStyling(this)

            CalcovaThemeManager.CalcovaComposeTheme(isDark = isDark) {
                val preventPhoneFromSleeping by preferences.preventPhoneFromSleepingFlow
                    .collectAsStateWithLifecycle(preferences.preventPhoneFromSleeping)
                val vibrateOnButtonPressFlow by preferences.vibrateOnButtonPressFlow
                    .collectAsStateWithLifecycle(preferences.vibrateOnButtonPress)
                val displayLanguage = remember { Locale.getDefault().displayLanguage }
                val showPrivacyOptions = remember { AdManager.isPrivacyOptionsRequired(this) }

                SettingsScreen(
                    displayLanguage = displayLanguage,
                    goBack = ::finish,
                    selectedThemeMode = themeMode,
                    onThemeModeSelected = { newMode ->
                        themeMode = newMode
                        preferences.calcovaThemeMode = newMode
                        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
                        CalcovaThemeManager.applyWindowStyling(this)
                    },
                    customizeWidgetColors = ::setupCustomizeWidgetColors,
                    preventPhoneFromSleeping = preventPhoneFromSleeping,
                    onPreventPhoneFromSleeping = preferences::preventPhoneFromSleeping::set,
                    vibrateOnButtonPress = vibrateOnButtonPressFlow,
                    onVibrateOnButtonPress = preferences::vibrateOnButtonPress::set,
                    onSetupLanguagePress = ::launchChangeAppLanguageIntent,
                    showPrivacyOptions = showPrivacyOptions,
                    onPrivacyOptionsPress = { AdManager.showPrivacyOptionsForm(this) },
                    onAboutPress = { startActivity(Intent(this, AboutActivity::class.java)) }
                )
            }
        }
    }

    private fun setupCustomizeWidgetColors() {
        Intent(this, WidgetConfigureActivity::class.java).apply {
            putExtra(IS_CUSTOMIZING_COLORS, true)
            startActivity(this)
        }
    }
}

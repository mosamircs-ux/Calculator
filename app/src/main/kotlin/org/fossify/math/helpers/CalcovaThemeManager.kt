package org.fossify.math.helpers

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.MaterialToolbar
import org.fossify.math.R
import org.fossify.math.extensions.config

object CalcovaThemeManager {
    const val THEME_DARK = 0
    const val THEME_LIGHT = 1
    const val THEME_SYSTEM = 2

    fun isDarkMode(context: Context): Boolean {
        return when (context.config.calcovaThemeMode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> {
                val nightFlags =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    @ColorInt
    fun bgColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_bg_dark else R.color.calcova_bg_light
    )

    @ColorInt
    fun surfaceColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_surface_dark else R.color.calcova_surface_light
    )

    @ColorInt
    fun textPrimaryColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_text_primary_dark else R.color.calcova_text_primary_light
    )

    @ColorInt
    fun textSecondaryColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_text_secondary_dark else R.color.calcova_text_secondary_light
    )

    @ColorInt
    fun accentIndigo(context: Context): Int =
        ContextCompat.getColor(context, R.color.calcova_accent_indigo)

    @ColorInt
    fun accentCyan(context: Context): Int =
        ContextCompat.getColor(context, R.color.calcova_accent_cyan)

    @ColorInt
    fun fnTextColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_key_fn_text_dark else R.color.calcova_key_fn_text_light
    )

    @ColorInt
    fun opTextColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_key_op_text_dark else R.color.calcova_key_op_text_light
    )

    @ColorInt
    fun clearTextColor(context: Context): Int = ContextCompat.getColor(
        context,
        if (isDarkMode(context)) R.color.calcova_key_clear_text_dark else R.color.calcova_key_clear_text_light
    )

    fun displayCardDrawableRes(context: Context): Int =
        if (isDarkMode(context)) R.drawable.bg_display_card_dark else R.drawable.bg_display_card_light

    fun digitKeyDrawableRes(context: Context): Int =
        if (isDarkMode(context)) R.drawable.bg_key_digit_dark else R.drawable.bg_key_digit_light

    fun fnKeyDrawableRes(context: Context): Int =
        if (isDarkMode(context)) R.drawable.bg_key_fn_dark else R.drawable.bg_key_fn_light

    fun opKeyDrawableRes(context: Context): Int =
        if (isDarkMode(context)) R.drawable.bg_key_op_dark else R.drawable.bg_key_op_light

    fun clearKeyDrawableRes(context: Context): Int =
        if (isDarkMode(context)) R.drawable.bg_key_clear_dark else R.drawable.bg_key_clear_light

    fun applyWindowStyling(activity: Activity, rootView: View? = null) {
        val isDark = isDarkMode(activity)
        val bg = bgColor(activity)
        activity.window.decorView.setBackgroundColor(bg)
        rootView?.setBackgroundColor(bg)
        @Suppress("DEPRECATION")
        activity.window.statusBarColor = bg
        @Suppress("DEPRECATION")
        activity.window.navigationBarColor = bg
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }

    fun styleToolbar(context: Context, toolbar: MaterialToolbar, showNavTint: Boolean = true) {
        val bg = bgColor(context)
        val fg = textPrimaryColor(context)
        toolbar.setBackgroundColor(bg)
        toolbar.setTitleTextColor(fg)
        toolbar.setSubtitleTextColor(textSecondaryColor(context))
        if (showNavTint) {
            toolbar.navigationIcon?.let { icon ->
                val wrapped = DrawableCompat.wrap(icon.mutate())
                DrawableCompat.setTint(wrapped, fg)
                toolbar.navigationIcon = wrapped
            }
        }
        toolbar.overflowIcon?.let { icon ->
            val wrapped = DrawableCompat.wrap(icon.mutate())
            DrawableCompat.setTint(wrapped, fg)
            toolbar.overflowIcon = wrapped
        }
        for (i in 0 until toolbar.menu.size()) {
            val item = toolbar.menu.getItem(i)
            item.icon?.let { icon ->
                val wrapped = DrawableCompat.wrap(icon.mutate())
                DrawableCompat.setTint(wrapped, fg)
                item.icon = wrapped
            }
        }
    }

    fun syncFossifyPrefsForTheme(context: Context) {
        val isDark = isDarkMode(context)
        context.config.apply {
            isSystemThemeEnabled = false
            backgroundColor = bgColor(context)
            textColor = textPrimaryColor(context)
            primaryColor = accentIndigo(context)
            accentColor = if (isDark) accentCyan(context) else accentIndigo(context)
            appIconColor = accentIndigo(context)
        }
    }

    private val DarkColors = darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF5B6CFF),
        onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        secondary = androidx.compose.ui.graphics.Color(0xFF22D3EE),
        onSecondary = androidx.compose.ui.graphics.Color(0xFF090D1F),
        background = androidx.compose.ui.graphics.Color(0xFF090D1F),
        onBackground = androidx.compose.ui.graphics.Color(0xFFF4F7FF),
        surface = androidx.compose.ui.graphics.Color(0xFF111834),
        onSurface = androidx.compose.ui.graphics.Color(0xFFF4F7FF),
        surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1A2348),
        onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF94A3D4),
        outline = androidx.compose.ui.graphics.Color(0xFF232F5C),
    )

    private val LightColors = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF4355E8),
        onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        secondary = androidx.compose.ui.graphics.Color(0xFF0891B2),
        onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        background = androidx.compose.ui.graphics.Color(0xFFF3F5FC),
        onBackground = androidx.compose.ui.graphics.Color(0xFF101633),
        surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        onSurface = androidx.compose.ui.graphics.Color(0xFF101633),
        surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7ECFC),
        onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF5A6691),
        outline = androidx.compose.ui.graphics.Color(0xFFDCE2F5),
    )

    @Composable
    fun CalcovaComposeTheme(isDark: Boolean, content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            content = content
        )
    }

    @ColorInt
    fun equalsTextColor(): Int = Color.WHITE
}

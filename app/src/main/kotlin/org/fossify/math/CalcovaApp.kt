package org.fossify.math

import android.app.Application
import org.fossify.math.helpers.CalcovaThemeManager

class CalcovaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
    }
}

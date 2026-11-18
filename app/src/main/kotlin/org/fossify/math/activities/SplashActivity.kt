package org.fossify.math.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.fossify.math.helpers.CalcovaThemeManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalcovaThemeManager.syncFossifyPrefsForTheme(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

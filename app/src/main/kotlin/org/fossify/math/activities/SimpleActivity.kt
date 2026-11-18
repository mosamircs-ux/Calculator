package org.fossify.math.activities

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.views.MyAppBarLayout
import org.fossify.math.R

open class SimpleActivity : AppCompatActivity() {
    var useDynamicTheme = false

    fun setupEdgeToEdge(
        padTopSystem: List<View> = emptyList(),
        padBottomSystem: List<View> = emptyList()
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val rootView = findViewById<View>(android.R.id.content)
        val initialTopPaddings = padTopSystem.associateWith { it.paddingTop }
        val initialBottomPaddings = padBottomSystem.associateWith { it.paddingBottom }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            padTopSystem.forEach { view ->
                val baseTop = initialTopPaddings[view] ?: 0
                view.updatePadding(top = baseTop + systemBars.top)
            }
            padBottomSystem.forEach { view ->
                val baseBottom = initialBottomPaddings[view] ?: 0
                view.updatePadding(bottom = baseBottom + systemBars.bottom)
            }
            insets
        }
        ViewCompat.requestApplyInsets(rootView)
    }

    fun setupTopAppBar(appBar: MyAppBarLayout, navigationIcon: NavigationIcon = NavigationIcon.None) {
        val toolbar = (0 until appBar.childCount)
            .map { appBar.getChildAt(it) }
            .filterIsInstance<MaterialToolbar>()
            .firstOrNull() ?: return

        if (navigationIcon == NavigationIcon.Arrow) {
            toolbar.setNavigationIcon(org.fossify.commons.R.drawable.ic_arrow_left_vector)
            toolbar.setNavigationOnClickListener { finish() }
        }
    }

    fun getAppLauncherName() = getString(R.string.app_launcher_name)
}

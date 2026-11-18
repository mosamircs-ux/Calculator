package org.fossify.math.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.fossify.commons.helpers.isTiramisuPlus
import org.fossify.math.R
import org.fossify.math.helpers.CalcovaThemeManager

@Composable
internal fun SettingsScreen(
    goBack: () -> Unit,
    selectedThemeMode: Int,
    onThemeModeSelected: (Int) -> Unit,
    customizeWidgetColors: () -> Unit,
    preventPhoneFromSleeping: Boolean,
    onPreventPhoneFromSleeping: (Boolean) -> Unit,
    vibrateOnButtonPress: Boolean,
    onVibrateOnButtonPress: (Boolean) -> Unit,
    onSetupLanguagePress: () -> Unit,
    displayLanguage: String,
    showPrivacyOptions: Boolean,
    onPrivacyOptionsPress: () -> Unit,
    onAboutPress: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = org.fossify.commons.R.string.settings),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(text = stringResource(id = R.string.theme_mode))
            SettingsCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "Midnight",
                            selected = selectedThemeMode == CalcovaThemeManager.THEME_DARK,
                            onClick = { onThemeModeSelected(CalcovaThemeManager.THEME_DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "Daylight",
                            selected = selectedThemeMode == CalcovaThemeManager.THEME_LIGHT,
                            onClick = { onThemeModeSelected(CalcovaThemeManager.THEME_LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "System",
                            selected = selectedThemeMode == CalcovaThemeManager.THEME_SYSTEM,
                            onClick = { onThemeModeSelected(CalcovaThemeManager.THEME_SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsActionRow(
                    title = stringResource(id = org.fossify.commons.R.string.customize_widget_colors),
                    onClick = customizeWidgetColors
                )
            }

            SectionHeader(text = stringResource(id = org.fossify.commons.R.string.general_settings))
            SettingsCard {
                if (isTiramisuPlus()) {
                    SettingsActionRow(
                        title = stringResource(id = org.fossify.commons.R.string.language),
                        subtitle = displayLanguage,
                        onClick = onSetupLanguagePress
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                SettingsSwitchRow(
                    title = stringResource(id = R.string.vibrate_on_button_press),
                    checked = vibrateOnButtonPress,
                    onCheckedChange = onVibrateOnButtonPress
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsSwitchRow(
                    title = stringResource(id = org.fossify.commons.R.string.prevent_phone_from_sleeping),
                    checked = preventPhoneFromSleeping,
                    onCheckedChange = onPreventPhoneFromSleeping
                )
            }

            SectionHeader(text = stringResource(id = R.string.about_calcova))
            SettingsCard {
                if (showPrivacyOptions) {
                    SettingsActionRow(
                        title = stringResource(id = R.string.privacy_ad_settings),
                        subtitle = stringResource(id = R.string.privacy_ad_settings_summary),
                        onClick = onPrivacyOptionsPress
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                SettingsActionRow(
                    title = stringResource(id = R.string.about_calcova),
                    subtitle = stringResource(id = R.string.calcova_tagline),
                    onClick = onAboutPress
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = 1,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

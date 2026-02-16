package com.oho.hiit_timer.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBottomSheet(
    sheetState: SheetState,
    selected: SettingsViewModel.ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (SettingsViewModel.ThemeMode) -> Unit,
) {
    val c = MonoTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.modalBackground,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 5.dp)
                        .background(
                            color = c.cardBorderColor.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MonoText(
                text = "Theme",
                style = MonoTextStyle.TitleMedium,
                color = c.primaryTextColor,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            )

            ThemeRow(
                title = "System",
                subtitle = "Follow device settings",
                selected = selected == SettingsViewModel.ThemeMode.System,
                onClick = { onSelect(SettingsViewModel.ThemeMode.System) }
            )

            ThemeRow(
                title = "Light",
                subtitle = null,
                selected = selected == SettingsViewModel.ThemeMode.Light,
                onClick = { onSelect(SettingsViewModel.ThemeMode.Light) }
            )

            ThemeRow(
                title = "Dark",
                subtitle = null,
                selected = selected == SettingsViewModel.ThemeMode.Dark,
                onClick = { onSelect(SettingsViewModel.ThemeMode.Dark) }
            )
        }
    }
}

@Composable
private fun ThemeRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors

    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        backgroundColor = c.cardBackground,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (selected) c.primaryButtonBackground.copy(alpha = 0.75f)
            else c.cardBorderColor.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                MonoText(
                    text = title,
                    style = MonoTextStyle.TitleMedium,
                    color = c.primaryTextColor,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    MonoText(
                        text = subtitle,
                        style = MonoTextStyle.BodySecondary,
                        color = c.secondaryTextColor.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Simple selection indicator (no extra icons)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) c.primaryButtonBackground else c.modalBackground.copy(alpha = 0.55f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(c.primaryTextColor)
                    )
                }
            }
        }
    }
}
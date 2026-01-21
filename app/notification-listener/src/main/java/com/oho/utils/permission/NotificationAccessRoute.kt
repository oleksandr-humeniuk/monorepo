package com.oho.utils.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoDivider
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme

/**
 * ComposeRoute: Notification access request (permission onboarding).
 *
 * Design goals:
 * - System-like, clean, premium.
 * - No glow / no gradients.
 * - Theme-driven colors (no purple).
 */
@Composable
fun NotificationAccessRoute(
    openPushNotificationListenerSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors
    val d = MonoTheme.dimens

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.appBackground)
            .systemBarsPadding()
            .padding(horizontal = d.screenPadding),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(28.dp))

            HeroIcon()

            Spacer(Modifier.height(18.dp))

            MonoText(
                text = "Notification History",
                style = MonoTextStyle.TitleLarge,
                color = c.primaryTextColor,
            )

            Spacer(Modifier.height(8.dp))

            MonoText(
                text = "See notifications even after you dismiss them",
                style = MonoTextStyle.BodyPrimary,
                color = c.secondaryTextColor,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(18.dp))

            BenefitsCard(
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(18.dp))
        }

        MonoPrimaryButton(
            text = "Enable Notification Access",
            onClick = openPushNotificationListenerSettings,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeroIcon() {
    val c = MonoTheme.colors
    val s = MonoTheme.shapes

    // Flat, tinted container; no glow.
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(s.cardRadius))
            .background(c.surfaceBackground)
            .border(
                1.dp,
                c.cardBorderColor.copy(alpha = 0.55f),
                androidx.compose.foundation.shape.RoundedCornerShape(s.cardRadius)
            ),
        contentAlignment = Alignment.Center,
    ) {
        MonoIcon(
            painter = painterResource(R.drawable.ic_outline_notifications),
            contentDescription = null,
            tint = c.accentIconColor,
            modifier = Modifier.size(44.dp),
        )
    }
}

@Composable
private fun BenefitsCard(
    modifier: Modifier = Modifier,
) {
    MonoCard(modifier = modifier) {
        BenefitRow("Never lose an important notification")
        MonoDivider()
        BenefitRow("Stored only on your device")
        MonoDivider()
        BenefitRow("View notification history anytime")
    }
}

@Composable
private fun BenefitRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors
    val d = MonoTheme.dimens

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(d.iconSizeMedium)
                .clip(CircleShape)
                .background(c.successColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            MonoIcon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = c.successColor,
                modifier = Modifier.size(d.iconSizeSmall + 2.dp),
            )
        }

        MonoText(
            text = text,
            style = MonoTextStyle.BodySecondary,
            color = c.primaryTextColor,
        )
    }
}

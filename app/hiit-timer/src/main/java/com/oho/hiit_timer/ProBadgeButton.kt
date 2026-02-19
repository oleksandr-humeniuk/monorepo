package com.oho.hiit_timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.HiitMonoPalettes
import com.oho.core.ui.theme.MonoTheme
import com.oho.utils.R as timerR

private val ProRed = Color(0xFFDB253B)
private val ProShape = RoundedCornerShape(8.dp)

@Composable
fun ProBadgeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ProShape)
            .border(1.dp, ProRed, ProShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        MonoText(
            text = "PRO",
            style = MonoTextStyle.Button,
            color = ProRed,
        )
    }
}

@Composable
fun ProUpgradeBanner(
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors

    val shape = RoundedCornerShape(14.dp)
    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        backgroundColor = ProRed.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, ProRed.copy(alpha = 0.25f)),
        shape = shape,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MonoText(
                    text = stringResource(timerR.string.pro_banner_title),
                    style = MonoTextStyle.TitleMedium,
                    color = c.primaryTextColor,
                )
                Spacer(Modifier.height(4.dp))
                MonoText(
                    text = stringResource(timerR.string.pro_banner_subtitle),
                    style = MonoTextStyle.BodySecondary,
                    color = c.secondaryTextColor,
                )
            }
            Spacer(Modifier.width(12.dp))
            ProBadgeButton(onClick = onClick)
        }
    }
}

@Preview(name = "ProBadgeButton — Dark", showBackground = true, backgroundColor = 0xFF0E1117)
@Composable
private fun ProBadgeButtonDarkPreview() {
    MonoTheme(darkTheme = true, colors = HiitMonoPalettes.dark()) {
        Box(modifier = Modifier.padding(16.dp)) {
            ProBadgeButton(onClick = {})
        }
    }
}

@Preview(name = "ProBadgeButton — Light", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun ProBadgeButtonLightPreview() {
    MonoTheme(darkTheme = false, colors = HiitMonoPalettes.light()) {
        Box(modifier = Modifier.padding(16.dp)) {
            ProBadgeButton(onClick = {})
        }
    }
}

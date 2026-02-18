package com.oho.hiit_timer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.HiitMonoPalettes
import com.oho.core.ui.theme.MonoTheme

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

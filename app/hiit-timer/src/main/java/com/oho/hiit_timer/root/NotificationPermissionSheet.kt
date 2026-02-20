package com.oho.hiit_timer.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.utils.R as timerR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionSheet(
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
) {
    val c = MonoTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onNotNow,
        sheetState = sheetState,
        containerColor = c.modalBackground,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MonoIcon(
                painter = painterResource(R.drawable.ic_notifications),
                contentDescription = null,
                tint = c.accentPrimary,
                modifier = Modifier.size(40.dp),
            )

            Spacer(Modifier.height(16.dp))

            MonoText(
                text = stringResource(timerR.string.notification_permission_title),
                style = MonoTextStyle.TitleLarge,
                color = c.primaryTextColor,
            )

            Spacer(Modifier.height(8.dp))

            MonoText(
                text = stringResource(timerR.string.notification_permission_body),
                style = MonoTextStyle.BodySecondary,
                color = c.secondaryTextColor,
            )

            Spacer(Modifier.height(28.dp))

            TextButton(
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MonoText(
                    text = stringResource(timerR.string.notification_permission_not_now),
                    style = MonoTextStyle.BodyPrimary,
                    color = c.secondaryTextColor,
                )
            }

            Spacer(Modifier.height(8.dp))

            MonoPrimaryButton(
                text = stringResource(timerR.string.notification_permission_allow),
                onClick = onAllow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

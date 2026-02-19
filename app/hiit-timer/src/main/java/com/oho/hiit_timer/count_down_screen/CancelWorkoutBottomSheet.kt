package com.oho.hiit_timer.count_down_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.utils.R as timerR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelWorkoutBottomSheet(
    onResume: () -> Unit,
    onCancel: () -> Unit,
) {
    val c = MonoTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onResume,
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
            MonoText(
                text = stringResource(timerR.string.timer_run_cancel_title),
                style = MonoTextStyle.TitleLarge,
                color = c.primaryTextColor,
            )

            Spacer(Modifier.height(8.dp))

            MonoText(
                text = stringResource(timerR.string.timer_run_cancel_body),
                style = MonoTextStyle.BodySecondary,
                color = c.secondaryTextColor,
            )

            Spacer(Modifier.height(28.dp))

            MonoPrimaryButton(
                text = stringResource(timerR.string.timer_run_resume),
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MonoText(
                    text = stringResource(timerR.string.timer_run_cancel_confirm),
                    style = MonoTextStyle.BodyPrimary,
                    color = c.errorColor,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

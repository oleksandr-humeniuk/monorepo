package com.oho.hiit_timer.workouts.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.utils.R as timerR


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameWorkoutBottomSheet(
    sheetState: SheetState,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val c = MonoTheme.colors
    var text by rememberSaveable { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            MonoText(
                text = stringResource(timerR.string.name_workout),
                style = MonoTextStyle.TitleMedium,
                color = c.primaryTextColor,
            )

            // Input
            MonoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = c.cardBackground,
                border = BorderStroke(1.dp, c.cardBorderColor),
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 0.dp
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = c.primaryTextColor
                    ),
                    cursorBrush = SolidColor(c.primaryTextColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (text.isNotBlank()) {
                                keyboardController?.hide()
                                onSave(text.trim())
                            }
                        }
                    ),
                    decorationBox = { inner ->
                        if (text.isBlank()) {
                            MonoText(
                                text = stringResource(timerR.string.workout_name_placeholder),
                                style = MonoTextStyle.BodySecondary,
                                color = c.secondaryTextColor
                            )
                        }
                        inner()
                    }
                )
            }

            MonoPrimaryButton(
                enabled = text.isNotBlank(),
                onClick = {
                    keyboardController?.hide()
                    onSave(text.trim())
                },
                text = stringResource(timerR.string.save_workout),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

package com.oho.hiit_timer.workouts.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.utils.R as timerR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    title: String = stringResource(timerR.string.set_actions)
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
                text = title,
                style = MonoTextStyle.TitleMedium,
                color = c.primaryTextColor,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            )

            ActionRow(
                text = stringResource(timerR.string.edit),
                iconRes = R.drawable.ic_edit,
                onClick = onEdit,
            )

            ActionRow(
                text = stringResource(timerR.string.duplicate),
                iconRes = R.drawable.ic_copy,
                onClick = onDuplicate,
            )

            ActionRow(
                text = stringResource(timerR.string.delete),
                iconRes = R.drawable.ic_delete,
                onClick = onDelete,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun ActionRow(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val c = MonoTheme.colors

    val textColor = if (isDestructive) {
        c.primaryTextColor
    } else c.primaryTextColor

    val iconTint = if (isDestructive) {
        c.secondaryIconColor
    } else c.secondaryIconColor

    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        backgroundColor = c.cardBackground,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, c.cardBorderColor.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonoIcon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )

            Spacer(Modifier.width(12.dp))

            MonoText(
                text = text,
                style = MonoTextStyle.TitleMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            MonoIcon(
                painter = painterResource(R.drawable.ic_navigate_next),
                contentDescription = null,
                tint = c.secondaryIconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
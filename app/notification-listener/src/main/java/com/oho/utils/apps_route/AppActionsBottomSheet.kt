package com.oho.utils.apps_route

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoDivider
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoModalSheet
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme

@Composable
fun AppActionsBottomSheet(
    ui: AppsContract.AppActionsSheetUi,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onExclude: () -> Unit,
    onClearHistory: () -> Unit,
) {
    MonoModalSheet(
        onDismissRequest = onDismiss,
    ) {
        // Pin / Unpin
        ActionRow(
            icon = R.drawable.ic_push_pin,
            title = if (ui.isPinned) "Unpin" else "Pin",
            subtitle = if (ui.isPinned) "Remove from pinned section" else "Keep this app at the top",
            isDestructive = false,
            onClick = {
                onTogglePin()
            },
        )

        MonoDivider()

        // Exclude
        ActionRow(
            icon = R.drawable.ic_visibility_off,
            title = "Exclude",
            subtitle = "Hide this app from the list",
            isDestructive = false,
            onClick = {
                onExclude()
            },
        )

        MonoDivider()

        // Clear history
        ActionRow(
            icon = R.drawable.ic_delete,
            title = "Clear history",
            subtitle = "Delete captured notifications for this app",
            isDestructive = true,
            onClick = {
                onClearHistory()
            },
        )

        Spacer(Modifier.Companion.height(MonoTheme.dimens.screenPadding))
    }
}


@Composable
private fun ActionRow(
    icon: Int,
    title: String,
    subtitle: String? = null,
    isDestructive: Boolean,
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors
    val d = MonoTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = d.listItemPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconTint = if (isDestructive) c.errorColor else c.primaryIconColor

        MonoIcon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            MonoText(
                text = title,
                style = MonoTextStyle.BodyPrimary,
                color = if (isDestructive) c.errorColor else c.primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                MonoText(
                    text = subtitle,
                    style = MonoTextStyle.BodySecondary,
                    color = c.secondaryTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

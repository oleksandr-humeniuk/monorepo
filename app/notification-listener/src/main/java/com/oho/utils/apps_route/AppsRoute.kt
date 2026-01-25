package com.oho.utils.apps_route

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoDivider
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppsRoute(
    viewModel: AppsViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    openSettings: () -> Unit,
    openFilters: () -> Unit,
    openAppDetails: (packageName: String) -> Unit,
    onGoProClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    AppsScreen(
        state = state,
        modifier = modifier,
        onEvent = { ev ->
            when (ev) {
                is AppsContract.UiEvent.OpenSettings -> openSettings()
                is AppsContract.UiEvent.OpenFilters -> openFilters()
                is AppsContract.UiEvent.OpenApp -> openAppDetails(ev.packageName)
                is AppsContract.UiEvent.GoPro -> onGoProClick()
                else -> viewModel.onEvent(ev)
            }
        },
    )
}

@Composable
fun AppsScreen(
    state: AppsContract.UiState,
    onEvent: (AppsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors
    val d = MonoTheme.dimens

    MonoScaffold(
        modifier = modifier,
        title = "Apps",
        actions = {
            IconButton(onClick = { onEvent(AppsContract.UiEvent.OpenFilters) }) {
                MonoIcon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = "Filter",
                    tint = c.accentIconColor,
                )
            }
            IconButton(onClick = { onEvent(AppsContract.UiEvent.OpenSettings) }) {
                MonoIcon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = c.accentIconColor,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .background(c.appBackground)
                .padding(padding)
                .padding(horizontal = d.screenPadding),
        ) {
            if (state is AppsContract.UiState.Content) {
                Spacer(Modifier.height(10.dp))
                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        onEvent(AppsContract.UiEvent.SearchClicked)
                    }
                )
                Spacer(Modifier.height(14.dp))
            }

            when (state) {
                AppsContract.UiState.Empty -> {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }

                is AppsContract.UiState.Content -> {
                    AppsList(
                        state = state,
                        onEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
        if (state is AppsContract.UiState.Content) {
            val sheetState = state.sheetState
            if (sheetState != null) {
                AppActionsBottomSheet(
                    ui = sheetState,
                    onDismiss = {
                        onEvent(AppsContract.UiEvent.ActionsSheetDismissed)
                    },
                    onTogglePin = {
                        onEvent(
                            AppsContract.UiEvent.Pin(
                                packageName = sheetState.packageName,
                                pinned = sheetState.isPinned
                            )
                        )
                    },
                    onExclude = {
                        onEvent(AppsContract.UiEvent.Hide(sheetState.packageName))
                    },
                    onClearHistory = {
                        onEvent(AppsContract.UiEvent.Clear(sheetState.packageName))
                    },
                )
            }
        }
    }
}

@Composable
private fun AppsList(
    state: AppsContract.UiState.Content,
    onEvent: (AppsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pinned = state.items.filter { it.isPinned }
    val rest = state.items.filterNot { it.isPinned }

    LazyColumn(modifier = modifier) {

        appsSection(
            title = "ACTIVE & PINNED",
            items = pinned,
            keyPrefix = "pinned",
            onEvent = onEvent,
            bottomSpacer = 16.dp,
        )

        if (state.showProNudge) {
            item(key = "pro_nudge") {
                ProNudgeCard(onGoPro = {
                    onEvent(AppsContract.UiEvent.GoPro)
                })
                Spacer(Modifier.height(16.dp))
            }
        }

        appsSection(
            title = "RECENT ACTIVITY",
            items = rest,
            keyPrefix = "recent",
            onEvent = onEvent,
            bottomSpacer = 14.dp,
        )
    }
}


@Composable
private fun SectionHeader(text: String) {
    val c = MonoTheme.colors
    MonoText(
        text = text,
        style = MonoTextStyle.Caption,
        color = c.secondaryTextColor,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun AppRow(
    item: AppsContract.AppItemUi,
    onEvent: (AppsContract.UiEvent) -> Unit,
) {
    val c = MonoTheme.colors
    val d = MonoTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                role = Role.Button,
                onClick = {
                    onEvent(AppsContract.UiEvent.OpenApp(item.packageName))
                },
            )
            .padding(vertical = 14.dp)
            .padding(start = d.listItemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            packageName = item.packageName,
            modifier = Modifier.size(48.dp),
        )

        Spacer(Modifier.width(12.dp))

        // MAIN CONTENT
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            MonoText(
                text = item.appName,
                style = MonoTextStyle.BodyPrimary,
                color = c.primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(2.dp))

            MonoText(
                text = item.lastPreview,
                style = MonoTextStyle.BodySecondary,
                color = c.secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(12.dp))

        // TRAILING META (FIX)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            if (item.timeLabel.isNotEmpty()) {
                MonoText(
                    text = item.timeLabel,
                    style = MonoTextStyle.Caption,
                    color = c.secondaryTextColor,
                )
            }

            Spacer(Modifier.height(6.dp))

            CountChip(count = item.totalCount)
        }


        MonoIcon(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = null,
            tint = c.secondaryIconColor,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        onEvent(AppsContract.UiEvent.OpenAppSheet(item.packageName))
                    }
                )
                .padding(12.dp)
                .size(18.dp),
        )
    }

}

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(14.dp)

    val trimmed = remember(packageName) { packageName.trim() }

    val icon: Drawable? = remember(trimmed) {
        runCatching {
            pm.getApplicationInfo(trimmed, 0).loadIcon(pm)
        }.onFailure {
            Log.d("ZXC", "AppIcon: failed to load icon for package '$trimmed'", it)
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(c.surfaceBackground)
            .border(1.dp, c.cardBorderColor.copy(alpha = 0.55f), shape),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(icon) //
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            contentScale = ContentScale.Fit,
            error = painterResource(R.drawable.ic_shield),
            fallback = painterResource(R.drawable.ic_shield),
        )
    }
}


@Composable
private fun CountChip(
    count: Long,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(c.surfaceBackground)
            .border(1.dp, c.cardBorderColor.copy(alpha = 0.35f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        MonoText(
            text = count.toString(),
            style = MonoTextStyle.Caption,
            color = c.secondaryTextColor,
        )
    }
}

@Composable
private fun ProChip() {
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(c.surfaceBackground)
            .border(1.dp, c.accentIconColor.copy(alpha = 0.35f), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        MonoText(
            text = "PRO",
            style = MonoTextStyle.Caption,
            color = c.accentIconColor,
        )
    }
}

@Composable
private fun ProNudgeCard(
    onGoPro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors

    MonoCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.surfaceBackground)
                    .border(1.dp, c.cardBorderColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                MonoIcon(
                    painter = painterResource(R.drawable.ic_lock_open),
                    contentDescription = null,
                    tint = c.accentIconColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                MonoText(
                    text = "Unlock Full History",
                    style = MonoTextStyle.BodyPrimary,
                    color = c.primaryTextColor,
                )
                Spacer(Modifier.height(2.dp))
                MonoText(
                    text = "See 50+ items and 24h+ history.",
                    style = MonoTextStyle.BodySecondary,
                    color = c.secondaryTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        MonoPrimaryButton(
            text = "Go Pro",
            onClick = onGoPro,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(56.dp))

        // light reuse of NotificationAccessRoute visual language
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(MonoTheme.shapes.cardRadius))
                .background(c.surfaceBackground)
                .border(
                    1.dp,
                    c.cardBorderColor.copy(alpha = 0.55f),
                    RoundedCornerShape(MonoTheme.shapes.cardRadius)
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

        Spacer(Modifier.height(18.dp))

        MonoText(
            text = "No notifications yet",
            style = MonoTextStyle.TitleMedium,
            color = c.primaryTextColor,
        )

        Spacer(Modifier.height(8.dp))

        MonoText(
            text = "We’ll show apps here after the first notification arrives",
            style = MonoTextStyle.BodySecondary,
            color = c.secondaryTextColor,
            modifier = Modifier.padding(horizontal = 10.dp),
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(c.surfaceBackground)
            .border(1.dp, c.cardBorderColor.copy(alpha = 0.45f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonoIcon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = c.secondaryIconColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        MonoText(
            text = "Search history",
            style = MonoTextStyle.BodySecondary,
            color = c.secondaryTextColor.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class GroupItemPos { Single, First, Middle, Last }

@Composable
private fun GroupCardItem(
    pos: GroupItemPos,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = MonoTheme.colors
    val r = MonoTheme.shapes.cardRadius

    val shape = when (pos) {
        GroupItemPos.Single -> RoundedCornerShape(r)
        GroupItemPos.First -> RoundedCornerShape(topStart = r, topEnd = r)
        GroupItemPos.Middle -> RoundedCornerShape(0.dp)
        GroupItemPos.Last -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        color = c.cardBackground,
        tonalElevation = 0.dp,
        shadowElevation = if (pos == GroupItemPos.Single || pos == GroupItemPos.First) MonoTheme.elevation.card else 0.dp,
    ) {
        content()
    }
}


private fun LazyListScope.appsSection(
    title: String,
    items: List<AppsContract.AppItemUi>,
    keyPrefix: String,
    onEvent: (AppsContract.UiEvent) -> Unit,
    bottomSpacer: Dp,
) {
    if (items.isEmpty()) return

    item(key = "${keyPrefix}_header") {
        SectionHeader(title)
    }

    itemsIndexed(
        items = items,
        key = { idx, it -> "${keyPrefix}_${it.packageName}" },
    ) { idx, item ->
        val pos = when {
            items.size == 1 -> GroupItemPos.Single
            idx == 0 -> GroupItemPos.First
            idx == items.lastIndex -> GroupItemPos.Last
            else -> GroupItemPos.Middle
        }

        GroupCardItem(pos = pos) {
            Column {
                AppRow(
                    item = item,
                    onEvent = onEvent,
                )
                // Divider only inside the row container for non-last items
                if (idx != items.lastIndex) {
                    MonoDivider(thickness = 0.5.dp)
                }
            }
        }
    }

    item(key = "${keyPrefix}_spacer") {
        Spacer(Modifier.height(bottomSpacer))
    }
}

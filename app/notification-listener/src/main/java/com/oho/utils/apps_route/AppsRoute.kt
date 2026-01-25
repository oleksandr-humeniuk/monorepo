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
    openNotificationListenerSettings: () -> Unit,
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
                is AppsContract.UiEvent.OpenNotificationAccess -> openNotificationListenerSettings()
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
                        onOpenApp = { onEvent(AppsContract.UiEvent.OpenApp(it)) },
                        onGoPro = { onEvent(AppsContract.UiEvent.GoPro) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppsList(
    state: AppsContract.UiState.Content,
    onOpenApp: (String) -> Unit,
    onGoPro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors

    val pinned = state.items.filter { it.isPinned }
    val rest = state.items.filterNot { it.isPinned }

    LazyColumn(
        modifier = modifier,
    ) {
        if (pinned.isNotEmpty()) {
            item {
                SectionHeader("ACTIVE & PINNED")
                GroupCard {
                    pinned.forEachIndexed { idx, item ->
                        AppRow(
                            item = item,
                            onClick = { onOpenApp(item.packageName) },
                        )
                        if (idx != pinned.lastIndex) MonoDivider()
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (state.showProNudge) {
            item {
                ProNudgeCard(onGoPro = onGoPro)
                Spacer(Modifier.height(16.dp))
            }
        }

        if (rest.isNotEmpty()) {
            item {
                SectionHeader("RECENT ACTIVITY")
                GroupCard {
                    rest.forEachIndexed { idx, item ->
                        AppRow(
                            item = item,
                            onClick = { onOpenApp(item.packageName) },
                        )
                        if (idx != rest.lastIndex) MonoDivider()
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }

        if (pinned.isEmpty() && rest.isEmpty()) {
            item {
                // filtered-out state (search or filters) – keep it calm
                MonoCard {
                    MonoText(
                        text = "No matching apps",
                        style = MonoTextStyle.BodyPrimary,
                        color = c.primaryTextColor,
                    )
                    Spacer(Modifier.height(6.dp))
                    MonoText(
                        text = "Try clearing search or filters.",
                        style = MonoTextStyle.BodySecondary,
                        color = c.secondaryTextColor,
                    )
                }
            }
        }
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
private fun GroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(MonoTheme.shapes.cardRadius)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, c.cardBorderColor, shape),
        color = c.cardBackground,
        tonalElevation = 0.dp,
        shadowElevation = MonoTheme.elevation.card,
    ) {
        Column { content() }
    }
}

@Composable
private fun AppRow(
    item: AppsContract.AppItemUi,
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors
    val d = MonoTheme.dimens

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MonoTheme.shapes.cardRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = d.listItemPadding, vertical = 14.dp),
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
                text = if (item.isLastPreviewLocked) "Limit reached" else item.lastPreview,
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

            if (item.isLastPreviewLocked) {
                Spacer(Modifier.height(4.dp))
                ProChip()
            }
        }

        Spacer(Modifier.width(8.dp))

        MonoIcon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = c.secondaryIconColor,
            modifier = Modifier.size(18.dp),
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
                .data(icon) // <- вже готовий Drawable
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            contentScale = ContentScale.Fit,
            error = painterResource(R.drawable.ic_android),
            fallback = painterResource(R.drawable.ic_android),
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

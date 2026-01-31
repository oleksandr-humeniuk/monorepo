package com.oho.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.oho.core.ui.theme.MonoTheme

@Composable
fun MonoScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    MonoScaffold(
        topBar = {
            if (title != null || onBackClick != null) {
                MonoTopBar(
                    title = title.orEmpty(),
                    onBackClick = onBackClick,
                    actions = actions,
                )
            }
        },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = content,
        modifier = modifier,
    )
}

@Composable
fun MonoScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            topBar()
        },
        floatingActionButton = { floatingActionButton?.invoke() },
        bottomBar = { bottomBar?.invoke() },
        content = content,
    )
}


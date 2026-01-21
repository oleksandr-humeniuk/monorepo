package com.oho.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.oho.core.ui.theme.MonoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonoModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape: Shape = RoundedCornerShape(
        topStart = MonoTheme.shapes.sheetRadius,
        topEnd = MonoTheme.shapes.sheetRadius,
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = shape,
        containerColor = MonoTheme.colors.modalBackground,
        contentColor = MonoTheme.colors.primaryTextColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MonoTheme.colors.dividerColor) },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MonoTheme.dimens.screenPadding)
                .padding(bottom = 16.dp),
            content = content,
        )
    }
}
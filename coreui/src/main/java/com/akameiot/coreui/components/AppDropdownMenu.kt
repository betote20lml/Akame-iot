package com.akameiot.coreui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akameiot.coreui.theme.LocalSpacing

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.width(260.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(vertical = spacing.xs),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            content = content
        )
    }
}

@Composable
fun AppMenuSectionHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    DropdownMenuItem(
        text = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Icon(
                imageVector = if (expanded)
                    Icons.Default.ArrowDropDown
                else
                    Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = spacing.md,
            vertical = spacing.sm
        )
    )
}

@Composable
fun AppMenuDivider() {
    val spacing = LocalSpacing.current

    HorizontalDivider(
        modifier = Modifier.padding(vertical = spacing.xs),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
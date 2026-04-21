package com.akameiot.coreui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val menuItemTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    )

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.width(240.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
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
    DropdownMenuItem(
        text = {
            Text(
                text = title,
                style = menuItemTextStyle
            )
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = Modifier.height(44.dp)
    )
}

@Composable
fun AppMenuCheckItem(
    label: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    val bgModifier = if (checked)
        Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    else
        Modifier

    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = if (checked)
                    menuItemTextStyle.copy(color = MaterialTheme.colorScheme.primary)
                else
                    menuItemTextStyle
            )
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = Modifier
            .height(44.dp)
            .then(bgModifier)
    )
}

@Composable
fun AppMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
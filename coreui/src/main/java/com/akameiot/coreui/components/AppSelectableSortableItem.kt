package com.akameiot.coreui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppSelectableSortableItem(
    label: String,
    selected: Boolean,
    isFirst: Boolean,
    onClick: () -> Unit,
    onMoveUp: (() -> Unit)? = null
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            )
        },
        leadingIcon = {
            if (selected)
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            else
                Spacer(Modifier.size(16.dp))
        },
        trailingIcon = {
            if (selected && !isFirst && onMoveUp != null)
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Subir",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        modifier = Modifier.height(44.dp)
    )
}
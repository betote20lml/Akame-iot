package com.akameiot.coreui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppFloatingButton(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String? = null,
    text: String? = null,
    visible: Boolean = true
) {

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {

        if (text == null) {

            FloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }

        } else {

            ExtendedFloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription
                    )
                },
                text = {
                    Text(text)
                }
            )
        }
    }
}


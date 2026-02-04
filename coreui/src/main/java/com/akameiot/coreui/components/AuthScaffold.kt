package com.akameiot.coreui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import com.akameiot.coreui.theme.LocalSpacing

@Composable
fun AuthScaffold(
    modifier: Modifier = Modifier,
    topContent: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding() // evita que el teclado tape inputs
            .navigationBarsPadding() // evita que el sistema tape el bottom
            .padding(spacing.lg)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(spacing.xl))

                topContent?.invoke()

                content()
            }

            if (bottomContent != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    bottomContent()
                }
            }
        }
    }
}

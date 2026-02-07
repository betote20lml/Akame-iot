package com.akameiot.coreui.components

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
import androidx.compose.material3.*
import androidx.compose.runtime.*


@Composable
fun AuthScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topContent: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    val spacing = LocalSpacing.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),

        snackbarHost = {
            AppSnackbarHost(snackbarHostState)
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.lg)
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

            bottomContent?.let {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    it()
                }
            }
        }
    }
}

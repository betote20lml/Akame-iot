package com.akameiot.coreui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.foundation.layout.width


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    titleContent: @Composable () -> Unit,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    drawerContent: @Composable ColumnScope.() -> Unit,
    onNavigationClick: () -> Unit,
    navigationIcon: ImageVector = Icons.Default.Menu,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet (
                modifier = Modifier.width(300.dp),
            )  {
                drawerContent()
            }
        }
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            ProvideTextStyle(
                                value = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.35f),
                                        offset = Offset(0f, 1.5f),
                                        blurRadius = 4f
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                titleContent()
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigationClick) {
                                Icon(
                                    imageVector = navigationIcon,
                                    contentDescription = "Menu",
                                )
                            }
                        },
                        actions = actions,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor             = MaterialTheme.colorScheme.primary,
                            titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                    )
                }
            },
            floatingActionButton = {
                floatingActionButton?.invoke()
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            content(padding)
        }
    }
}
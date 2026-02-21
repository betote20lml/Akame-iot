package com.akameiot.coreui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    title: String,
    drawerContent: @Composable ColumnScope.() -> Unit,
    onMenuClick: suspend CoroutineScope.(DrawerState) -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerContent()
            }
        }
    ) {

        Scaffold(
            snackbarHost = { AppSnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    onMenuClick(drawerState)
                                }
                            }
                        ) {
                            Icon(

                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = actions
                )
            },
            floatingActionButton = {
                floatingActionButton?.invoke()
            }
        ) { padding ->
            content(padding)
        }
    }
}
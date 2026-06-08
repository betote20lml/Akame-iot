package com.akameiot.app.ui.terms

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavController

@Composable
fun TermsScreen(
    navController: NavController
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {

        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://cnxontle.github.io/terms.html".toUri()
            )
        )

        navController.popBackStack()
    }
}
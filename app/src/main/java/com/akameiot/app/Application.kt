package com.akameiot.app

import android.app.Application
import android.util.Log
import com.akameiot.di.AppModule
import com.akameiot.domain.usecase.CalculateMeshWindowUseCase
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import com.akameiot.coreui.theme.ThemeController
import kotlinx.coroutines.withTimeoutOrNull

class AkameApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppModule.init(this)
        ThemeController.bind(AppModule.themeStore.isDark)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
        } catch (e: Exception) {
            Log.e("AmplifyInit", "Failed", e)
        }

        checkAndSyncStaleData()
        watchNetworkFreshness()
    }

    private fun watchNetworkFreshness() {
        appScope.launch {
            val meshWindows = try {
                AppModule.meshWindowStore.getAllWindows()
            } catch (_: Exception) { emptyMap() }

            while (true) {
                try {
                    val networks = AppModule.networkStore.getNetworks()

                    if (networks.isNotEmpty()) {
                        val nowSeconds = System.currentTimeMillis() / 1000L
                        val lastSeen = AppModule.lastSeenPerMesh.value
                        var minTimeToExpiry = Long.MAX_VALUE

                        var staleCount = 0
                        networks.forEach { network ->
                            val lastTs = lastSeen[network.thingName]
                                ?: AppModule.telemetryDao.getLatestTimestamp(network.thingName)
                                ?: run { staleCount++; return@forEach }

                            val windowSeconds = meshWindows[network.thingName]
                                ?: CalculateMeshWindowUseCase.DEFAULT_WINDOW_SECONDS

                            val threshold = (windowSeconds * 2)
                                .coerceAtLeast(CalculateMeshWindowUseCase.DEFAULT_FRESHNESS_SECONDS)

                            val timeToExpiry = threshold - (nowSeconds - lastTs)
                            if (timeToExpiry in 1 until minTimeToExpiry) {
                                minTimeToExpiry = timeToExpiry
                            }

                            if ((nowSeconds - lastTs) > threshold) staleCount++
                        }

                        AppModule.networkStatusFlow.value = when {
                            staleCount == 0              -> AppModule.NetworkStatus.ALL_ONLINE
                            staleCount < networks.size   -> AppModule.NetworkStatus.PARTIAL
                            else                         -> AppModule.NetworkStatus.ALL_OFFLINE
                        }

                        val waitMillis = when {
                            minTimeToExpiry == Long.MAX_VALUE -> 60_000L
                            else -> minTimeToExpiry * 1000L
                        }

                        withTimeoutOrNull(waitMillis) {
                            AppModule.freshnessWakeUp.collect {
                                return@collect
                            }
                        }
                        continue
                    }

                } catch (_: Exception) { }

                withTimeoutOrNull(60_000L) {
                    AppModule.freshnessWakeUp.collect {
                        return@collect
                    }
                }
            }
        }
    }

    private fun checkAndSyncStaleData() {
        appScope.launch {
            try {
                val networks = AppModule.networkStore.getNetworks()
                val nowSeconds = System.currentTimeMillis() / 1000L

                networks
                    .map { network ->
                        async {
                            try {
                                val latestTs = AppModule.telemetryDao
                                    .getLatestTimestamp(network.thingName)

                                if (latestTs == null) return@async

                                val ageSeconds = nowSeconds - latestTs

                                val window = AppModule.calculateMeshWindowUseCase
                                    .getOrCalculate(network.thingName)

                                val threshold = (window * 1.2)
                                    .toLong()
                                    .coerceAtLeast(CalculateMeshWindowUseCase.DEFAULT_FRESHNESS_SECONDS)

                                Log.d(
                                    "MeshSync",
                                    "mesh=${network.thingName} window=${window}s " +
                                            "age=${ageSeconds}s threshold=${threshold}s " +
                                            "isStale=${ageSeconds > threshold}"
                                )

                                if (ageSeconds > threshold) {
                                    AppModule.syncRecentTelemetryUseCase.syncStaleWindow(
                                        meshId = network.thingName,
                                        fromTs = latestTs
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("MeshSync", "Error ${network.thingName}", e)
                            }
                        }
                    }
                    .chunked(4)
                    .forEach { it.awaitAll() }

            } catch (_: Exception) { }
        }
    }
}
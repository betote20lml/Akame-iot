package com.akameiot.app

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.akameiot.app.fcm.worker.SyncTelemetryWorker
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



class AkameApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppModule.init(this)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
        } catch (e: Exception) {
            Log.e("AmplifyInit", "Failed", e)
        }

        checkAndSyncStaleData()
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
                                    "mesh=${network.thingName} age=${ageSeconds}s threshold=${threshold}s"
                                )

                                if (ageSeconds > threshold) {
                                    val work = OneTimeWorkRequestBuilder<SyncTelemetryWorker>()
                                        .setInputData(
                                            workDataOf(
                                                "meshId" to network.thingName,
                                                "notifTs" to 0L
                                            )
                                        )
                                        .setConstraints(
                                            Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build()
                                        )
                                        .build()

                                    WorkManager.getInstance(this@AkameApp)
                                        .enqueueUniqueWork(
                                            "sync_${network.thingName}",
                                            ExistingWorkPolicy.KEEP,
                                            work
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
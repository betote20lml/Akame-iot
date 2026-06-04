package com.akameiot.app.ui.data

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.session.DeviceNetworkStore
import com.akameiot.domain.formatter.MetricFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.akameiot.app.fcm.worker.InitialSyncFinalizerWorker
import com.akameiot.app.fcm.worker.RecoverHistoricalDataWorker
import com.akameiot.di.AppModule


class DataViewModel(
    application: Application,
    private val telemetryDao: TelemetryDao,
    private val networkStore: DeviceNetworkStore,
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()

    private val _state = MutableStateFlow(DataUiState(isLoading = true))
    val uiState: StateFlow<DataUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DataEvent>()
    val events: SharedFlow<DataEvent> = _events.asSharedFlow()

    init {
        loadFilters()
    }

    private fun loadFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            val nets = networkStore.getNetworks()
                .map { NetworkOption(it.thingName, it.displayName) }

            val nameMap = nets.associate { it.thingName to it.displayName }

            val metricKeys = telemetryDao.observeLatestPerMetric()
                .map { list ->
                    list.map { it.metric }
                        .distinct()
                        .sorted()
                }
                .first()

            val locale = Locale.getDefault()
            val display = metricKeys.associateWith { MetricFormatter.formatName(it, locale) }

            _state.update {
                it.copy(
                    isLoading           = false,
                    networks            = nets,
                    networkDisplayNames = nameMap,
                    metrics             = metricKeys,
                    metricsDisplay      = display,
                )
            }

            val syncFailed = AppModule.recoveryStateStore.hasInitialSyncFailed()
            val inProgress = AppModule.syncInProgress.value
            _state.update {
                it.copy(canRecoverHistoricalData = !inProgress && syncFailed)
            }

            AppModule.syncInProgress.collect { inProgressUpdate ->
                val failed = AppModule.recoveryStateStore.hasInitialSyncFailed()
                _state.update {
                    it.copy(canRecoverHistoricalData = !inProgressUpdate && failed)
                }
            }
        }
    }

    fun selectNetwork(thingName: String?) {
        _state.update { it.copy(selectedNetworkId = thingName) }
    }

    fun selectMetric(metric: String?) {
        _state.update { it.copy(selectedMetric = metric) }
    }


    fun exportCsv(context: Context) {
        val s = _state.value
        _state.update { it.copy(isExporting = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = telemetryDao.getFiltered(
                    meshId = s.selectedNetworkId,
                    metric = s.selectedMetric,
                )

                val sdf     = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val isoFmt  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val fileName = "akame_datos_${sdf.format(Date())}.csv"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        put(MediaStore.Downloads.IS_PENDING, 1)   // ← bloquea hasta terminar
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                    ) ?: throw Exception("No se pudo crear el archivo en Downloads")

                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                            writer.write("timestamp,datetime,node,metric,value\n")
                            rows.forEach { row ->
                                val displayName = s.networkDisplayNames[row.meshid] ?: row.meshid
                                val nodeLabel   = "${displayName}_${row.nodeId}"
                                val metricLabel = MetricFormatter.formatName(row.metric, Locale.getDefault())
                                val datetime    = isoFmt.format(Date(row.timestamp * 1000L))
                                writer.write("${row.timestamp},$datetime,$nodeLabel,$metricLabel,${row.value}\n")
                            }
                        }
                    } ?: throw Exception("No se pudo abrir el stream de escritura")

                    // Marcar como disponible
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)

                } else {
                    // Android 9 o menor — necesita WRITE_EXTERNAL_STORAGE en runtime
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!dir.exists()) dir.mkdirs()
                    val file = java.io.File(dir, fileName)
                    file.outputStream().use { out ->
                        OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                            writer.write("meshid,nodeId,metric,timestamp,value\n")
                            rows.forEach { row ->
                                writer.write("${row.meshid},${row.nodeId},${row.metric},${row.timestamp},${row.value}\n")
                            }
                        }
                    }
                }

                _events.emit(DataEvent.ExportSuccess(fileName))

            } catch (e: Exception) {
                _events.emit(DataEvent.ShowError(e.message ?: "Error al exportar"))
            } finally {
                _state.update { it.copy(isExporting = false) }
            }
        }
    }
    fun recoverHistoricalData() {
        if (_state.value.isRecovering) return
        _state.update { it.copy(isRecovering = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val networks = networkStore.getNetworks()
                if (networks.isEmpty()) {
                    _events.emit(DataEvent.ShowError("No hay redes disponibles"))
                    return@launch
                }

                val oldestTs = telemetryDao.getOldestTimestampGlobal()
                    ?: (System.currentTimeMillis() / 1000L)

                val fromTs = oldestTs - (90 * 86400L)

                AppModule.recoveryStateStore.setInitialSyncFailed(true)
                AppModule.syncInProgress.value = true

                val workManager = WorkManager.getInstance(context)
                workManager.cancelUniqueWork("manual_recovery")

                workManager
                    .beginUniqueWork(
                        "manual_recovery",
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequestBuilder<RecoverHistoricalDataWorker>()
                            .setInputData(
                                workDataOf(
                                    "meshIds" to networks.map { it.thingName }.toTypedArray(),
                                    "fromTs"  to fromTs,
                                    "toTs"    to oldestTs
                                )
                            )
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .build()
                    )
                    .then(
                        OneTimeWorkRequestBuilder<InitialSyncFinalizerWorker>().build()
                    )
                    .enqueue()

                _events.emit(DataEvent.RecoverySuccess)

            } catch (e: Exception) {
                AppModule.recoveryStateStore.setInitialSyncFailed(true)
                AppModule.syncInProgress.value = false
                _events.emit(DataEvent.ShowError(e.message ?: "Error al recuperar datos"))
            } finally {
                _state.update { it.copy(isRecovering = false) }
            }
        }
    }
}

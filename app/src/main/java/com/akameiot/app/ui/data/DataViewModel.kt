package com.akameiot.app.ui.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
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


class DataViewModel(
    private val telemetryDao: TelemetryDao,
    private val networkStore: DeviceNetworkStore,
) : ViewModel() {

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

            val metricKeys = telemetryDao.observeLatestPerMetric()
                .map { list ->
                    list.map { it.metric }
                        .distinct()
                        .sorted()
                }
                .first()

            val locale = Locale.getDefault()
            val display = metricKeys.associateWith { MetricFormatter.formatName(it, locale) }

            val count = telemetryDao.countAll()

            _state.update {
                it.copy(
                    isLoading = false,
                    networks = nets,
                    metrics = metricKeys,
                    metricsDisplay = display,
                    rowCount = count,
                )
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

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
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
                            writer.write("meshid,nodeId,metric,timestamp,value\n")
                            rows.forEach { row ->
                                writer.write("${row.meshid},${row.nodeId},${row.metric},${row.timestamp},${row.value}\n")
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
}

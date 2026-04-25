package com.akameiot.app.ui.indexfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.session.DeviceNetworkStore
import com.akameiot.di.AppModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface IndexFactoryEvent {
    data class ShowError(val message: String) : IndexFactoryEvent
    object ExportChanges : IndexFactoryEvent
    object RecoverFromCloud : IndexFactoryEvent
}

class IndexFactoryViewModel(
    private val telemetryDao: TelemetryDao,
    private val networkStore: DeviceNetworkStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndexFactoryUiState(isLoading = true))
    val uiState: StateFlow<IndexFactoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<IndexFactoryEvent>()
    val events: SharedFlow<IndexFactoryEvent> = _events.asSharedFlow()

    // Cache network display names so we don't re-fetch on every metric load
    private var networkNamesCache: Map<String, String> = emptyMap()

    // ── Metric selection ──────────────────────────────────────────────────────

    fun selectMetric(metricKey: String) {
        val display = TelemetryFormatter.formatName(metricKey, Locale.getDefault())
        _uiState.update {
            it.copy(
                selectedMetric = metricKey,
                selectedMetricDisplay = display,
                isLoading = true,
            )
        }
        loadItemsForMetric(metricKey)
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun setSearchActive(active: Boolean) {
        _uiState.update {
            it.copy(searchActive = active, searchQuery = if (!active) "" else it.searchQuery)
        }
        refilter()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refilter()
    }

    // ── Limit editing ─────────────────────────────────────────────────────────

    fun onUserMinChange(nodeId: Int, value: String) =
        updateItem(nodeId) { it.copy(userMin = value) }

    fun onUserMaxChange(nodeId: Int, value: String) =
        updateItem(nodeId) { it.copy(userMax = value) }

    // ── Menu actions (stubs) ──────────────────────────────────────────────────

    fun exportChanges() =
        viewModelScope.launch { _events.emit(IndexFactoryEvent.ExportChanges) }

    fun recoverFromCloud() =
        viewModelScope.launch { _events.emit(IndexFactoryEvent.RecoverFromCloud) }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun loadItemsForMetric(metricKey: String) {
        viewModelScope.launch {
            try {
                // Refresh network names in case they changed
                networkNamesCache = networkStore.getNetworks()
                    .associate { it.thingName to it.displayName }

                // Get one snapshot of the latest-per-metric data
                val latestEntities = telemetryDao.observeLatestPerMetric().first()

                val nodesForMetric = latestEntities
                    .filter { it.metric == metricKey }
                    .distinctBy { it.meshid to it.nodeId }

                val nowSeconds = System.currentTimeMillis() / 1000L
                val locale = Locale.getDefault()
                val metricDisplayName = TelemetryFormatter.formatName(metricKey, locale)


                val ranges = listOf(
                    Triple("7 días",  604_800L,  "7d"),
                    Triple("1 mes",   2_592_000L, "1m"),
                    Triple("3 meses", 7_776_000L, "3m"),
                    Triple("1 año",   31_536_000L,"1y"),
                )

                val items = nodesForMetric.map { entity ->
                    val networkName = networkNamesCache[entity.meshid] ?: entity.meshid
                    val nodeName    = "$networkName · ${entity.nodeId}"

                    // ── 24h directo desde telemetry ──────────────────────────────────
                    val from24h   = nowSeconds - 86_400L
                    val raw24h    = telemetryDao.getMetricHistory(
                        meshId        = entity.meshid,
                        nodeId        = entity.nodeId,
                        metric        = metricKey,
                        fromTimestamp = from24h,
                    )
                    val stat24h = RangeStats(
                        label = "1 día",
                        min   = raw24h.minOfOrNull { it.value },
                        max   = raw24h.maxOfOrNull { it.value },
                    )

                    // ── rangos de agregados ───────────────────────────────────────────
                    val aggStats = ranges.map { (label, secondsBack, level) ->
                        val fromTs  = nowSeconds - secondsBack
                        val buckets = telemetryDao.getAggHistory(
                            level  = level,
                            meshId = entity.meshid,
                            nodeId = entity.nodeId,
                            metric = metricKey,
                            fromTs = fromTs,
                        )
                        RangeStats(
                            label = label,
                            min   = buckets.minOfOrNull { it.minVal },
                            max   = buckets.maxOfOrNull { it.maxVal },
                        )
                    }

                    NodeLimitItem(
                        networkName       = networkName,
                        meshId            = entity.meshid,
                        nodeId            = entity.nodeId,
                        nodeName          = nodeName,
                        metricKey         = metricKey,
                        metricDisplayName = metricDisplayName,
                        stats             = listOf(stat24h) + aggStats,
                    )
                }.sortedWith(compareBy({ it.networkName }, { it.nodeId }))

                _uiState.update { it.copy(items = items, isLoading = false) }
                refilter()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(IndexFactoryEvent.ShowError(e.message ?: "Error cargando datos"))
            }
        }
    }

    private fun updateItem(nodeId: Int, transform: (NodeLimitItem) -> NodeLimitItem) {
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.nodeId == nodeId) transform(it) else it })
        }
        refilter()
    }

    private fun refilter() {
        val state = _uiState.value
        val raw   = state.searchQuery.trim().lowercase()

        val visible = if (raw.isEmpty()) {
            state.items
        } else {

            val normalizedQuery = raw
                .replace("·", "")
                .replace(Regex("\\s+"), " ")
                .trim()

            state.items.filter { item ->
                val normalizedName = item.nodeName
                    .lowercase()
                    .replace("·", "")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                // Busca la query normalizada, o bien red y nodeId por separado
                normalizedName.contains(normalizedQuery)
            }
        }

        _uiState.update { it.copy(visibleItems = visible) }
    }
}

// ── Factory ───────────────────────────────────────────────────────────────────

class IndexFactoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IndexFactoryViewModel(
            telemetryDao = AppModule.telemetryDao,
            networkStore = AppModule.networkStore,
        ) as T
}
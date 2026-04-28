package com.akameiot.app.ui.indexfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akameiot.domain.formatter.MetricFormatter
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.session.DeviceNetworkStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import com.akameiot.domain.policy.isIndexMetric
import androidx.compose.runtime.mutableStateListOf


@OptIn(kotlinx.coroutines.FlowPreview::class)
class IndexFactoryViewModel(
    private val telemetryDao: TelemetryDao,
    private val networkStore: DeviceNetworkStore,
    private val nodeLimitRepository: com.akameiot.domain.repository.NodeLimitRepository,
) : ViewModel() {

    private val spaceRegex = Regex("\\s+")

    private val _baseState = MutableStateFlow(IndexFactoryUiState(isLoading = true))
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<IndexFactoryUiState> =
        combine(
            _baseState,
            searchQuery.debounce(300)
        ) { state, query ->

            val raw = query.trim().lowercase()

            val visible = if (raw.isEmpty()) {
                state.items
            } else {
                val normalizedQuery = raw
                    .replace("·", "")
                    .replace(spaceRegex, " ")
                    .trim()

                state.items.filter { uiItem ->
                    val normalizedName = uiItem.item.nodeName
                        .lowercase()
                        .replace("·", "")
                        .replace(spaceRegex, " ")
                        .trim()

                    normalizedName.contains(normalizedQuery)
                }
            }

            state.copy(
                searchQuery = query,
                visibleItems = visible
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                IndexFactoryUiState(isLoading = true)
            )

    private val _events = MutableSharedFlow<IndexFactoryEvent>()
    val events: SharedFlow<IndexFactoryEvent> = _events.asSharedFlow()

    // Cache network display names so we don't re-fetch on every metric load
    private var networkNamesCache: Map<String, String> = emptyMap()

    // ── Metric selection ──────────────────────────────────────────────────────

    fun selectMetric(metricKey: String) {
        if (metricKey.isIndexMetric()) return
        val display = MetricFormatter.formatName(metricKey, Locale.getDefault())
        _baseState.update {
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
        _baseState.update {
            it.copy(
                searchActive = active,
            )
        }
        if (!active) searchQuery.value = ""
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    // ── Limit editing ─────────────────────────────────────────────────────────

    fun onUserMinChange(nodeId: Int, value: String) {
        _baseState.update { state ->
            val list = state.items
            val index = list.indexOfFirst { it.item.nodeId == nodeId }

            if (index != -1) {
                val current = list[index]
                list[index] = current.copy(userMin = value)
            }

            state
        }
    }

    fun onUserMaxChange(nodeId: Int, value: String) {
        _baseState.update { state ->
            val list = state.items
            val index = list.indexOfFirst { it.item.nodeId == nodeId }

            if (index != -1) {
                val current = list[index]
                list[index] = current.copy(userMax = value)
            }

            state
        }
    }

    // ── Menu actions (stubs) ──────────────────────────────────────────────────

    fun exportChanges() =
        viewModelScope.launch { _events.emit(IndexFactoryEvent.ExportChanges) }

    fun recoverFromCloud() =
        viewModelScope.launch { _events.emit(IndexFactoryEvent.RecoverFromCloud) }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun loadItemsForMetric(metricKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                networkNamesCache = networkStore.getNetworks()
                    .associate { it.thingName to it.displayName }

                val latestEntities = telemetryDao.observeLatestPerMetric().first()
                val nodesForMetric = latestEntities
                    .filter { it.metric == metricKey }
                    .distinctBy { it.meshid to it.nodeId }

                if (nodesForMetric.isEmpty()) {
                    _baseState.update {
                        it.copy(
                            items = mutableStateListOf(),
                            isLoading = false
                        )
                    }

                    return@launch
                }

                val nowSeconds = System.currentTimeMillis() / 1000L
                val metricDisplayName = MetricFormatter.formatName(metricKey, Locale.getDefault())

                // ── 5 queries totales independientemente del número de nodos ──────

                val raw24h = telemetryDao.getMetricMinMaxAllNodes(
                    meshId        = nodesForMetric.first().meshid,
                    metric        = metricKey,
                    fromTimestamp = nowSeconds - 86_400L,
                ).associateBy { it.nodeId }

                val ranges = listOf(
                    Triple("7 días",  604_800L,    "7d"),
                    Triple("1 mes",   2_592_000L,  "1m"),
                    Triple("3 meses", 7_776_000L,  "3m"),
                    Triple("1 año",   31_536_000L, "1y"),
                )

                val aggByLevel = ranges.associate { (_, secondsBack, level) ->
                    level to telemetryDao.getAggMinMaxAllNodes(
                        level  = level,
                        meshId = nodesForMetric.first().meshid,
                        metric = metricKey,
                        fromTs = nowSeconds - secondsBack,
                    ).associateBy { it.nodeId }
                }

                val savedLimits = nodeLimitRepository.getLimitsForMetric(
                    meshId = nodesForMetric.first().meshid,
                    metric = metricKey,
                ).associateBy { it.nodeId }

                val items = nodesForMetric.map { entity ->
                    val networkName = networkNamesCache[entity.meshid] ?: entity.meshid
                    val saved = savedLimits[entity.nodeId]

                    val stat24h = raw24h[entity.nodeId].let { row ->
                        RangeStats(
                            label = "1 día",
                            min   = row?.minVal?.let { "%.2f".format(it) } ?: "—",
                            max   = row?.maxVal?.let { "%.2f".format(it) } ?: "—",
                        )
                    }

                    val aggStats = ranges.map { (label, _, level) ->
                        val row = aggByLevel[level]?.get(entity.nodeId)
                        RangeStats(
                            label = label,
                            min   = row?.minVal?.let { "%.2f".format(it) } ?: "—",
                            max   = row?.maxVal?.let { "%.2f".format(it) } ?: "—",
                        )
                    }

                    NodeLimitItem(
                        networkName       = networkName,
                        meshId            = entity.meshid,
                        nodeId            = entity.nodeId,
                        nodeName          = "$networkName · ${entity.nodeId}",
                        metricKey         = metricKey,
                        metricDisplayName = metricDisplayName,
                        stats             = listOf(stat24h) + aggStats,
                        savedMin          = saved?.userMin?.let { "%.2f".format(it) } ?: "",
                        savedMax          = saved?.userMax?.let { "%.2f".format(it) } ?: "",
                    )
                }.sortedWith(compareBy({ it.networkName }, { it.nodeId }))

                val itemsUi = mutableStateListOf<NodeLimitItemUi>().apply {
                    addAll(items.map { item ->
                        NodeLimitItemUi(
                            item = item,
                            userMin = "",
                            userMax = ""
                        )
                    })
                }

                _baseState.update { state ->
                    state.items.clear()
                    state.items.addAll(itemsUi)
                    state.copy(isLoading = false)
                }


            } catch (e: Exception) {
                _baseState.update { it.copy(isLoading = false) }
                _events.emit(IndexFactoryEvent.ShowError(e.message ?: "Error cargando datos"))
            }
        }
    }

    fun saveLimit(item: NodeLimitItem) {
        val uiItem = _baseState.value.items.find { it.item.nodeId == item.nodeId } ?: return

        val existingMin = _baseState.value.items
            .find { it.item.nodeId == item.nodeId }?.item?.savedMin
        val existingMax = _baseState.value.items
            .find { it.item.nodeId == item.nodeId }?.item?.savedMax

        val min = uiItem.userMin
            .takeIf { it.isNotBlank() }
            ?.toDoubleOrNull()
            ?: existingMin?.toDoubleOrNull()

        val max = uiItem.userMax
            .takeIf { it.isNotBlank() }
            ?.toDoubleOrNull()
            ?: existingMax?.toDoubleOrNull()

        if (min == null && max == null) return

        viewModelScope.launch {
            try {
                nodeLimitRepository.upsert(
                    com.akameiot.domain.model.NodeLimit(
                        meshId  = item.meshId,
                        nodeId  = item.nodeId,
                        metric  = item.metricKey,
                        userMin = min,
                        userMax = max,
                    )
                )
                _baseState.update { state ->
                    val list = state.items
                    val index = list.indexOfFirst { it.item.nodeId == item.nodeId }

                    if (index != -1) {
                        val current = list[index]
                        list[index] = current.copy(
                            userMin = "",
                            userMax = "",
                            item = current.item.copy(
                                savedMin = uiItem.userMin.ifBlank { current.item.savedMin },
                                savedMax = uiItem.userMax.ifBlank { current.item.savedMax },
                            )
                        )
                    }

                    state
                }
                _events.emit(IndexFactoryEvent.LimitSaved(item.nodeName))
            } catch (e: Exception) {
                _events.emit(IndexFactoryEvent.ShowError(e.message ?: "Error guardando límites"))
            }
        }
    }
}
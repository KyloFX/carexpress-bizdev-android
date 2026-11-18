package com.carexpress.bizdev.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carexpress.bizdev.data.SettingsRepository
import com.carexpress.bizdev.data.VisitRepository
import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.VisitEntity
import com.carexpress.bizdev.domain.KpiEngine
import com.carexpress.bizdev.domain.KpiSnapshot
import com.carexpress.bizdev.domain.XlsxExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class FeedFilter(val query: String = "", val segment: Segment? = null, val stage: Stage? = null)

data class DashboardState(
    val loading: Boolean = true,
    val allVisits: List<VisitEntity> = emptyList(),
    val feed: List<VisitEntity> = emptyList(),
    val filter: FeedFilter = FeedFilter(),
    val snapshot: KpiSnapshot = KpiEngine.snapshot(emptyList(), SettingsRepository.DEFAULT_LISTINGS),
    val medicalAidListings: Int = SettingsRepository.DEFAULT_LISTINGS,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val visits: VisitRepository,
    private val settings: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val filter = MutableStateFlow(FeedFilter())

    val state: StateFlow<DashboardState> =
        combine(visits.visits, settings.medicalAidListings, filter) { all, listings, f ->
            val q = f.query.trim()
            val feed = all.filter { v ->
                (q.isEmpty() || v.organisation.contains(q, true) || v.keyPerson.contains(q, true) || v.role.contains(q, true)) &&
                    (f.segment == null || v.segment == f.segment) &&
                    (f.stage == null || v.stage == f.stage)
            }
            DashboardState(false, all, feed, f, KpiEngine.snapshot(all, listings), listings)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun setQuery(q: String) { filter.value = filter.value.copy(query = q) }
    fun setSegment(s: Segment?) { filter.value = filter.value.copy(segment = s) }
    fun setStage(s: Stage?) { filter.value = filter.value.copy(stage = s) }

    fun adjustListings(delta: Int) = viewModelScope.launch {
        settings.setMedicalAidListings(state.value.medicalAidListings + delta)
    }

    /** Writes a native .xlsx to cache and returns a share-sheet intent. */
    suspend fun buildExportIntent(): Intent = withContext(Dispatchers.IO) {
        val all = visits.visits.first()
        val snap = KpiEngine.snapshot(all, settings.medicalAidListings.first())
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
        val file = File(dir, "BIZDEV_Field_Visit_KPI_Tracker_$stamp.xlsx")
        file.outputStream().use { XlsxExporter.write(it, all, snap) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BIZDEV Field Visit & KPI Tracker – $stamp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Intent.createChooser(send, "Share tracker (.xlsx)")
    }
}

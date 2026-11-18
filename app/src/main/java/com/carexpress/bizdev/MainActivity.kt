package com.carexpress.bizdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carexpress.bizdev.ui.DashboardViewModel
import com.carexpress.bizdev.ui.entry.LogVisitBottomSheet
import com.carexpress.bizdev.ui.entry.VisitEditorViewModel
import com.carexpress.bizdev.ui.kpi.KpiTrackerScreen
import com.carexpress.bizdev.ui.report.VisitReportScreen
import com.carexpress.bizdev.ui.theme.BizdevTheme
import com.carexpress.bizdev.ui.theme.BrandTeal
import com.carexpress.bizdev.ui.theme.Divider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(BrandTeal.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Color.White.toArgb(), Color.White.toArgb()),
        )
        super.onCreate(savedInstanceState)
        setContent { BizdevTheme { BizdevAppShell() } }
    }
}

private enum class Tab(val title: String, val subtitle: String) {
    REPORT("Visit Report", "Harare Avenues • Field Dashboard"),
    KPI("KPI Tracker", "Live weighted execution scoring"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BizdevAppShell(
    dashboard: DashboardViewModel = hiltViewModel(),
    editor: VisitEditorViewModel = hiltViewModel(),
) {
    val state by dashboard.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.REPORT) }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tab.title, style = MaterialTheme.typography.titleLarge)
                        Text(tab.subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .8f))
                    }
                },
                actions = {
                    if (tab == Tab.REPORT) IconButton(onClick = {
                        scope.launch {
                            runCatching { dashboard.buildExportIntent() }
                                .onSuccess { context.startActivity(it) }
                                .onFailure { snackbar.showSnackbar("Export failed: ${it.message}") }
                        }
                    }) { Icon(Icons.Outlined.FileDownload, "Export to Excel (.xlsx)") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandTeal, titleContentColor = Color.White, actionIconContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                listOf(Tab.REPORT to Icons.Outlined.ViewAgenda, Tab.KPI to Icons.Outlined.Insights).forEach { (t, icon) ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(icon, null) },
                        label = { Text(t.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, indicatorColor = BrandTeal,
                            selectedTextColor = BrandTeal, unselectedIconColor = Divider.copy(red = .45f, green = .5f, blue = .48f),
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editor.startNew(); sheetOpen = true },
                containerColor = BrandTeal,
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Log Visit") },
            )
        },
    ) { padding ->
        AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
            when (t) {
                Tab.REPORT -> VisitReportScreen(
                    state = state,
                    contentPadding = padding,
                    onQuery = dashboard::setQuery,
                    onSegment = dashboard::setSegment,
                    onStage = dashboard::setStage,
                    onOpenVisit = { editor.startEdit(it); sheetOpen = true },
                )
                Tab.KPI -> KpiTrackerScreen(state.snapshot, padding) { dashboard.adjustListings(it) }
            }
        }
    }

    if (sheetOpen) LogVisitBottomSheet(editor, onDismiss = { sheetOpen = false })
}

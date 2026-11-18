package com.carexpress.bizdev.ui.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Tour
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.VisitEntity
import com.carexpress.bizdev.domain.BannerMetrics
import com.carexpress.bizdev.ui.DashboardState
import com.carexpress.bizdev.ui.components.KpiMetricCard
import com.carexpress.bizdev.ui.components.LeadScoreBadge
import com.carexpress.bizdev.ui.components.SegmentChip
import com.carexpress.bizdev.ui.components.StageChip
import com.carexpress.bizdev.ui.components.grouped
import com.carexpress.bizdev.ui.components.usd
import com.carexpress.bizdev.ui.theme.AccentTeal
import com.carexpress.bizdev.ui.theme.BrandTeal
import com.carexpress.bizdev.ui.theme.Divider
import com.carexpress.bizdev.ui.theme.InkSecondary
import com.carexpress.bizdev.ui.theme.MintSurface
import com.carexpress.bizdev.ui.theme.colors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val stampFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy • hh:mm a")
private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisitReportScreen(
    state: DashboardState,
    contentPadding: PaddingValues,
    onQuery: (String) -> Unit,
    onSegment: (Segment?) -> Unit,
    onStage: (Stage?) -> Unit,
    onOpenVisit: (VisitEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("banner") { KpiBanner(state.snapshot.banner) }
        item("search") { SearchField(state.filter.query, onQuery) }
        item("segments") {
            ChipRow(
                options = listOf<Segment?>(null) + Segment.entries,
                selected = state.filter.segment,
                label = { it?.label ?: "All Segments" },
                onSelect = onSegment,
            )
        }
        item("stages") {
            ChipRow(
                options = listOf<Stage?>(null) + Stage.entries,
                selected = state.filter.stage,
                label = { it?.label ?: "All Stages" },
                onSelect = onStage,
                tint = { it?.colors?.fg },
            )
        }
        item("feed_header") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Visit Feed", style = MaterialTheme.typography.titleMedium, color = BrandTeal)
                Spacer(Modifier.weight(1f))
                Text("${state.feed.size} of ${state.allVisits.size}", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
            }
        }
        if (!state.loading && state.feed.isEmpty()) {
            item("empty") { EmptyFeed() }
        }
        items(state.feed, key = { it.id }) { visit ->
            VisitCard(visit, onClick = { onOpenVisit(visit) }, modifier = Modifier.animateItemPlacement())
        }
    }
}

@Composable
private fun KpiBanner(b: BannerMetrics) {
    data class M(val label: String, val value: String, val icon: ImageVector, val hl: Boolean = false)
    val metrics = listOf(
        M("Est. Monthly Pipeline", b.monthlyPipeline.usd(), Icons.Outlined.Payments, true),
        M("Total Visits Logged", b.totalVisits.grouped(), Icons.Outlined.Tour),
        M("Hot & Warm Leads", b.hotWarmLeads.grouped(), Icons.Outlined.LocalFireDepartment),
        M("Proposals Sent", b.proposalsSent.grouped(), Icons.Outlined.Assignment),
        M("Contracts Won", b.contractsWon.grouped(), Icons.Outlined.EmojiEvents),
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(metrics, key = { it.label }) { m ->
            KpiMetricCard(m.label, m.value, m.icon, Modifier.width(if (m.hl) 180.dp else 148.dp), highlight = m.hl)
        }
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        placeholder = { Text("Search organisation or key person") },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = AccentTeal) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Outlined.Clear, "Clear search") }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentTeal, unfocusedBorderColor = Divider,
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
        ),
    )
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    tint: (T) -> Color? = { null },
) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options, key = { label(it) }) { opt ->
            val isSel = opt == selected
            FilterChip(
                selected = isSel,
                onClick = { onSelect(opt) },
                label = { Text(label(opt), maxLines = 1) },
                shape = RoundedCornerShape(50),
                leadingIcon = tint(opt)?.let { c -> { Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(c)) } },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandTeal, selectedLabelColor = Color.White,
                    containerColor = Color.White, labelColor = BrandTeal,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = isSel, borderColor = Divider, selectedBorderColor = BrandTeal,
                ),
            )
        }
    }
}

@Composable
fun VisitCard(visit: VisitEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(visit.id) { mutableStateOf(false) }
    val rot by animateFloatAsState(if (expanded) 180f else 0f, label = "chev")
    val overdue = visit.followUpDate?.isBefore(LocalDate.now()) == true && visit.stage != Stage.CLOSED_WON

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Divider, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(visit.organisation, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SegmentChip(visit.segment)
                }
            }
            Spacer(Modifier.width(8.dp))
            LeadScoreBadge(visit.leadScore)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = InkSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(visit.visitTimestamp.format(stampFmt), style = MaterialTheme.typography.labelMedium, color = InkSecondary)
            Spacer(Modifier.width(8.dp))
            Text("• ${visit.visitType.label}", style = MaterialTheme.typography.labelMedium, color = AccentTeal, fontWeight = FontWeight.SemiBold)
        }

        if (visit.keyPerson.isNotBlank() || visit.role.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, tint = InkSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(listOf(visit.keyPerson, visit.role).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        StageChip(visit.stage)

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MintSurface).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Stat(Icons.Outlined.Groups, "Covered Lives", visit.coveredLives.grouped())
            Stat(Icons.Outlined.Payments, "Est. Monthly", visit.monthlyValue.usd())
        }

        if (visit.notes.isNotBlank()) {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Badge, null, tint = InkSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Conversation notes", style = MaterialTheme.typography.labelLarge, color = InkSecondary, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ExpandMore, if (expanded) "Collapse notes" else "Expand notes", tint = InkSecondary, modifier = Modifier.rotate(rot))
            }
            AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Text(visit.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (visit.nextAction.isNotBlank() || visit.followUpDate != null) {
            val accent = if (overdue) Color(0xFF991B1B) else BrandTeal
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = .08f))
                    .border(1.dp, accent.copy(alpha = .25f), RoundedCornerShape(14.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.TaskAlt, null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("NEXT ACTION", style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
                    Text(visit.nextAction.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                visit.followUpDate?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (overdue) "OVERDUE" else "FOLLOW-UP", style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
                        Text(it.format(dateFmt), style = MaterialTheme.typography.labelLarge, color = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AccentTeal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, style = MaterialTheme.typography.labelLarge, color = BrandTeal, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
        }
    }
}

@Composable
private fun EmptyFeed() {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.SearchOff, null, tint = InkSecondary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text("No visits match your filters", style = MaterialTheme.typography.titleSmall, color = InkSecondary)
    }
}

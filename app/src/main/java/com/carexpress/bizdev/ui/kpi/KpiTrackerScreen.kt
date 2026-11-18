package com.carexpress.bizdev.ui.kpi

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carexpress.bizdev.domain.KpiResult
import com.carexpress.bizdev.domain.KpiSnapshot
import com.carexpress.bizdev.domain.KpiStatus
import com.carexpress.bizdev.domain.SegmentBreakdown
import com.carexpress.bizdev.domain.Tier
import com.carexpress.bizdev.ui.components.Pill
import com.carexpress.bizdev.ui.components.ProgressBar
import com.carexpress.bizdev.ui.components.RadialGauge
import com.carexpress.bizdev.ui.components.grouped
import com.carexpress.bizdev.ui.components.pct
import com.carexpress.bizdev.ui.components.usd
import com.carexpress.bizdev.ui.theme.AccentTeal
import com.carexpress.bizdev.ui.theme.BrandTeal
import com.carexpress.bizdev.ui.theme.Divider
import com.carexpress.bizdev.ui.theme.InkSecondary
import com.carexpress.bizdev.ui.theme.MintSurface
import com.carexpress.bizdev.ui.theme.colors

@Composable
fun KpiTrackerScreen(
    snapshot: KpiSnapshot,
    contentPadding: PaddingValues,
    onAdjustListings: (Int) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("hero") { HeroCard(snapshot) }
        item("a_header") { SectionHeader("Section A", "Core BIZDEV Objectives & KPI Scorecard") }
        items(snapshot.kpis, key = { it.id }) { k -> KpiCard(k, onAdjustListings) }
        item("b_header") { SectionHeader("Section B", "Performance by Target Segment") }
        items(snapshot.segments, key = { it.segment }) { SegmentCard(it) }
    }
}

@Composable
private fun SectionHeader(eyebrow: String, title: String) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = AccentTeal, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, color = BrandTeal)
    }
}

@Composable
private fun HeroCard(s: KpiSnapshot) {
    val score = s.totalScore
    val animatedScore by animateFloatAsState(score.toFloat(), tween(900), label = "score")
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(BrandTeal, AccentTeal)))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadialGauge(progress = (score / 100).toFloat(), modifier = Modifier.size(132.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${animatedScore.toInt()}%", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("weighted", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .8f))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Total Execution Score", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("Auto-calculated live from ${s.banner.totalVisits} logged visits",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .8f))
                Pill("Tier ${s.tier.number}", Color.White, BrandTeal)
                Text(s.tier.title, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
        // Tier ladder
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Tier.entries.forEach { t ->
                val active = t == s.tier
                val bg by animateColorAsState(if (active) Color.White else Color.White.copy(alpha = .12f), label = "tier")
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(bg).padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("T${t.number}", style = MaterialTheme.typography.labelLarge, color = if (active) BrandTeal else Color.White, fontWeight = FontWeight.Bold)
                    Text("${t.range.first}–${t.range.last}%", style = MaterialTheme.typography.labelSmall,
                        color = if (active) AccentTeal else Color.White.copy(alpha = .75f))
                }
            }
        }
    }
}

private fun KpiStatus.colors(): Pair<Color, Color> = when (this) {
    KpiStatus.EXCEEDED -> Color(0xFFDCFCE7) to Color(0xFF166534)
    KpiStatus.ON_TRACK -> Color(0xFFCCFBF1) to Color(0xFF115E59)
    KpiStatus.IN_PROGRESS -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    KpiStatus.NEEDS_FOCUS -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
}

@Composable
private fun KpiCard(k: KpiResult, onAdjustListings: (Int) -> Unit) {
    val fmt: (Double) -> String = { if (k.isCurrency) it.usd() else it.toInt().grouped() }
    val (sbg, sfg) = k.status.colors()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MintSurface)
            .border(1.dp, Divider, RoundedCornerShape(20.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(k.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(k.description, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
            Spacer(Modifier.width(8.dp))
            Pill(k.status.label, sbg, sfg)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(fmt(k.actual), style = MaterialTheme.typography.headlineSmall, color = BrandTeal)
            Text("  / ${fmt(k.target)}", style = MaterialTheme.typography.labelLarge, color = InkSecondary,
                modifier = Modifier.padding(bottom = 3.dp))
            Spacer(Modifier.weight(1f))
            if (k.isManual) {
                val c = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.White, contentColor = BrandTeal)
                FilledTonalIconButton(onClick = { onAdjustListings(-1) }, enabled = k.actual > 0, colors = c, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Remove, "Decrease listings")
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(onClick = { onAdjustListings(1) }, colors = c, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Add, "Increase listings")
                }
            }
        }
        ProgressBar(k.cappedProgress.toFloat())
        Row {
            Text("Progress ${k.progress.pct()}", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
            Spacer(Modifier.weight(1f))
            Text("Weight ${(k.weight * 100).toInt()}%  •  ", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
            Text("+${"%.1f".format(k.weightedScore)} pts", style = MaterialTheme.typography.labelMedium, color = AccentTeal, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SegmentCard(s: SegmentBreakdown) {
    val c = s.segment.colors
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White)
            .border(1.dp, Divider, RoundedCornerShape(20.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(c.fg))
            Spacer(Modifier.width(8.dp))
            Text(s.segment.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(s.pipeline.usd(), style = MaterialTheme.typography.titleSmall, color = BrandTeal, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth()) {
            Cell("Visits", s.visits.grouped(), Modifier.weight(1f))
            Cell("Proposals", s.proposals.grouped(), Modifier.weight(1f))
            Cell("Won", s.won.grouped(), Modifier.weight(1f))
            Cell("Lives", s.coveredLives.grouped(), Modifier.weight(1f))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text("Conversion rate", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
                Spacer(Modifier.weight(1f))
                Text(s.conversionRate.pct(), style = MaterialTheme.typography.labelMedium, color = AccentTeal, fontWeight = FontWeight.Bold)
            }
            ProgressBar(s.conversionRate.toFloat(), height = 6.dp, color = c.fg)
        }
    }
}

@Composable
private fun Cell(label: String, value: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSecondary)
    }
}

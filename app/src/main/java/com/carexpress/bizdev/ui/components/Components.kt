package com.carexpress.bizdev.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.ui.theme.AccentTeal
import com.carexpress.bizdev.ui.theme.BrandTeal
import com.carexpress.bizdev.ui.theme.Divider
import com.carexpress.bizdev.ui.theme.InkSecondary
import com.carexpress.bizdev.ui.theme.MintSurface
import com.carexpress.bizdev.ui.theme.colors
import com.carexpress.bizdev.ui.theme.leadScoreColors
import java.text.NumberFormat
import java.util.Locale

private val usd0 = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }
private val int = NumberFormat.getIntegerInstance(Locale.US)

fun Double.usd(): String = usd0.format(this)
fun Int.grouped(): String = int.format(this)
fun Double.pct(): String = "${(this * 100).toInt()}%"

@Composable
fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun StageChip(stage: Stage, modifier: Modifier = Modifier) =
    Pill(stage.label, stage.colors.bg, stage.colors.fg, modifier)

@Composable
fun SegmentChip(segment: Segment, modifier: Modifier = Modifier) =
    Pill(segment.label, segment.colors.bg, segment.colors.fg, modifier)

@Composable
fun LeadScoreBadge(score: Int, modifier: Modifier = Modifier) {
    val c = leadScoreColors(score)
    Box(
        modifier = modifier.size(34.dp).clip(CircleShape).background(c.bg),
        contentAlignment = Alignment.Center,
    ) {
        Text("$score", color = c.fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KpiMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val bg = if (highlight) Brush.linearGradient(listOf(BrandTeal, AccentTeal))
    else Brush.linearGradient(listOf(MintSurface, MintSurface))
    val fg = if (highlight) Color.White else BrandTeal
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, if (highlight) Color.Transparent else Divider, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).clip(CircleShape)
                    .background(if (highlight) Color.White.copy(alpha = .18f) else BrandTeal.copy(alpha = .10f)),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp)) }
        }
        Text(value, style = MaterialTheme.typography.headlineSmall, color = fg, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = if (highlight) Color.White.copy(alpha = .85f) else InkSecondary, maxLines = 1)
    }
}

/** Animated linear progress bar (capped 0..1). */
@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = AccentTeal, height: Dp = 8.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "bar")
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(Divider)) {
        Box(Modifier.fillMaxWidth(animated).height(height).clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = .75f)))))
    }
}

/** Animated radial gauge for the overall execution score. */
@Composable
fun RadialGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    stroke: Dp = 14.dp,
    track: Color = Color.White.copy(alpha = .18f),
    color: Color = Color(0xFF7DDCC4),
    content: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "gauge")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val sw = stroke.toPx()
            val d = size.minDimension - sw
            val tl = Offset((size.width - d) / 2, (size.height - d) / 2)
            drawArc(track, 135f, 270f, false, tl, Size(d, d), style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(color, 135f, 270f * animated, false, tl, Size(d, d), style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}


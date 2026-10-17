package app.familiar.ui.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.familiar.R
import app.familiar.ui.theme.U
import app.familiar.ui.theme.UType
import java.time.DayOfWeek
import java.time.LocalDate

/** Shades for no reading, a little, some, a lot and a feast. */
@Composable
private fun shades(): List<Color> {
    val empty = U.c.surface2
    val full = U.c.mana
    return listOf(empty, lerp(empty, full, 0.3f), lerp(empty, full, 0.55f), lerp(empty, full, 0.8f), full)
}

private fun level(pages: Int, goal: Int): Int = when {
    pages <= 0 -> 0
    pages < goal / 2 -> 1
    pages < goal -> 2
    pages < goal * 2 -> 3
    else -> 4
}

/**
 * A square for every day of [year], weeks in columns and Monday on top; the colour says how much
 * was read against the daily [goal]. Days still ahead stay faint.
 */
@Composable
fun Heatmap(days: Map<LocalDate, Int>, year: Int, goal: Int, today: LocalDate, modifier: Modifier = Modifier) {
    val c = shades()
    val ahead = U.c.line.copy(alpha = 0.35f)
    val first = LocalDate.of(year, 1, 1)
    val start = first.minusDays((first.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    val last = LocalDate.of(year, 12, 31)
    val weeks = ((last.toEpochDay() - start.toEpochDay()) / 7 + 1).toInt()
    val g = goal.coerceAtLeast(1)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(weeks / 7f)) {
            val cell = size.width / weeks
            val gap = cell * 0.16f
            for (w in 0 until weeks) for (d in 0 until 7) {
                val day = start.plusDays((w * 7 + d).toLong())
                if (day.year != year) continue
                val color = if (day.isAfter(today)) ahead else c[level(days[day] ?: 0, g)]
                drawRoundRect(color, Offset(w * cell + gap / 2, d * cell + gap / 2), Size(cell - gap, cell - gap), CornerRadius(cell * 0.18f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.heat_less), style = UType.Small)
            Spacer(Modifier.width(6.dp))
            c.forEach {
                Box(Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(it))
                Spacer(Modifier.width(3.dp))
            }
            Spacer(Modifier.width(3.dp))
            Text(stringResource(R.string.heat_more), style = UType.Small)
        }
    }
}

package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habittracker.ui.AppLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle

/**
 * GitHub-style contribution grid: one column per week, one cell per day.
 * Completed days use the habit accent, scheduled-but-missed days a faint outline.
 */
@Composable
fun HabitHeatmap(
    completedDates: Set<LocalDate>,
    isScheduled: (LocalDate) -> Boolean,
    accent: Color,
    today: LocalDate,
    modifier: Modifier = Modifier,
    weeks: Int = 16,
    startDate: LocalDate? = null,
) {
    val gap = 3.dp
    val labelWidth = 24.dp
    val missedColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val restDayColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val outOfRangeColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val firstMonday = today.with(DayOfWeek.MONDAY).minusWeeks((weeks - 1).toLong())

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cell = ((maxWidth - labelWidth - gap * (weeks + 1)) / weeks).coerceIn(8.dp, 22.dp)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                // Weekday labels, shown on alternating rows to avoid crowding.
                Column(
                    modifier = Modifier.width(labelWidth),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    DayOfWeek.values().forEach { day ->
                        Box(
                            modifier = Modifier.height(cell).fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (day.value % 2 == 1) {
                                Text(
                                    text = day.getDisplayName(TextStyle.SHORT, AppLocale).take(3),
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                repeat(weeks) { weekIndex ->
                    val monday = firstMonday.plusWeeks(weekIndex.toLong())
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(7) { dayIndex ->
                            val date = monday.plusDays(dayIndex.toLong())
                            val isOutOfRange = date.isAfter(today) ||
                                (startDate != null && date.isBefore(startDate))
                            val done = date in completedDates
                            val missed = !done && !isOutOfRange && isScheduled(date)
                            Box(
                                modifier = Modifier
                                    .size(cell)
                                    .background(
                                        color = when {
                                            done -> accent
                                            isOutOfRange -> outOfRangeColor
                                            missed -> missedColor
                                            else -> restDayColor
                                        },
                                        shape = RoundedCornerShape(3.dp),
                                    )
                                    .then(
                                        if (missed) {
                                            Modifier.border(1.dp, outline, RoundedCornerShape(3.dp))
                                        } else {
                                            Modifier
                                        },
                                    ),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$weeks minggu terakhir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "→ hari ini",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

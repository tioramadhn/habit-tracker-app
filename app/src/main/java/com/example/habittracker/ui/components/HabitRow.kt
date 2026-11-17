package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.habittracker.domain.StreakUnit

/** One habit inside the day list. */
@Composable
fun HabitRow(
    name: String,
    emoji: String,
    accent: Color,
    scheduleText: String,
    streak: Int,
    streakUnit: StreakUnit,
    isCompleted: Boolean,
    weeklyDone: Int?,
    weeklyTarget: Int?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.18f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = scheduleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (streak > 0) {
                        StreakBadge(streak = streak, unit = streakUnit, accent = accent)
                    }
                }
                if (weeklyDone != null && weeklyTarget != null && weeklyTarget > 0) {
                    LinearProgressIndicator(
                        progress = { (weeklyDone.toFloat() / weeklyTarget).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = accent,
                        trackColor = accent.copy(alpha = 0.18f),
                    )
                    Text(
                        text = "$weeklyDone dari $weeklyTarget minggu ini",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            CheckButton(
                checked = isCompleted,
                accent = accent,
                onClick = onToggle,
                contentDescription = if (isCompleted) "Batalkan $name" else "Tandai $name selesai",
            )
        }
    }
}

/** Small streak pill, e.g. "5 hari". */
@Composable
fun StreakBadge(streak: Int, unit: StreakUnit, accent: Color, modifier: Modifier = Modifier) {
    val suffix = if (unit == StreakUnit.WEEKS) "minggu" else "hari"
    Surface(
        shape = MaterialTheme.shapes.small,
        color = accent.copy(alpha = 0.16f),
        modifier = modifier,
    ) {
        Text(
            text = "🔥 $streak $suffix",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

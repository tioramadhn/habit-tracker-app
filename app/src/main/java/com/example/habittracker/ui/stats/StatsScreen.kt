package com.example.habittracker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.domain.StreakUnit
import com.example.habittracker.ui.components.DailyCompletionChart
import com.example.habittracker.ui.components.SectionHeader
import com.example.habittracker.ui.components.StatTile
import com.example.habittracker.ui.mascot.MascotEmptyState
import com.example.habittracker.ui.mascot.MascotMoods
import com.example.habittracker.ui.mascot.MascotView
import com.example.habittracker.ui.scheduleLabel
import com.example.habittracker.ui.theme.habitColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onHabitClick: (Long) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistik") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = bottomBar,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!state.isLoading && !state.hasData) {
                item {
                    MascotEmptyState(
                        mascot = MascotMoods.forStats(hasData = false, averageRate = 0f),
                        title = "Belum ada data",
                        message = "Tambahkan habit dan mulai mencatat untuk melihat statistikmu di sini.",
                    )
                }
            }

            if (state.hasData) {
                item {
                    MascotSummaryCard(
                        averageRate = state.averageRate,
                        doneToday = state.doneToday,
                        scheduledToday = state.scheduledToday,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            value = "${state.doneToday}/${state.scheduledToday}",
                            label = "Selesai hari ini",
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "${(state.averageRate * 100).toInt()}%",
                            label = "Rata-rata 30 hari",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            value = state.bestStreak.toString(),
                            label = "Streak terbaik",
                            accent = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = state.perfectDays.toString(),
                            label = "Hari sempurna (30 hari)",
                            accent = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    StatTile(
                        value = "${state.totalCompletions}×",
                        label = "Total habit tercatat sepanjang waktu",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item { SectionHeader("Penyelesaian 14 hari terakhir") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            DailyCompletionChart(days = state.last14Days)
                        }
                    }
                }
            }

            if (state.rows.isNotEmpty()) {
                item { SectionHeader("Per habit") }
                items(state.rows, key = { it.habit.id }) { row ->
                    HabitStatCard(row = row, onClick = { onHabitClick(row.habit.id) })
                }
            }

            if (state.archived.isNotEmpty()) {
                item { SectionHeader("Diarsipkan") }
                items(state.archived, key = { it.id }) { habit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${habit.emoji}  ${habit.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(onClick = { viewModel.restore(habit.id) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Aktifkan kembali")
                            }
                            IconButton(onClick = { viewModel.deleteArchived(habit.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Mascot verdict on the last 30 days, above the numbers. */
@Composable
private fun MascotSummaryCard(averageRate: Float, doneToday: Int, scheduledToday: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MascotView(
                mascot = MascotMoods.forStats(hasData = true, averageRate = averageRate),
                size = 76.dp,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = MascotMoods.statsMessage(hasData = true, averageRate = averageRate),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Hari ini $doneToday dari $scheduledToday habit \u00B7 rata-rata " +
                        "${(averageRate * 100).toInt()}% dalam 30 hari",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun HabitStatCard(row: HabitStatRow, onClick: () -> Unit) {
    val accent = habitColor(row.habit.colorIndex)
    val unit = if (row.stats.streakUnit == StreakUnit.WEEKS) "minggu" else "hari"
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = accent.copy(alpha = 0.18f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(row.habit.emoji, style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = row.habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${row.habit.scheduleLabel()} · streak ${row.stats.currentStreak} $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { row.stats.completionRate },
                    modifier = Modifier.fillMaxWidth(),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.18f),
                )
            }
            Text(
                text = "${(row.stats.completionRate * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
        }
    }
}

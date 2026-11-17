package com.example.habittracker.ui.detail

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.domain.StreakUnit
import com.example.habittracker.ui.components.CheckButton
import com.example.habittracker.ui.components.HabitHeatmap
import com.example.habittracker.ui.components.SectionHeader
import com.example.habittracker.ui.components.StatTile
import com.example.habittracker.ui.dayInitial
import com.example.habittracker.ui.formatMinuteOfDay
import com.example.habittracker.ui.mascot.MascotMoods
import com.example.habittracker.ui.mascot.MascotView
import com.example.habittracker.ui.scheduleLabel
import com.example.habittracker.ui.theme.habitColor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: HabitDetailViewModel = viewModel(factory = HabitDetailViewModel.factory(habitId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val habit = state.habit
    val accent = habitColor(habit?.colorIndex ?: 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = habit?.name ?: "Detail habit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (habit != null) {
                        IconButton(onClick = { onEdit(habit.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Ubah habit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus habit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            habit == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { Text("Habit tidak ditemukan.") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.14f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accent.copy(alpha = 0.25f),
                                modifier = Modifier.size(56.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(habit.emoji, style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text(habit.name, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = buildString {
                                        append(habit.scheduleLabel())
                                        if (habit.reminderEnabled) {
                                            append("  \u00B7  \uD83D\uDD14 ")
                                            append(formatMinuteOfDay(habit.reminderMinuteOfDay))
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (habit.notes.isNotBlank()) {
                                    Text(
                                        text = habit.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            MascotView(
                                mascot = MascotMoods.forHabit(
                                    currentStreak = state.stats.currentStreak,
                                    completionRate = state.stats.completionRate,
                                    isArchived = habit.isArchived,
                                    isScheduledToday = habit.isScheduledOn(state.today) &&
                                        !state.today.isBefore(habit.createdAt),
                                ),
                                size = 76.dp,
                            )
                        }
                    }
                }

                item {
                    val unit = if (state.stats.streakUnit == StreakUnit.WEEKS) "minggu" else "hari"
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            value = "${state.stats.currentStreak} $unit",
                            label = "Streak saat ini",
                            accent = accent,
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "${state.stats.bestStreak} $unit",
                            label = "Streak terbaik",
                            accent = accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            value = "${(state.stats.completionRate * 100).toInt()}%",
                            label = "Konsistensi 30 hari",
                            accent = accent,
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            value = "${state.stats.totalCompleted}×",
                            label = "Total tercatat",
                            accent = accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item { SectionHeader("7 hari terakhir") }
                item {
                    RecentDaysRow(
                        days = state.recentDays,
                        accent = accent,
                        onToggle = viewModel::toggleDate,
                    )
                }

                item { SectionHeader("Riwayat") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            HabitHeatmap(
                                completedDates = state.completedDates,
                                isScheduled = { date -> habit.countsAsTarget(date) },
                                accent = accent,
                                today = state.today,
                                startDate = habit.createdAt,
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            viewModel.setArchived(!habit.isArchived)
                            if (!habit.isArchived) onBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (habit.isArchived) "Aktifkan kembali" else "Arsipkan habit")
                    }
                }
            }
        }
    }

    if (showDeleteDialog && habit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus habit?") },
            text = { Text("\"${habit.name}\" dan seluruh riwayatnya akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(onDeleted = onBack)
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun RecentDaysRow(
    days: List<RecentDay>,
    accent: Color,
    onToggle: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        days.takeLast(7).forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = day.date.dayInitial(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CheckButton(
                    checked = day.isCompleted,
                    accent = accent,
                    onClick = { onToggle(day.date) },
                    size = 36.dp,
                    contentDescription = "Ubah status ${day.date}",
                )
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

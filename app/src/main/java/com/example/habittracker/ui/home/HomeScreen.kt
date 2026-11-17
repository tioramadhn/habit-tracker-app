package com.example.habittracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.domain.ScheduleType
import com.example.habittracker.ui.components.HabitRow
import com.example.habittracker.ui.components.ProgressRing
import com.example.habittracker.ui.components.SectionHeader
import com.example.habittracker.ui.components.WeekStrip
import com.example.habittracker.ui.fullDateLabel
import com.example.habittracker.ui.mascot.Mascot
import com.example.habittracker.ui.mascot.MascotEmptyState
import com.example.habittracker.ui.mascot.MascotMoods
import com.example.habittracker.ui.mascot.MascotReaction
import com.example.habittracker.ui.mascot.MascotReactionBubble
import com.example.habittracker.ui.mascot.MascotView
import com.example.habittracker.ui.relativeDateLabel
import com.example.habittracker.ui.scheduleLabel
import com.example.habittracker.ui.theme.habitColor
import kotlinx.coroutines.delay

private const val REACTION_VISIBLE_MILLIS = 2_200L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHabitClick: (Long) -> Unit,
    onAddHabit: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var reaction by remember { mutableStateOf<MascotReaction?>(null) }
    var checkInCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(reaction) {
        if (reaction != null) {
            delay(REACTION_VISIBLE_MILLIS)
            reaction = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = relativeDateLabel(state.selectedDate, state.today),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = state.selectedDate.fullDateLabel(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddHabit) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(text = "Habit baru", modifier = Modifier.padding(start = 8.dp))
            }
        },
        bottomBar = bottomBar,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DaySummaryCard(
                        progress = state.progress,
                        completed = state.completed.size,
                        scheduled = state.scheduledCount,
                        hasAnyHabit = state.hasAnyHabit,
                    )
                }

                item {
                    WeekStrip(
                        dates = state.weekDates,
                        selectedDate = state.selectedDate,
                        today = state.today,
                        ratios = state.dayRatios,
                        onSelect = viewModel::selectDate,
                    )
                }

                if (!state.isLoading && state.scheduledCount == 0) {
                    item {
                        if (state.hasAnyHabit) {
                            MascotEmptyState(
                                mascot = Mascot.SLEEPING,
                                title = "Tidak ada jadwal",
                                message = "Tidak ada habit yang dijadwalkan pada tanggal ini. Nikmati harimu!",
                            )
                        } else {
                            MascotEmptyState(
                                mascot = Mascot.WAVING,
                                title = "Mulai habit pertamamu",
                                message = "Tekan tombol \"Habit baru\" untuk menambahkan kebiasaan yang mau kamu bangun.",
                            )
                        }
                    }
                }

                if (state.pending.isNotEmpty()) {
                    item { SectionHeader("Belum selesai (${state.pending.size})") }
                    items(state.pending, key = { it.habit.id }) { row ->
                        HabitListItem(
                            row = row,
                            onToggle = {
                                // Completing the last pending habit finishes the day.
                                val finishesDay = state.pending.size == 1
                                checkInCount++
                                reaction = MascotReaction(
                                    mascot = MascotMoods.forCheckIn(finishesDay),
                                    message = MascotMoods.checkInMessage(finishesDay, checkInCount),
                                )
                                viewModel.toggleHabit(row.habit.id)
                            },
                            onClick = onHabitClick,
                        )
                    }
                }

                if (state.completed.isNotEmpty()) {
                    item { SectionHeader("Selesai (${state.completed.size})") }
                    items(state.completed, key = { it.habit.id }) { row ->
                        HabitListItem(
                            row = row,
                            // Un-checking is a correction, not something to celebrate.
                            onToggle = { viewModel.toggleHabit(row.habit.id) },
                            onClick = onHabitClick,
                        )
                    }
                }
            }

            MascotReactionBubble(
                reaction = reaction,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 104.dp),
            )
        }
    }
}

@Composable
private fun HabitListItem(
    row: HabitDayUi,
    onToggle: () -> Unit,
    onClick: (Long) -> Unit,
) {
    val isFlexible = row.habit.scheduleType == ScheduleType.TIMES_PER_WEEK
    HabitRow(
        name = row.habit.name,
        emoji = row.habit.emoji,
        accent = habitColor(row.habit.colorIndex),
        scheduleText = row.habit.scheduleLabel(),
        streak = row.stats.currentStreak,
        streakUnit = row.stats.streakUnit,
        isCompleted = row.isCompleted,
        weeklyDone = row.stats.completedThisWeek.takeIf { isFlexible },
        weeklyTarget = row.stats.weeklyTarget.takeIf { isFlexible },
        onToggle = onToggle,
        onClick = { onClick(row.habit.id) },
    )
}

@Composable
private fun DaySummaryCard(
    progress: Float,
    completed: Int,
    scheduled: Int,
    hasAnyHabit: Boolean,
) {
    val mascot = MascotMoods.forDay(hasAnyHabit, scheduled, completed)
    val message = MascotMoods.dayMessage(hasAnyHabit, scheduled, completed)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProgressRing(
                progress = progress,
                diameter = 104.dp,
                strokeWidth = 8.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
            ) {
                MascotView(mascot = mascot, size = 80.dp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (scheduled == 0) {
                        "Tidak ada jadwal"
                    } else {
                        "${(progress * 100).toInt()}% · $completed dari $scheduled selesai"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

package com.example.habittracker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ScheduleType
import com.example.habittracker.ui.AppLocale
import android.Manifest
import android.os.Build
import com.example.habittracker.notification.HabitNotifier
import com.example.habittracker.ui.components.SectionHeader
import com.example.habittracker.ui.components.TimePickerDialog
import com.example.habittracker.ui.mascot.MascotMoods
import com.example.habittracker.ui.mascot.MascotView
import com.example.habittracker.ui.formatMinuteOfDay
import com.example.habittracker.ui.theme.HabitColorCount
import com.example.habittracker.ui.theme.LocalIsDarkTheme
import com.example.habittracker.ui.theme.habitSwatch
import java.time.DayOfWeek
import java.time.format.TextStyle

private val ScheduleOptions = listOf(
    ScheduleType.DAILY to "Harian",
    ScheduleType.SPECIFIC_DAYS to "Hari tertentu",
    ScheduleType.TIMES_PER_WEEK to "Fleksibel",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditorScreen(
    habitId: Long,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    viewModel: HabitEditorViewModel = viewModel(factory = HabitEditorViewModel.factory(habitId)),
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val notifier = remember(context) { HabitNotifier(context) }
    var notificationsAllowed by remember { mutableStateOf(notifier.canPostNotifications()) }
    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> notificationsAllowed = granted && notifier.canPostNotifications() }

    val requestNotificationAccess: () -> Unit = {
        notificationsAllowed = notifier.canPostNotifications()
        if (!notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = viewModel.reminderMinuteOfDay / 60,
            initialMinute = viewModel.reminderMinuteOfDay % 60,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.onReminderTimeSelected(hour, minute)
                showTimePicker = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isNewHabit) "Habit baru" else "Ubah habit") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save(onSaved) }) { Text("Simpan") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MascotView(
                    mascot = MascotMoods.forEditor(
                        nameBlank = viewModel.name.isBlank(),
                        hasError = viewModel.nameError,
                    ),
                    size = 72.dp,
                )
                Text(
                    text = MascotMoods.editorMessage(
                        nameBlank = viewModel.name.isBlank(),
                        hasError = viewModel.nameError,
                        isNew = viewModel.isNewHabit,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nama habit") },
                placeholder = { Text("Misal: Olahraga pagi") },
                isError = viewModel.nameError,
                supportingText = if (viewModel.nameError) {
                    { Text("Nama habit tidak boleh kosong") }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader("Ikon")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EmojiChoices.forEach { emoji ->
                    val selected = emoji == viewModel.emoji
                    Surface(
                        onClick = { viewModel.onEmojiSelected(emoji) },
                        shape = CircleShape,
                        color = if (selected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            SectionHeader("Warna")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(HabitColorCount) { index ->
                    val swatch = habitSwatch(index, isDark)
                    val selected = index == viewModel.colorIndex
                    Surface(
                        onClick = { viewModel.onColorSelected(index) },
                        shape = CircleShape,
                        color = swatch,
                        modifier = Modifier.size(if (selected) 40.dp else 32.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            SectionHeader("Jadwal")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ScheduleOptions.forEachIndexed { index, (type, label) ->
                    SegmentedButton(
                        selected = viewModel.scheduleType == type,
                        onClick = { viewModel.onScheduleTypeSelected(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, ScheduleOptions.size),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            when (viewModel.scheduleType) {
                ScheduleType.DAILY -> Text(
                    text = "Habit ini muncul setiap hari.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ScheduleType.SPECIFIC_DAYS -> FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DayOfWeek.values().forEach { day ->
                        FilterChip(
                            selected = day in viewModel.daysOfWeek,
                            onClick = { viewModel.onDayToggled(day) },
                            label = {
                                Text(day.getDisplayName(TextStyle.SHORT, AppLocale))
                            },
                        )
                    }
                }

                ScheduleType.TIMES_PER_WEEK -> Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "${viewModel.timesPerWeek}× per minggu",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Slider(
                        value = viewModel.timesPerWeek.toFloat(),
                        onValueChange = { viewModel.onTimesPerWeekChange(it.toInt()) },
                        valueRange = Habit.MIN_TIMES_PER_WEEK.toFloat()..Habit.MAX_TIMES_PER_WEEK.toFloat(),
                        steps = Habit.MAX_TIMES_PER_WEEK - Habit.MIN_TIMES_PER_WEEK - 1,
                    )
                    Text(
                        text = "Bisa dicatat di hari apa saja; streak dihitung per minggu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SectionHeader("Pengingat")
            ReminderSection(
                enabled = viewModel.reminderEnabled,
                minuteOfDay = viewModel.reminderMinuteOfDay,
                onToggle = { wantsReminder ->
                    viewModel.onReminderToggled(wantsReminder)
                    if (wantsReminder) requestNotificationAccess()
                },
                onPickTime = { showTimePicker = true },
                permissionMissing = viewModel.reminderEnabled && !notificationsAllowed,
            )

            SectionHeader("Catatan (opsional)")
            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = viewModel::onNotesChange,
                placeholder = { Text("Kenapa habit ini penting?") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { viewModel.save(onSaved) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
            ) {
                Text(if (viewModel.isNewHabit) "Tambahkan habit" else "Simpan perubahan")
            }
        }
    }
}

@Composable
private fun ReminderSection(
    enabled: Boolean,
    minuteOfDay: Int,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit,
    permissionMissing: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MascotView(mascot = MascotMoods.forReminder(enabled), size = 56.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ingatkan saya",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = if (enabled) {
                        "Notifikasi setiap hari jadwal pukul ${formatMinuteOfDay(minuteOfDay)}"
                    } else {
                        "Tidak ada notifikasi untuk habit ini"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }

        if (enabled) {
            OutlinedButton(onClick = onPickTime, modifier = Modifier.fillMaxWidth()) {
                Text("Jam pengingat: ${formatMinuteOfDay(minuteOfDay)}")
            }
            if (permissionMissing) {
                Text(
                    text = "Izin notifikasi belum aktif. Aktifkan di Pengaturan > Notifikasi " +
                        "agar pengingat bisa muncul.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

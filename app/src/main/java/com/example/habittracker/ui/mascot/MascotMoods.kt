package com.example.habittracker.ui.mascot

/**
 * Picks the mascot that matches what the user is looking at. Pure functions so the
 * mood rules can be unit tested without a device.
 */
object MascotMoods {

    /** Mood for the "today" summary card. */
    fun forDay(hasAnyHabit: Boolean, scheduled: Int, completed: Int): Mascot = when {
        !hasAnyHabit -> Mascot.WAVING
        scheduled == 0 -> Mascot.SLEEPING
        completed >= scheduled -> Mascot.CELEBRATING
        completed == 0 -> Mascot.LOW_ENERGY
        completed * 2 >= scheduled -> Mascot.HAPPY
        else -> Mascot.FOCUSED
    }

    /** One-shot reaction right after a habit is checked off. */
    fun forCheckIn(dayCompleted: Boolean): Mascot =
        if (dayCompleted) Mascot.GOAL_ACHIEVEMENT else Mascot.EXCITED

    /** Mood on a habit's detail screen. */
    fun forHabit(
        currentStreak: Int,
        completionRate: Float,
        isArchived: Boolean,
        isScheduledToday: Boolean,
    ): Mascot = when {
        isArchived -> Mascot.LOCKED
        currentStreak >= 21 -> Mascot.GOAL_ACHIEVEMENT
        currentStreak >= 7 -> Mascot.HAPPY_CONTENT
        !isScheduledToday -> Mascot.RECHARGING
        currentStreak >= 1 -> Mascot.FOCUSED
        completionRate < 0.3f -> Mascot.DRAINED
        else -> Mascot.LOW_ENERGY
    }

    /** Mood for the stats header. */
    fun forStats(hasData: Boolean, averageRate: Float): Mascot = when {
        !hasData -> Mascot.CONFUSED
        averageRate >= 0.85f -> Mascot.GOAL_ACHIEVEMENT
        averageRate >= 0.6f -> Mascot.HAPPY_CONTENT
        averageRate >= 0.25f -> Mascot.MONITORING
        else -> Mascot.BURNED_OUT
    }

    /** Mood while filling in the habit form. */
    fun forEditor(nameBlank: Boolean, hasError: Boolean): Mascot = when {
        hasError -> Mascot.SURPRISED
        nameBlank -> Mascot.THINKING
        else -> Mascot.HAPPY_WRITING
    }

    /** Tiny mascot next to the reminder switch. */
    fun forReminder(enabled: Boolean): Mascot =
        if (enabled) Mascot.MEDITATING else Mascot.SLEEPING

    /** Caption shown with the day summary mascot. */
    fun dayMessage(hasAnyHabit: Boolean, scheduled: Int, completed: Int): String = when {
        !hasAnyHabit -> "Halo! Ayo bikin habit pertamamu."
        scheduled == 0 -> "Hari santai, tidak ada jadwal."
        completed >= scheduled -> "Semua beres. Kerja bagus!"
        completed == 0 -> "Ayo mulai dari satu habit."
        completed * 2 >= scheduled -> "Setengah jalan, lanjutkan!"
        else -> "Awal yang baik, teruskan."
    }

    /** Headline next to the stats mascot. */
    fun statsMessage(hasData: Boolean, averageRate: Float): String = when {
        !hasData -> "Belum ada yang bisa dihitung"
        averageRate >= 0.85f -> "Konsistensimu luar biasa!"
        averageRate >= 0.6f -> "Ritmemu sudah bagus"
        averageRate >= 0.25f -> "Masih naik turun, ayo rapikan"
        else -> "Bulan ini berat, ya? Mulai kecil lagi"
    }

    /** Headline next to the editor mascot. */
    fun editorMessage(nameBlank: Boolean, hasError: Boolean, isNew: Boolean): String = when {
        hasError -> "Nama habitnya belum diisi, nih."
        nameBlank && isNew -> "Habit apa yang mau kamu bangun?"
        nameBlank -> "Namanya jangan dikosongkan, ya."
        isNew -> "Siap! Atur jadwal dan pengingatnya."
        else -> "Perubahanmu siap disimpan."
    }

    /** Short praise for the check-off reaction bubble. */
    fun checkInMessage(dayCompleted: Boolean, seed: Int): String = if (dayCompleted) {
        "Semua habit hari ini selesai!"
    } else {
        PRAISES[((seed % PRAISES.size) + PRAISES.size) % PRAISES.size]
    }

    private val PRAISES = listOf(
        "Mantap, satu lagi selesai!",
        "Keren, lanjutkan!",
        "Konsisten itu kamu!",
        "Satu langkah lebih dekat.",
    )
}

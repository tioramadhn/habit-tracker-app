package com.example.habittracker.ui.mascot

import androidx.annotation.RawRes
import com.example.habittracker.R

/**
 * The chibi mascot's repertoire. Each entry maps to one Lottie sticker in `res/raw`
 * (320x320, 30fps, 3 second loop).
 */
enum class Mascot(@RawRes val rawRes: Int, val description: String) {
    WAVING(R.raw.waving, "Maskot melambaikan tangan"),
    HAPPY(R.raw.happy, "Maskot senang"),
    HAPPY_CONTENT(R.raw.happy_content, "Maskot puas"),
    HAPPY_WRITING(R.raw.happy_writing, "Maskot sedang menulis"),
    EXCITED(R.raw.excited, "Maskot bersemangat"),
    CELEBRATING(R.raw.celebrating, "Maskot merayakan"),
    GOAL_ACHIEVEMENT(R.raw.goal_achievement, "Maskot meraih target"),
    FOCUSED(R.raw.focused, "Maskot fokus"),
    MONITORING(R.raw.monitoring, "Maskot memantau data"),
    THINKING(R.raw.thinking, "Maskot berpikir"),
    CONFUSED(R.raw.confused, "Maskot kebingungan"),
    SURPRISED(R.raw.surprised, "Maskot terkejut"),
    LOW_ENERGY(R.raw.low_energy, "Maskot lesu"),
    DRAINED(R.raw.drained, "Maskot kehabisan tenaga"),
    BURNED_OUT(R.raw.burned_out, "Maskot kelelahan"),
    RECHARGING(R.raw.recharging, "Maskot mengisi tenaga"),
    SLEEPING(R.raw.sleeping, "Maskot tertidur"),
    MEDITATING(R.raw.meditating, "Maskot bermeditasi"),
    LOCKED(R.raw.locked, "Maskot terkunci"),
}

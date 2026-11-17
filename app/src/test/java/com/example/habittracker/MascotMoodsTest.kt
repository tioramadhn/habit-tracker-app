package com.example.habittracker

import com.example.habittracker.ui.mascot.Mascot
import com.example.habittracker.ui.mascot.MascotMoods
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MascotMoodsTest {

    @Test
    fun `day mood follows how much of the day is done`() {
        assertEquals(Mascot.WAVING, MascotMoods.forDay(hasAnyHabit = false, scheduled = 0, completed = 0))
        assertEquals(Mascot.SLEEPING, MascotMoods.forDay(hasAnyHabit = true, scheduled = 0, completed = 0))
        assertEquals(Mascot.LOW_ENERGY, MascotMoods.forDay(hasAnyHabit = true, scheduled = 4, completed = 0))
        assertEquals(Mascot.FOCUSED, MascotMoods.forDay(hasAnyHabit = true, scheduled = 4, completed = 1))
        assertEquals(Mascot.HAPPY, MascotMoods.forDay(hasAnyHabit = true, scheduled = 4, completed = 2))
        assertEquals(Mascot.CELEBRATING, MascotMoods.forDay(hasAnyHabit = true, scheduled = 4, completed = 4))
    }

    @Test
    fun `finishing the last habit of the day gets the big reaction`() {
        assertEquals(Mascot.GOAL_ACHIEVEMENT, MascotMoods.forCheckIn(dayCompleted = true))
        assertEquals(Mascot.EXCITED, MascotMoods.forCheckIn(dayCompleted = false))
    }

    @Test
    fun `habit mood rewards streaks and flags a slump`() {
        fun mood(streak: Int, rate: Float = 0.5f, archived: Boolean = false, dueToday: Boolean = true) =
            MascotMoods.forHabit(streak, rate, archived, dueToday)

        assertEquals(Mascot.GOAL_ACHIEVEMENT, mood(streak = 30))
        assertEquals(Mascot.HAPPY_CONTENT, mood(streak = 10))
        assertEquals(Mascot.FOCUSED, mood(streak = 3))
        assertEquals(Mascot.LOW_ENERGY, mood(streak = 0, rate = 0.5f))
        assertEquals(Mascot.DRAINED, mood(streak = 0, rate = 0.1f))
        // A rest day is not a failure.
        assertEquals(Mascot.RECHARGING, mood(streak = 0, rate = 0.1f, dueToday = false))
        // Archiving wins over everything else.
        assertEquals(Mascot.LOCKED, mood(streak = 30, archived = true))
    }

    @Test
    fun `stats mood tracks the 30 day average`() {
        assertEquals(Mascot.CONFUSED, MascotMoods.forStats(hasData = false, averageRate = 0f))
        assertEquals(Mascot.BURNED_OUT, MascotMoods.forStats(hasData = true, averageRate = 0.1f))
        assertEquals(Mascot.MONITORING, MascotMoods.forStats(hasData = true, averageRate = 0.4f))
        assertEquals(Mascot.HAPPY_CONTENT, MascotMoods.forStats(hasData = true, averageRate = 0.7f))
        assertEquals(Mascot.GOAL_ACHIEVEMENT, MascotMoods.forStats(hasData = true, averageRate = 0.9f))
    }

    @Test
    fun `editor mood reacts to the form state`() {
        assertEquals(Mascot.THINKING, MascotMoods.forEditor(nameBlank = true, hasError = false))
        assertEquals(Mascot.HAPPY_WRITING, MascotMoods.forEditor(nameBlank = false, hasError = false))
        assertEquals(Mascot.SURPRISED, MascotMoods.forEditor(nameBlank = true, hasError = true))
    }

    @Test
    fun `reminder mascot sleeps while the reminder is off`() {
        assertEquals(Mascot.MEDITATING, MascotMoods.forReminder(enabled = true))
        assertEquals(Mascot.SLEEPING, MascotMoods.forReminder(enabled = false))
    }

    @Test
    fun `praise rotates and never crashes on any counter`() {
        val messages = (0..8).map { MascotMoods.checkInMessage(dayCompleted = false, seed = it) }
        assertTrue("varian pujian harus lebih dari satu", messages.distinct().size > 1)
        // Negative or overflowing counters must still land inside the list.
        assertTrue(MascotMoods.checkInMessage(dayCompleted = false, seed = -3).isNotBlank())
        assertTrue(MascotMoods.checkInMessage(dayCompleted = false, seed = Int.MAX_VALUE).isNotBlank())
    }
}

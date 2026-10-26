package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.di.BackgroundClock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundClockTest {

    private var now = 1_000_000L

    private fun clock(grace: Long = 30_000L) = BackgroundClock(clock = { now }, graceMillis = grace)

    @Test
    fun `relocks after the grace window`() {
        val clock = clock()
        clock.onBackground()
        now += 31_000L
        assertTrue(clock.shouldRelock())
    }

    @Test
    fun `a quick interruption keeps the dossier open`() {
        val clock = clock()
        clock.onBackground()
        now += 5_000L
        assertFalse(clock.shouldRelock())
    }

    @Test
    fun `grace boundary itself relocks`() {
        val clock = clock()
        clock.onBackground()
        now += 30_000L
        assertTrue(clock.shouldRelock())
    }

    @Test
    fun `never backgrounded means no relock`() {
        assertFalse(clock().shouldRelock())
    }

    @Test
    fun `state is consumed so a later start without a stop does not relock`() {
        val clock = clock()
        clock.onBackground()
        now += 60_000L
        assertTrue(clock.shouldRelock())
        now += 60_000L
        assertFalse(clock.shouldRelock())
    }

    @Test
    fun `reset discards a pending background`() {
        val clock = clock()
        clock.onBackground()
        clock.reset()
        now += 60_000L
        assertFalse(clock.shouldRelock())
    }
}

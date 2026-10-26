package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.di.BackgroundClock
import com.puneetfsn.kutumbcare.ui.records.maskReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HardeningTest {

    @Test
    fun `sensitive references are masked to the last four`() {
        assertEquals("•••• 7890", maskReference("50100234567890"))
        assertEquals("•••• 3456", maskReference("1234 5678 3456"))
        assertEquals("••••", maskReference("123"))
        assertEquals("••••", maskReference("  12  "))
    }

    @Test
    fun `masked form never leaks the middle of a number`() {
        val masked = maskReference("XXXX123456781234")
        assertFalse(masked.contains("12345678"))
    }
}

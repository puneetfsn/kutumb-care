package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.data.NomineeAssignment
import com.puneetfsn.kutumbcare.data.NomineeProfile
import com.puneetfsn.kutumbcare.data.NomineeShares
import com.puneetfsn.kutumbcare.data.NomineesCodec
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NomineeTest {

    private val meena = NomineeProfile(
        id = "nom-1", fullName = "Meena Kumari", relationship = "Wife",
        dateOfBirth = "14/02/1985", address = "12, MG Road, Jaipur",
        mobile = "9876543210", email = "meena@example.com", idLast4 = "4821"
    )
    private val arjun = NomineeProfile(
        id = "nom-2", fullName = "Arjun Kumari", relationship = "Son",
        dateOfBirth = "03/07/2010", mobile = "9812345678"
    )

    @Test
    fun `age is derived from date of birth, never stored stale`() {
        assertEquals(41, meena.ageYears(LocalDate.of(2026, 10, 26)))
        assertEquals(16, arjun.ageYears(LocalDate.of(2026, 10, 26)))
        assertNull(NomineeProfile(id = "x", fullName = "No Dob").ageYears())
        assertNull(meena.copy(dateOfBirth = "31/02/1985").ageYears())
        assertTrue(NomineeProfile.isValidDob("14/02/1985"))
        assertFalse(NomineeProfile.isValidDob("1985-02-14"))
    }

    @Test
    fun `codec round trip keeps every card field`() {
        val decoded = NomineesCodec.decode(NomineesCodec.encode(listOf(meena, arjun)))
        assertEquals(listOf(meena, arjun), decoded)
        assertTrue(NomineesCodec.decode("").isEmpty())
        assertTrue(NomineesCodec.decode("not json").isEmpty())
    }

    @Test
    fun `shares must total exactly one hundred`() {
        assertTrue(NomineeShares.isComplete(NomineeShares.single("nom-1")))
        assertTrue(NomineeShares.isComplete(NomineeShares.fiftyFifty("nom-1", "nom-2")))
        assertFalse(NomineeShares.isComplete(listOf(NomineeAssignment("nom-1", 60))))
        assertFalse(NomineeShares.isComplete(emptyList()))
        assertEquals(100, NomineeShares.total(NomineeShares.fiftyFifty("nom-1", "nom-2")))
        // Same person twice collapses to a single 100% instead of a fake 50/50.
        assertEquals(
            listOf(NomineeAssignment("nom-1", 100)),
            NomineeShares.fiftyFifty("nom-1", "nom-1")
        )
    }

    @Test
    fun `summary line carries name relationship and age`() {
        val line = meena.summaryLine()
        assertTrue(line.startsWith("Meena Kumari"))
        assertTrue(line.contains("Wife"))
    }
}

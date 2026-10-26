package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.BackupFormat
import com.puneetfsn.kutumbcare.data.ChecklistItem
import com.puneetfsn.kutumbcare.data.NomineeStatus
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.data.ThemeMode
import com.puneetfsn.kutumbcare.data.UserPreferences
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupFormatTest {

    private val sampleItem = ChecklistItem(
        category = AssetCategory.BANKING_FUNDS.name,
        title = "Savings & current accounts",
        description = "Bank name and branch",
        isCompleted = true,
        nomineeStatus = NomineeStatus.NEEDS_NOMINEE.name,
        titleHi = "बचत और चालू खाते",
        descriptionHi = "बैंक का नाम और शाखा",
        institutionName = "SBI — MG Road",
        accountRef = "XXXX9931",
        physicalLocation = "Blue folder, almirah",
        contactName = "Rakesh",
        contactPhone = "9876543210",
        additionalNotes = "Joint holder is spouse",
        sortOrder = 50,
        updatedAt = 1_700_000_000_000L
    )

    private val samplePrefs = UserPreferences(
        pinHash = SettingsRepository.hashPin("1234"),
        pinEnabled = true,
        themeMode = ThemeMode.DARK,
        primaryUserName = "Asha",
        familyLabel = "Kumar family",
        emergencyContactName = "Ravi",
        emergencyContactPhone = "9998887776",
        setupComplete = true
    )

    @Test
    fun `the app pin is never written to a backup`() {
        val json = BackupFormat.encode(listOf(sampleItem), samplePrefs)
        assertFalse(json.contains("pin", ignoreCase = false))
        assertFalse(json.contains(SettingsRepository.hashPin("1234")))
        assertFalse(json.contains("1234"))
        assertTrue(json.contains("kutumb-care"))
    }

    @Test
    fun `round trip preserves records and profile`() {
        val json = BackupFormat.encode(listOf(sampleItem), samplePrefs)
        val payload = BackupFormat.decode(json)

        assertEquals(1, payload.items.size)
        val restored = payload.items.single()
        assertEquals(sampleItem.title, restored.title)
        assertEquals(sampleItem.institutionName, restored.institutionName)
        assertEquals(sampleItem.accountRef, restored.accountRef)
        assertEquals(sampleItem.physicalLocation, restored.physicalLocation)
        assertEquals(sampleItem.contactPhone, restored.contactPhone)
        assertEquals(sampleItem.nomineeStatus, restored.nomineeStatus)
        assertEquals(sampleItem.category, restored.category)
        assertEquals(sampleItem.sortOrder, restored.sortOrder)
        assertEquals(sampleItem.titleHi, restored.titleHi)
        assertEquals(sampleItem.descriptionHi, restored.descriptionHi)
        assertEquals(true, restored.isCompleted)
        // Ids are reassigned by Room on restore.
        assertEquals(0L, restored.id)

        assertEquals("Kumar family", payload.profile?.familyLabel)
        assertEquals(ThemeMode.DARK, payload.profile?.themeMode)
    }

    @Test
    fun `foreign and malformed files are rejected with a readable message`() {
        assertRejected("""{"app":"invoice-generator","version":1,"items":[]}""", "not a Kutumb Care backup")
        assertRejected("not json at all", "not valid JSON")
        assertRejected("""{"app":"kutumb-care","version":1}""", "no records")
        assertRejected("""{"app":"kutumb-care","version":1,"items":[]}""", "no records")
    }

    @Test
    fun `a backup from a newer schema is refused instead of half restored`() {
        val json = BackupFormat.encode(listOf(sampleItem), samplePrefs)
        val bumped = JSONObject(json).put("version", BackupFormat.SCHEMA_VERSION + 1).toString()
        assertRejected(bumped, "newer version")
    }

    @Test
    fun `unknown categories fall back instead of failing the restore`() {
        val json = """
            {"app":"kutumb-care","version":1,"items":[
              {"title":"Old record","category":"SOME_REMOVED_CATEGORY","nomineeStatus":"GONE"}
            ]}
        """.trimIndent()
        val item = BackupFormat.decode(json).items.single()
        assertEquals(AssetCategory.IDENTITY_GOVT.name, item.category)
        assertEquals(NomineeStatus.NOT_APPLICABLE.name, item.nomineeStatus)
    }

    @Test
    fun `a missing profile is reported as absent so current answers survive`() {
        val json = """{"app":"kutumb-care","version":1,"items":[{"title":"Aadhaar card"}]}"""
        assertNull(BackupFormat.decode(json).profile)
    }

    @Test
    fun `a partial profile only carries the fields it has`() {
        val json = """{"app":"kutumb-care","version":1,"profile":{"familyLabel":"Kumar family"},"items":[{"title":"PAN card"}]}"""
        val profile = BackupFormat.decode(json).profile
        assertEquals("Kumar family", profile?.familyLabel)
        assertNull(profile?.primaryUserName)
        assertNull(profile?.themeMode)
    }

    @Test
    fun `titles that are blank are skipped rather than creating empty rows`() {
        val json = """{"app":"kutumb-care","version":1,"items":[{"title":"PAN card"},{"title":"  "}]}"""
        assertEquals(1, BackupFormat.decode(json).items.size)
    }

    private fun assertRejected(json: String, expectedFragment: String) {
        try {
            BackupFormat.decode(json)
            fail("Expected the backup to be rejected: $json")
        } catch (error: IllegalArgumentException) {
            val message = error.message.orEmpty()
            assertTrue("Unexpected message: $message", message.contains(expectedFragment, ignoreCase = true))
        }
    }
}

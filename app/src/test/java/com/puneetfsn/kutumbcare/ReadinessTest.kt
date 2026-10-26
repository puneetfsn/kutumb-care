package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.ChecklistItem
import com.puneetfsn.kutumbcare.data.ChecklistRepository
import com.puneetfsn.kutumbcare.data.DefaultChecklist
import com.puneetfsn.kutumbcare.data.NomineeStatus
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.data.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM checks for the maths a family relies on: the readiness score, the
 * nominee alerts and the seeded India-specific checklist.
 */
class ReadinessTest {

    private fun item(
        category: AssetCategory,
        completed: Boolean = false,
        nominee: NomineeStatus = NomineeStatus.NOT_APPLICABLE,
        title: String = "record"
    ) = ChecklistItem(
        category = category.name,
        title = title,
        description = "",
        isCompleted = completed,
        nomineeStatus = nominee.name
    )

    @Test
    fun `empty checklist is zero percent`() {
        val snapshot = ChecklistRepository.snapshotOf(emptyList(), UserPreferences())
        assertEquals(0, snapshot.readinessPercent)
        assertEquals(0, snapshot.totalCount)
        assertEquals(0, snapshot.completedCount)
    }

    @Test
    fun `readiness rounds down`() {
        val items = (1..3).map { item(AssetCategory.BANKING_FUNDS, completed = true) } +
            (1..5).map { item(AssetCategory.INSURANCE) }
        val snapshot = ChecklistRepository.snapshotOf(items, UserPreferences())
        assertEquals(8, snapshot.totalCount)
        assertEquals(3, snapshot.completedCount)
        assertEquals(37, snapshot.readinessPercent)
    }

    @Test
    fun `full checklist is one hundred percent`() {
        val items = DefaultChecklist.ITEMS.map { it.copy(isCompleted = true) }
        val snapshot = ChecklistRepository.snapshotOf(items, UserPreferences())
        assertEquals(100, snapshot.readinessPercent)
    }

    @Test
    fun `nominee alerts follow the recorded status`() {
        val items = listOf(
            item(AssetCategory.BANKING_FUNDS, nominee = NomineeStatus.NEEDS_NOMINEE),
            item(AssetCategory.INSURANCE, nominee = NomineeStatus.NEEDS_NOMINEE),
            item(AssetCategory.INSURANCE, nominee = NomineeStatus.NOMINEE_VERIFIED),
            // A user may flag anything that carries a nominee, so an identity or
            // digital record must not have its alert swallowed.
            item(AssetCategory.IDENTITY_GOVT, nominee = NomineeStatus.NEEDS_NOMINEE),
            item(AssetCategory.DIGITAL_LEGACY, nominee = NomineeStatus.NEEDS_NOMINEE),
            item(AssetCategory.TRUSTED_CONTACTS)
        )
        val snapshot = ChecklistRepository.snapshotOf(items, UserPreferences())
        assertEquals(4, snapshot.missingNomineeItems.size)
    }

    @Test
    fun `category progress is reported for every category`() {
        val items = listOf(
            item(AssetCategory.BANKING_FUNDS, completed = true),
            item(AssetCategory.BANKING_FUNDS)
        )
        val snapshot = ChecklistRepository.snapshotOf(items, UserPreferences())
        assertEquals(AssetCategory.entries.size, snapshot.categoryProgress.size)
        val banking = snapshot.categoryProgress.first { it.category == AssetCategory.BANKING_FUNDS }
        assertEquals(1, banking.completed)
        assertEquals(2, banking.total)
        assertEquals(50, banking.percent)
        val untouched = snapshot.categoryProgress.first { it.category == AssetCategory.TRUSTED_CONTACTS }
        assertEquals(0, untouched.percent)
    }

    @Test
    fun `seeded checklist covers all 31 India-specific records`() {
        assertEquals(31, DefaultChecklist.ITEMS.size)
        assertEquals(31, DefaultChecklist.ITEMS.map { it.sortOrder }.distinct().size)
        assertEquals(AssetCategory.entries.size, DefaultChecklist.ITEMS.map { it.category }.distinct().size)
        // Only 1,1,1,1 ... every seeded record starts unrecorded so the score is honest.
        assertTrue(DefaultChecklist.ITEMS.none { it.isCompleted })
        assertTrue(DefaultChecklist.ITEMS.none { it.title.isBlank() })
    }

    @Test
    fun `seeded nominee flags all surface on the dashboard`() {
        val items = DefaultChecklist.ITEMS
        val snapshot = ChecklistRepository.snapshotOf(items, UserPreferences())
        val flagged = items.filter { it.nomineeEnum == NomineeStatus.NEEDS_NOMINEE }
        assertEquals(flagged.size, snapshot.missingNomineeItems.size)
        assertTrue(flagged.size >= 12)
        // The credit-shield override under "Property & loans" must not be lost.
        assertTrue(flagged.any { it.title.startsWith("Home loan") })
        assertTrue(DefaultChecklist.ITEMS.none { it.title.startsWith("Property deeds") && it.nomineeEnum == NomineeStatus.NEEDS_NOMINEE })
    }

    @Test
    fun `pin hash is stable, salted by value and never the raw pin`() {
        val first = SettingsRepository.hashPin("1234")
        val second = SettingsRepository.hashPin("1234")
        assertEquals(first, second)
        assertNotEquals(first, SettingsRepository.hashPin("1235"))
        assertNotEquals("1234", first)
        assertEquals(64, first.length)
    }

    @Test
    fun `unknown category falls back to identity instead of crashing`() {
        assertEquals(AssetCategory.IDENTITY_GOVT, AssetCategory.fromRaw("SOMETHING_OLD"))
        assertEquals(NomineeStatus.NOT_APPLICABLE, NomineeStatus.fromRaw(""))
    }

    @Test
    fun `family label falls back to the primary person then a neutral label`() {
        assertEquals("Kumar family", UserPreferences(familyLabel = "Kumar family").displayFamilyName)
        assertEquals("Asha's family", UserPreferences(primaryUserName = " Asha ").displayFamilyName)
        assertEquals("Your family", UserPreferences().displayFamilyName)
    }
}

class HindiSeedTest {

    @Test
    fun `every seeded record has a Hindi title and description`() {
        DefaultChecklist.ITEMS.forEach { item ->
            assertTrue("Missing Hindi title for ${item.title}", item.titleHi.isNotBlank())
            assertTrue("Missing Hindi description for ${item.title}", item.descriptionHi.isNotBlank())
            assertEquals(item.titleHi, item.displayTitle(hindi = true))
            assertEquals(item.title, item.displayTitle(hindi = false))
        }
        assertEquals(31, DefaultChecklist.HINDI_BY_ORDER.size)
    }

    @Test
    fun `hindi family label uses the hindi suffix`() {
        assertEquals("Asha का परिवार", UserPreferences(primaryUserName = "Asha", language = com.puneetfsn.kutumbcare.data.AppLanguage.HINDI).displayFamilyName)
        assertEquals("आपका परिवार", UserPreferences(language = com.puneetfsn.kutumbcare.data.AppLanguage.HINDI).displayFamilyName)
    }
}

class EntryFieldsTest {

    private fun seeded(sortOrder: Int) = DefaultChecklist.ITEMS.first { it.sortOrder == sortOrder }

    @Test
    fun `Aadhaar never asks for a branch and never offers a full number field`() {
        val fields = com.puneetfsn.kutumbcare.data.EntryFieldsCatalog.forItem(seeded(10))
        assertEquals(null, fields.primary)
        assertTrue(fields.ref!!.en.contains("last 4"))
        assertTrue(fields.guidanceEn.contains("masked Aadhaar"))
        assertEquals(false, fields.nomineeRelevant)
    }

    @Test
    fun `bank and LIC records keep nominee fields`() {
        assertTrue(com.puneetfsn.kutumbcare.data.EntryFieldsCatalog.forItem(seeded(50)).nomineeRelevant)
        val lic = com.puneetfsn.kutumbcare.data.EntryFieldsCatalog.forItem(seeded(140))
        assertTrue(lic.nomineeRelevant)
        assertTrue(lic.ref!!.en.contains("Policy"))
    }

    @Test
    fun `digital online assets ask where the password is kept, never the password`() {
        val gmail = com.puneetfsn.kutumbcare.data.EntryFieldsCatalog.forItem(seeded(250))
        assertTrue(gmail.location!!.en.contains("Password kept at"))
        assertTrue(gmail.guidanceEn.contains("Inactive Account Manager"))
        val manager = com.puneetfsn.kutumbcare.data.EntryFieldsCatalog.forItem(seeded(290))
        assertTrue(manager.contact!!.en.contains("Emergency access"))
        // No seeded digital record may solicit an actual password as a field label.
        DefaultChecklist.ITEMS.filter { it.categoryEnum == AssetCategory.DIGITAL_LEGACY }.forEach { item ->
            val fields = com.puneetfsn.kutumbcare.data.EntryFieldsCatalog.forItem(item)
            listOfNotNull(fields.primary, fields.ref, fields.contact, fields.phone).forEach { label ->
                assertTrue(
                    "Label '${label.en}' must not ask for a password",
                    !label.en.matches(Regex("(?i)^password$|enter.*password|your password"))
                )
            }
        }
    }

    @Test
    fun `digital online assets are seeded`() {
        val titles = DefaultChecklist.ITEMS.map { it.title }
        assertTrue(titles.any { it.contains("Gmail") })
        assertTrue(titles.any { it.contains("Apple ID") })
        assertTrue(titles.any { it.contains("Income Tax") })
        assertTrue(titles.any { it.contains("Password manager") })
        assertEquals(10, DefaultChecklist.ITEMS.count { it.categoryEnum == AssetCategory.DIGITAL_LEGACY })
    }
}

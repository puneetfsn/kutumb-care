package com.puneetfsn.kutumbcare.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetCategory(
    val displayName: String,
    val subtitle: String,
    val requiresNominee: Boolean
) {
    IDENTITY_GOVT("Identity & civil", "Aadhaar, PAN, passport, will", false),
    BANKING_FUNDS("Banking & funds", "Accounts, FDs, lockers, cards", true),
    INVESTMENTS("Investments", "Demat, EPF, PPF, NPS, gold", true),
    INSURANCE("Insurance", "Life, health, vehicle", true),
    REAL_ESTATE_LOANS("Property & loans", "Deeds, khata, home loans", false),
    DIGITAL_LEGACY("Digital online assets", "Gmail, Apple ID, ITR, password manager", false),
    TRUSTED_CONTACTS("Trusted contacts", "CA, lawyer, agent, family", false);

    val icon: ImageVector
        get() = when (this) {
            IDENTITY_GOVT -> Icons.Outlined.Badge
            BANKING_FUNDS -> Icons.Outlined.AccountBalance
            INVESTMENTS -> Icons.Outlined.TrendingUp
            INSURANCE -> Icons.Outlined.HealthAndSafety
            REAL_ESTATE_LOANS -> Icons.Outlined.HomeWork
            DIGITAL_LEGACY -> Icons.Outlined.PhonelinkLock
            TRUSTED_CONTACTS -> Icons.Outlined.PeopleOutline
        }

    companion object {
        fun fromRaw(raw: String): AssetCategory =
            entries.find { it.name == raw } ?: IDENTITY_GOVT
    }
}

enum class NomineeStatus(val displayName: String) {
    NOMINEE_VERIFIED("Nominee verified"),
    NEEDS_NOMINEE("Needs nominee"),
    NOT_APPLICABLE("Not applicable");

    companion object {
        fun fromRaw(raw: String): NomineeStatus =
            entries.find { it.name == raw } ?: NOT_APPLICABLE
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** App language chosen on first launch. Stored locally; never leaves the device. */
enum class AppLanguage {
    ENGLISH, HINDI;

    val isHindi: Boolean get() = this == HINDI

    companion object {
        fun fromRaw(raw: String?): AppLanguage = entries.find { it.name == raw } ?: ENGLISH
    }
}

@Entity(tableName = "checklist_items")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val nomineeStatus: String = NomineeStatus.NOT_APPLICABLE.name,
    val institutionName: String = "",
    val accountRef: String = "",
    val physicalLocation: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val additionalNotes: String = "",
    /** Reusable nominee cards attached to this record, with shares totalling 100. */
    val nominee1Id: String = "",
    val nominee1Share: Int = 0,
    val nominee2Id: String = "",
    val nominee2Share: Int = 0,
    val isUserCreated: Boolean = false,
    /** Hindi rendering of the seeded title/description. Empty for user records. */
    val titleHi: String = "",
    val descriptionHi: String = "",
    val sortOrder: Int = 0,
    /** Which per-entry template (EntryFieldsCatalog key) this record uses.
     *  Duplicates created with "+" keep the template even though sortOrder is shared. */
    val specOrder: Int = 0,
    /** Maturity / expiry date as typed by the family (FD, RD, MF, PPF, passport…). */
    val maturityDate: String = "",
    /** JSON array of document-photo file names in the app-private documents folder. */
    val attachmentsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val categoryEnum: AssetCategory get() = AssetCategory.fromRaw(category)
    val nomineeEnum: NomineeStatus get() = NomineeStatus.fromRaw(nomineeStatus)
    val hasLocation: Boolean get() = physicalLocation.isNotBlank()

    /** Title to show for [hindi]; falls back to the stored (English) title. */
    fun displayTitle(hindi: Boolean): String =
        if (hindi && titleHi.isNotBlank()) titleHi else title

    fun displayDescription(hindi: Boolean): String =
        if (hindi && descriptionHi.isNotBlank()) descriptionHi else description

    val attachmentNames: List<String>
        get() = runCatching {
            val arr = org.json.JSONArray(attachmentsJson)
            (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())

    val nomineeAssignments: List<NomineeAssignment>
        get() = listOf(
            NomineeAssignment(nominee1Id, nominee1Share),
            NomineeAssignment(nominee2Id, nominee2Share)
        ).filter { it.profileId.isNotBlank() }
}

data class UserPreferences(
    val pinHash: String = "",
    val pinEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryUserName: String = "",
    val familyLabel: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val setupComplete: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    /** False until the first-launch English / हिन्दी chooser has been answered. */
    val languageChosen: Boolean = false,
    /** Saved nominee cards, shared by every record (stored in DataStore). */
    val nominees: List<NomineeProfile> = emptyList(),
    /** Lock the moment the app leaves the foreground (no 30s grace). */
    val lockInstant: Boolean = false,
    /** Erase everything after 10 wrong unlock tries (owner-enabled). */
    val autoWipe: Boolean = false
) {
    val displayFamilyName: String
        get() = familyName(language.isHindi)

    fun familyName(hindi: Boolean): String = when {
        familyLabel.isNotBlank() -> familyLabel
        primaryUserName.isNotBlank() ->
            if (hindi) "${primaryUserName.trim()} का परिवार" else "${primaryUserName.trim()}'s family"
        else -> if (hindi) "आपका परिवार" else "Your family"
    }
}

data class CategoryProgress(
    val category: AssetCategory,
    val completed: Int,
    val total: Int
) {
    val percent: Int get() = if (total == 0) 0 else (completed * 100) / total
}

data class DashboardSnapshot(
    val items: List<ChecklistItem> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val readinessPercent: Int = 0,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val missingNomineeItems: List<ChecklistItem> = emptyList(),
    val categoryProgress: List<CategoryProgress> = emptyList()
)

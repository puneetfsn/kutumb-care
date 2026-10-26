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
    DIGITAL_LEGACY("Digital legacy", "Email, phone, DigiLocker", false),
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
    val isUserCreated: Boolean = false,
    val sortOrder: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val categoryEnum: AssetCategory get() = AssetCategory.fromRaw(category)
    val nomineeEnum: NomineeStatus get() = NomineeStatus.fromRaw(nomineeStatus)
    val hasLocation: Boolean get() = physicalLocation.isNotBlank()
}

data class UserPreferences(
    val pinHash: String = "",
    val pinEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryUserName: String = "",
    val familyLabel: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val setupComplete: Boolean = false
) {
    val displayFamilyName: String
        get() = when {
            familyLabel.isNotBlank() -> familyLabel
            primaryUserName.isNotBlank() -> "${primaryUserName.trim()}'s family"
            else -> "Your family"
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

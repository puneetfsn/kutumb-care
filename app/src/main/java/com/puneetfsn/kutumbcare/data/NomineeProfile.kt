package com.puneetfsn.kutumbcare.data

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import org.json.JSONArray
import org.json.JSONObject

/**
 * A reusable nominee "card". Families save a person once — full name,
 * relationship, date of birth, address and contacts — then attach the card to
 * any bank / investment / insurance record with a share percentage instead of
 * re-typing the details on every record.
 *
 * Age is never stored; it is derived from [dateOfBirth] so it cannot go stale.
 * [idLast4] accepts only the last 4 characters of PAN/Aadhaar — the full
 * numbers should not be written down (UIDAI masked-Aadhaar guidance).
 */
data class NomineeProfile(
    val id: String,
    val fullName: String,
    val relationship: String = "",
    /** dd/MM/yyyy, as families in India usually write it. */
    val dateOfBirth: String = "",
    val address: String = "",
    val mobile: String = "",
    val email: String = "",
    val idLast4: String = ""
) {
    fun ageYears(today: LocalDate = LocalDate.now()): Int? {
        val dob = parseDob(dateOfBirth) ?: return null
        if (dob.isAfter(today)) return null
        return Period.between(dob, today).years
    }

    /** "Meena Kumari · Wife · 42 yrs" style one-liner for pickers and PDFs. */
    fun summaryLine(): String = buildList {
        add(fullName)
        if (relationship.isNotBlank()) add(relationship)
        ageYears()?.let { add("$it yrs") }
    }.joinToString(" · ")

    companion object {
        // STRICT so 31/02/1985 is rejected instead of rolled to 28/02; uuuu (proleptic
        // year) is required for strict parsing without an era field.
        private val DOB_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

        fun parseDob(raw: String): LocalDate? =
            runCatching { LocalDate.parse(raw.trim(), DOB_FORMAT) }.getOrNull()

        fun isValidDob(raw: String): Boolean = raw.isBlank() || parseDob(raw) != null

        fun newId(): String = "nom-" + System.currentTimeMillis().toString(36) +
            "-" + (1000..9999).random()
    }
}

/** One nominee attached to a record, with their share of the proceeds. */
data class NomineeAssignment(val profileId: String, val sharePercent: Int)

object NomineeShares {
    /** Shares of all selected nominees; a complete nomination totals exactly 100. */
    fun total(assignments: List<NomineeAssignment>): Int =
        assignments.filter { it.profileId.isNotBlank() }.sumOf { it.sharePercent }

    fun isComplete(assignments: List<NomineeAssignment>): Boolean =
        assignments.any { it.profileId.isNotBlank() } && total(assignments) == 100

    /** The 1×100% quick split. */
    fun single(profileId: String): List<NomineeAssignment> =
        if (profileId.isBlank()) emptyList() else listOf(NomineeAssignment(profileId, 100))

    /** The 2×50% quick split (needs two distinct people). */
    fun fiftyFifty(firstId: String, secondId: String): List<NomineeAssignment> =
        listOf(firstId, secondId)
            .filter { it.isNotBlank() }
            .distinct()
            .map { NomineeAssignment(it, 50) }
            .let { if (it.size == 1) single(it.first().profileId) else it }
}

/** JSON for DataStore and for the backup file (one format, both places). */
object NomineesCodec {

    fun encode(profiles: List<NomineeProfile>): String {
        val array = JSONArray()
        profiles.forEach { array.put(it.toJson()) }
        return array.toString()
    }

    fun decode(text: String?): List<NomineeProfile> {
        if (text.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(text)
            buildList {
                for (i in 0 until array.length()) {
                    fromJson(array.getJSONObject(i))?.let { add(it) }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun NomineeProfile.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("fullName", fullName)
        put("relationship", relationship)
        put("dateOfBirth", dateOfBirth)
        put("address", address)
        put("mobile", mobile)
        put("email", email)
        put("idLast4", idLast4)
    }

    fun fromJson(obj: JSONObject): NomineeProfile? {
        val name = obj.optString("fullName")
        if (name.isBlank()) return null
        return NomineeProfile(
            id = obj.optString("id").ifBlank { NomineeProfile.newId() },
            fullName = name,
            relationship = obj.optString("relationship"),
            dateOfBirth = obj.optString("dateOfBirth"),
            address = obj.optString("address"),
            mobile = obj.optString("mobile"),
            email = obj.optString("email"),
            idLast4 = obj.optString("idLast4")
        )
    }
}

package com.puneetfsn.kutumbcare.data

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Serialises the family dossier to and from the JSON backup file.
 *
 * The optional app PIN is deliberately absent from [encode]: neither
 * [UserPreferences.pinHash] nor `pinEnabled` is written, so a backup file can
 * never be replayed to unlock the app. Passwords, UPI PINs, ATM PINs, OTPs and
 * CVVs are not part of the data model to begin with.
 *
 * Kept free of Android and disk APIs so the privacy contract can be unit tested.
 */
object BackupFormat {

    const val APP_ID = "kutumb-care"
    const val SCHEMA_VERSION = 1

    /** Profile fields are nullable so an older backup cannot blank out newer answers. */
    data class Profile(
        val primaryUserName: String? = null,
        val familyLabel: String? = null,
        val emergencyContactName: String? = null,
        val emergencyContactPhone: String? = null,
        val themeMode: ThemeMode? = null
    )

    data class Payload(
        val items: List<ChecklistItem>,
        val profile: Profile?
    )

    fun encode(items: List<ChecklistItem>, preferences: UserPreferences): String {
        val root = JSONObject().apply {
            put("app", APP_ID)
            put("version", SCHEMA_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("privacy", "This backup never contains passwords, PINs, OTPs or CVVs.")
            put("profile", JSONObject().apply {
                put("primaryUserName", preferences.primaryUserName)
                put("familyLabel", preferences.familyLabel)
                put("emergencyContactName", preferences.emergencyContactName)
                put("emergencyContactPhone", preferences.emergencyContactPhone)
                put("themeMode", preferences.themeMode.name)
            })
            put("items", JSONArray().also { array ->
                items.forEach { item ->
                    array.put(JSONObject().apply {
                        put("category", item.category)
                        put("title", item.title)
                        put("description", item.description)
                        put("isCompleted", item.isCompleted)
                        put("nomineeStatus", item.nomineeStatus)
                        put("institutionName", item.institutionName)
                        put("accountRef", item.accountRef)
                        put("physicalLocation", item.physicalLocation)
                        put("contactName", item.contactName)
                        put("contactPhone", item.contactPhone)
                        put("additionalNotes", item.additionalNotes)
                        put("isUserCreated", item.isUserCreated)
                        put("sortOrder", item.sortOrder)
                        put("updatedAt", item.updatedAt)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun decode(text: String): Payload {
        val root = try {
            JSONObject(text)
        } catch (error: JSONException) {
            throw IllegalArgumentException("That file is not valid JSON.", error)
        }
        require(root.optString("app") == APP_ID) {
            "This file is not a Kutumb Care backup."
        }
        require(root.optInt("version", 0) <= SCHEMA_VERSION) {
            "This backup was created by a newer version of Kutumb Care."
        }
        val array = root.optJSONArray("items")
            ?: throw IllegalArgumentException("This backup has no records to restore.")
        require(array.length() > 0) { "This backup has no records to restore." }

        val items = buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title")
                if (title.isBlank()) continue
                add(
                    ChecklistItem(
                        category = AssetCategory.fromRaw(
                            obj.optString("category", AssetCategory.IDENTITY_GOVT.name)
                        ).name,
                        title = title,
                        description = obj.optString("description"),
                        isCompleted = obj.optBoolean("isCompleted"),
                        nomineeStatus = NomineeStatus.fromRaw(
                            obj.optString("nomineeStatus", NomineeStatus.NOT_APPLICABLE.name)
                        ).name,
                        institutionName = obj.optString("institutionName"),
                        accountRef = obj.optString("accountRef"),
                        physicalLocation = obj.optString("physicalLocation"),
                        contactName = obj.optString("contactName"),
                        contactPhone = obj.optString("contactPhone"),
                        additionalNotes = obj.optString("additionalNotes"),
                        isUserCreated = obj.optBoolean("isUserCreated"),
                        sortOrder = obj.optInt("sortOrder", i * 10),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }
        require(items.isNotEmpty()) { "This backup has no records to restore." }
        return Payload(items = items, profile = root.optJSONObject("profile")?.toProfile())
    }

    private fun JSONObject.toProfile(): Profile = Profile(
        primaryUserName = stringOrNull("primaryUserName"),
        familyLabel = stringOrNull("familyLabel"),
        emergencyContactName = stringOrNull("emergencyContactName"),
        emergencyContactPhone = stringOrNull("emergencyContactPhone"),
        themeMode = stringOrNull("themeMode")?.let { raw ->
            ThemeMode.entries.firstOrNull { it.name == raw }
        }
    )

    private fun JSONObject.stringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}

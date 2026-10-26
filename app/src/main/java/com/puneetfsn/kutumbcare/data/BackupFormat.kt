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
        val profile: Profile?,
        /** Null when the backup predates nominee cards (keep what the family has). */
        val nominees: List<NomineeProfile>? = null
    )

    /**
     * [attachmentData] maps item id → base64 JPEG payloads, supplied by the
     * caller (kept out of this pure formatter). The web app writes the same
     * field as data URLs; decoders accept both, so backups stay compatible.
     */
    fun encode(
        items: List<ChecklistItem>,
        preferences: UserPreferences,
        attachmentData: Map<Long, List<String>> = emptyMap()
    ): String {
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
            put("nominees", JSONArray().also { array ->
                preferences.nominees.forEach { profile ->
                    with(NomineesCodec) { array.put(profile.toJson()) }
                }
            })
            put("items", JSONArray().also { array ->
                items.forEach { item ->
                    array.put(JSONObject().apply {
                        put("category", item.category)
                        put("title", item.title)
                        put("description", item.description)
                        put("titleHi", item.titleHi)
                        put("descriptionHi", item.descriptionHi)
                        put("isCompleted", item.isCompleted)
                        put("nomineeStatus", item.nomineeStatus)
                        put("institutionName", item.institutionName)
                        put("accountRef", item.accountRef)
                        put("physicalLocation", item.physicalLocation)
                        put("contactName", item.contactName)
                        put("contactPhone", item.contactPhone)
                        put("additionalNotes", item.additionalNotes)
                        put("nominee1Id", item.nominee1Id)
                        put("nominee1Share", item.nominee1Share)
                        put("nominee2Id", item.nominee2Id)
                        put("nominee2Share", item.nominee2Share)
                        put("isUserCreated", item.isUserCreated)
                        put("sortOrder", item.sortOrder)
                        put("specId", item.specOrder)
                        put("maturityDate", item.maturityDate)
                        put("attachments", JSONArray(attachmentData[item.id] ?: emptyList<String>()))
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
                        titleHi = obj.optString("titleHi"),
                        descriptionHi = obj.optString("descriptionHi"),
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
                        nominee1Id = obj.optString("nominee1Id"),
                        nominee1Share = obj.optInt("nominee1Share"),
                        nominee2Id = obj.optString("nominee2Id"),
                        nominee2Share = obj.optInt("nominee2Share"),
                        isUserCreated = obj.optBoolean("isUserCreated"),
                        sortOrder = obj.optInt("sortOrder", i * 10),
                        specOrder = obj.optInt("specId"),
                        maturityDate = obj.optString("maturityDate"),
                        attachmentsJson = obj.optJSONArray("attachments")?.let { arr ->
                            JSONArray().also { out ->
                                for (j in 0 until arr.length()) out.put(arr.optString(j))
                            }.toString()
                        } ?: "[]",
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }
        require(items.isNotEmpty()) { "This backup has no records to restore." }
        val nominees = root.optJSONArray("nominees")?.let { nomineeArray ->
            buildList {
                for (i in 0 until nomineeArray.length()) {
                    NomineesCodec.fromJson(nomineeArray.getJSONObject(i))?.let { add(it) }
                }
            }
        }
        return Payload(
            items = items,
            profile = root.optJSONObject("profile")?.toProfile(),
            nominees = nominees
        )
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

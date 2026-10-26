package com.puneetfsn.kutumbcare.data

import android.content.Context
import java.io.InputStream
import java.io.OutputStream
import org.json.JSONArray
import org.json.JSONObject

@Suppress("UNUSED_PARAMETER")
class BackupManager(
    context: Context,
    private val checklistRepository: ChecklistRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun exportTo(stream: OutputStream) {
        val items = checklistRepository.getAll()
        val prefs = settingsRepository.snapshot()
        val root = JSONObject().apply {
            put("app", "kutumb-care")
            put("version", SCHEMA_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("privacy", "This backup never contains passwords, PINs, OTPs or CVVs.")
            put("profile", JSONObject().apply {
                put("primaryUserName", prefs.primaryUserName)
                put("familyLabel", prefs.familyLabel)
                put("emergencyContactName", prefs.emergencyContactName)
                put("emergencyContactPhone", prefs.emergencyContactPhone)
                put("themeMode", prefs.themeMode.name)
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
        stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
        stream.flush()
    }

    suspend fun importFrom(stream: InputStream) {
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        if (root.optString("app") != "kutumb-care") {
            error("This file is not a Kutumb Care backup.")
        }
        if (root.optInt("version", 0) > SCHEMA_VERSION) {
            error("This backup was created by a newer version of Kutumb Care.")
        }
        val profile = root.optJSONObject("profile")
        if (profile != null) {
            // Keep whatever the family already typed in when a field is absent.
            val current = settingsRepository.snapshot()
            settingsRepository.updateProfile(
                primaryUserName = profile.optString("primaryUserName", current.primaryUserName),
                familyLabel = profile.optString("familyLabel", current.familyLabel),
                emergencyContactName = profile.optString("emergencyContactName", current.emergencyContactName),
                emergencyContactPhone = profile.optString("emergencyContactPhone", current.emergencyContactPhone)
            )
            runCatching {
                settingsRepository.setThemeMode(
                    ThemeMode.valueOf(profile.optString("themeMode", current.themeMode.name))
                )
            }
        }
        val array = root.optJSONArray("items")
            ?: error("This backup has no records to restore.")
        if (array.length() == 0) error("This backup has no records to restore.")
        val items = buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    ChecklistItem(
                        category = obj.optString("category", AssetCategory.IDENTITY_GOVT.name),
                        title = obj.optString("title"),
                        description = obj.optString("description"),
                        isCompleted = obj.optBoolean("isCompleted"),
                        nomineeStatus = obj.optString("nomineeStatus", NomineeStatus.NOT_APPLICABLE.name),
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
        checklistRepository.replaceAll(items)
    }

    private companion object {
        /** Bump when the JSON layout changes, and keep imports backward compatible. */
        const val SCHEMA_VERSION = 1
    }
}

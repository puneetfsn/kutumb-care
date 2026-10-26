package com.puneetfsn.kutumbcare.data

import android.content.Context
import java.io.InputStream
import java.io.OutputStream
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val context: Context,
    private val checklistRepository: ChecklistRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun exportTo(stream: OutputStream) {
        val items = checklistRepository.getAll()
        val prefs = settingsRepository.snapshot()
        val root = JSONObject().apply {
            put("app", "kutumb-care")
            put("version", 1)
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
        val profile = root.optJSONObject("profile")
        if (profile != null) {
            settingsRepository.updateProfile(
                primaryUserName = profile.optString("primaryUserName"),
                familyLabel = profile.optString("familyLabel"),
                emergencyContactName = profile.optString("emergencyContactName"),
                emergencyContactPhone = profile.optString("emergencyContactPhone")
            )
            runCatching {
                settingsRepository.setThemeMode(ThemeMode.valueOf(profile.optString("themeMode", "SYSTEM")))
            }
        }
        val array = root.getJSONArray("items")
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

    fun cacheBackupFile(): java.io.File {
        val dir = java.io.File(context.cacheDir, "backup").apply { mkdirs() }
        return java.io.File(dir, "kutumb-care-backup.json")
    }
}

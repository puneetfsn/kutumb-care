package com.puneetfsn.kutumbcare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ChecklistItem::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checklistDao(): ChecklistDao

    companion object {
        /**
         * 1 → 2 adds the Hindi title/description columns and fills them for the
         * seeded records (matched by sortOrder). Everything the family typed —
         * locations, contacts, nominee answers — is untouched.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN titleHi TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN descriptionHi TEXT NOT NULL DEFAULT ''")
                DefaultChecklist.HINDI_BY_ORDER.forEach { (order, hindi) ->
                    val title = hindi.first.replace("'", "''")
                    val description = hindi.second.replace("'", "''")
                    db.execSQL(
                        "UPDATE checklist_items SET titleHi = '$title', descriptionHi = '$description' " +
                            "WHERE sortOrder = $order AND isUserCreated = 0"
                    )
                }
            }
        }

        /** 2 → 3 adds the two nominee-card slots (id + share %) per record. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN nominee1Id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN nominee1Share INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN nominee2Id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE checklist_items ADD COLUMN nominee2Share INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kutumb_care.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

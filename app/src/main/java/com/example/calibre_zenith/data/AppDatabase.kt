package com.example.calibre_zenith.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.calibre_zenith.data.combat.ActiveBoss
import com.example.calibre_zenith.data.combat.BossTemplate
import com.example.calibre_zenith.data.combat.CombatDao

@Database(
    entities = [
        BossTemplate::class,
        ActiveBoss::class
    ],
    version = 3,          // ← bumped from 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun combatDao(): CombatDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Adds tags + bossImageUrl default columns to existing installs
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE boss_templates ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE boss_templates ADD COLUMN bossImageUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calibre_zenith_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
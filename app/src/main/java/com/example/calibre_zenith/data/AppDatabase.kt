package com.example.calibre_zenith.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.calibre_zenith.data.combat.ActiveBoss
import com.example.calibre_zenith.data.combat.BossTemplate
import com.example.calibre_zenith.data.combat.CombatDao

@Database(
    entities = [
        BossTemplate::class,
        ActiveBoss::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun combatDao(): CombatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calibre_zenith_db"
                ).fallbackToDestructiveMigration()  // ← add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
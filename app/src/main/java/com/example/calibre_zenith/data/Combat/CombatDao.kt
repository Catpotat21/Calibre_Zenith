package com.example.calibre_zenith.data.combat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CombatDao {

    // --- Boss Templates ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBossTemplate(boss: BossTemplate)

    @Query("SELECT * FROM boss_templates")
    fun getAllBossTemplates(): Flow<List<BossTemplate>>

    @Query("SELECT * FROM boss_templates WHERE id = :id")
    suspend fun getBossTemplateById(id: Int): BossTemplate?

    // --- Active Boss ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveBoss(activeBoss: ActiveBoss)

    @Query("SELECT * FROM active_boss LIMIT 1")
    fun getActiveBoss(): Flow<ActiveBoss?>

    @Query("UPDATE active_boss SET currentHp = :hp WHERE id = :id")
    suspend fun updateActiveBossHp(id: Int, hp: Int)

    @Query("DELETE FROM active_boss WHERE id = :id")
    suspend fun deleteActiveBoss(id: Int)
}
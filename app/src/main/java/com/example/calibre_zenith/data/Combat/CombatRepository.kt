package com.example.calibre_zenith.data.combat

import android.content.Context
import com.example.calibre_zenith.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class CombatRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).combatDao()

    // --- Boss Templates ---
    fun getAllBossTemplates(): Flow<List<BossTemplate>> =
        dao.getAllBossTemplates()

    suspend fun insertBossTemplate(boss: BossTemplate) =
        dao.insertBossTemplate(boss)

    suspend fun getBossTemplateById(id: Int): BossTemplate? =
        dao.getBossTemplateById(id)

    // --- Active Boss ---
    fun getActiveBoss(): Flow<ActiveBoss?> =
        dao.getActiveBoss()

    suspend fun spawnBoss(templateId: Int, hp: Int) {
        dao.insertActiveBoss(
            ActiveBoss(
                currentHp = hp,
                bossTemplateId = templateId
            )
        )
    }

    suspend fun updateActiveBossHp(id: Int, hp: Int) =
        dao.updateActiveBossHp(id, hp)

    suspend fun defeatActiveBoss(id: Int) =
        dao.deleteActiveBoss(id)
}
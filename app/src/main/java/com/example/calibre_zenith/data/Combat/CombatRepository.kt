package com.example.calibre_zenith.data.combat

import android.content.Context
import com.example.calibre_zenith.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class CombatRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).combatDao()

    // ── Boss Templates ─────────────────────────────────────────
    fun getAllBossTemplates(): Flow<List<BossTemplate>> =
        dao.getAllBossTemplates()

    suspend fun insertBossTemplate(boss: BossTemplate) =
        dao.insertBossTemplate(boss)

    suspend fun getBossTemplateById(id: Int): BossTemplate? =
        dao.getBossTemplateById(id)

    suspend fun deleteBossTemplate(id: Int) {
        dao.deleteActiveBossByTemplateId(id)  // remove active boss first
        dao.deleteBossTemplate(id)
    }

    // ── Active Bosses ──────────────────────────────────────────
    fun getAllActiveBosses(): Flow<List<ActiveBoss>> =
        dao.getAllActiveBosses()

    suspend fun spawnBossIfNotActive(templateId: Int, hp: Int) {
        val existing = dao.getActiveBossByTemplateId(templateId)
        if (existing == null) {
            dao.insertActiveBoss(
                ActiveBoss(currentHp = hp, bossTemplateId = templateId)
            )
        }
    }

    // Called when a task is checked off
    // Finds the active boss for the given templateId and drains HP
    suspend fun drainHp(templateId: Int, damage: Int) {
        val active = dao.getActiveBossByTemplateId(templateId) ?: return
        val newHp = (active.currentHp - damage).coerceAtLeast(0)
        if (newHp <= 0) {
            dao.deleteActiveBoss(active.id)
        } else {
            dao.updateActiveBossHp(active.id, newHp)
        }
    }

    suspend fun deleteActiveBoss(id: Int) =
        dao.deleteActiveBoss(id)

    suspend fun getActiveBossWithTemplateByTag(tag: String): Pair<ActiveBoss, BossTemplate>? {
        val activeList = dao.getAllActiveBossesSync()
        android.util.Log.d("CombatRepository", "getActiveBossWithTemplateByTag: Searching for '$tag'. Active bosses count: ${activeList.size}")
        for (active in activeList) {
            val template = dao.getBossTemplateById(active.bossTemplateId)
            if (template != null) {
                val tags = template.tags.split(",").map { it.trim().lowercase() }
                android.util.Log.d("CombatRepository", "Boss '${template.name}' has tags: $tags")
                if (tags.contains(tag.lowercase())) {
                    return Pair(active, template)
                }
            }
        }
        return null
    }
}
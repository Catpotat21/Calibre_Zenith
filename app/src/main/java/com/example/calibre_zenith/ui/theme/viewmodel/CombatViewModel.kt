package com.example.calibre_zenith.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calibre_zenith.data.combat.BossTemplate
import com.example.calibre_zenith.data.combat.ActiveBoss
import com.example.calibre_zenith.data.combat.CombatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CombatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CombatRepository(application)

    // ── All templates the user has created ─────────────────────
    val bossTemplates = repository.getAllBossTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── All currently active bosses (multiple simultaneous) ────
    val activeBosses = repository.getAllActiveBosses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── Which boss is currently being attacked in the UI ───────
    private val _attackingBossId = MutableStateFlow<Int?>(null)
    val attackingBossId: StateFlow<Int?> = _attackingBossId.asStateFlow()

    // ── Boss just defeated — show banner ───────────────────────
    private val _defeatedBossName = MutableStateFlow<String?>(null)
    val defeatedBossName: StateFlow<String?> = _defeatedBossName.asStateFlow()

    // ── Create a new boss template ──────────────────────────────
    fun createBossTemplate(
        name: String,
        baseHp: Int,
        imageUrl: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            repository.insertBossTemplate(
                BossTemplate(
                    name = name,
                    baseHp = baseHp,
                    bossImageUrl = imageUrl,
                    tags = tags.joinToString(",")
                )
            )
        }
    }

    // ── Delete boss template + its active boss ──────────────────
    fun deleteBossTemplate(id: Int) {
        viewModelScope.launch {
            repository.deleteBossTemplate(id)
        }
    }

    // ── Spawn a boss into the active arena ─────────────────────
    fun spawnBoss(templateId: Int, hp: Int) {
        viewModelScope.launch {
            repository.spawnBossIfNotActive(templateId, hp)
        }
    }

    // ── Manual attack from CombatScreen ────────────────────────
    fun attackBoss(activeBossId: Int, templateId: Int, damage: Int) {
        viewModelScope.launch {
            _attackingBossId.value = activeBossId

            val template = bossTemplates.value.find { it.id == templateId }
            repository.drainHp(templateId, damage)

            // Check if boss was defeated
            val stillAlive = activeBosses.value.any { it.id == activeBossId }
            if (!stillAlive) {
                _defeatedBossName.value = template?.name ?: "BOSS"
            }

            kotlinx.coroutines.delay(300)
            _attackingBossId.value = null
        }
    }

    // ── Called from task checkbox in Roadmap or Planner ────────
    // Finds the boss assigned to this task and drains its HP
    fun drainHpForTask(assignedBossId: Int?, hpDrain: Int) {
        if (assignedBossId == null) return
        viewModelScope.launch {
            val template = bossTemplates.value.find { it.id == assignedBossId }
            repository.drainHp(assignedBossId, hpDrain)

            // Check defeat after drain
            val active = activeBosses.value.find { it.bossTemplateId == assignedBossId }
            val newHp = (active?.currentHp ?: 0) - hpDrain
            if (newHp <= 0) {
                _defeatedBossName.value = template?.name ?: "BOSS"
            }
        }
    }

    // ── Dismiss defeat banner ───────────────────────────────────
    fun clearDefeatedBoss() {
        _defeatedBossName.value = null
    }
}
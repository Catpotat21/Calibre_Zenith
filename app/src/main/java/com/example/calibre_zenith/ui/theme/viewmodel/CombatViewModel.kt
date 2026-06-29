package com.example.calibre_zenith.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calibre_zenith.data.combat.BossTemplate
import com.example.calibre_zenith.data.combat.CombatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CombatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CombatRepository(application)

    // --- Boss Templates ---
    val bossTemplates = repository.getAllBossTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Active Boss ---
    val activeBoss = repository.getActiveBoss()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --- Current template for active boss (resolved) ---
    private val _activeBossTemplate = MutableStateFlow<BossTemplate?>(null)
    val activeBossTemplate: StateFlow<BossTemplate?> = _activeBossTemplate.asStateFlow()

    // --- HP animation state ---
    private val _isAttacking = MutableStateFlow(false)
    val isAttacking: StateFlow<Boolean> = _isAttacking.asStateFlow()

    private val _isBossDefeated = MutableStateFlow(false)
    val isBossDefeated: StateFlow<Boolean> = _isBossDefeated.asStateFlow()

    // --- Seed default bosses on first launch ---
    fun seedDefaultBossesIfEmpty() {
        viewModelScope.launch {
            if (bossTemplates.value.isEmpty()) {
                val defaults = listOf(
                    BossTemplate(name = "Procrastination Wraith", baseHp = 100, bossImageUrl = ""),
                    BossTemplate(name = "Perfectionism Golem", baseHp = 150, bossImageUrl = ""),
                    BossTemplate(name = "Distraction Hydra", baseHp = 200, bossImageUrl = "")
                )
                defaults.forEach { repository.insertBossTemplate(it) }
            }
        }
    }

    // --- Spawn a boss from a template ---
    fun spawnBoss(templateId: Int, hp: Int) {
        viewModelScope.launch {
            repository.spawnBoss(templateId, hp)
            _activeBossTemplate.value = repository.getBossTemplateById(templateId)
            _isBossDefeated.value = false
        }
    }

    // --- Deal damage to active boss ---
    fun attackBoss(damagePoints: Int) {
        val current = activeBoss.value ?: return
        val newHp = (current.currentHp - damagePoints).coerceAtLeast(0)

        viewModelScope.launch {
            _isAttacking.value = true
            repository.updateActiveBossHp(current.id, newHp)

            if (newHp <= 0) {
                repository.defeatActiveBoss(current.id)
                _isBossDefeated.value = true
            }

            kotlinx.coroutines.delay(300)
            _isAttacking.value = false
        }
    }

    // --- Reset defeat state ---
    fun resetDefeatState() {
        _isBossDefeated.value = false
    }
}
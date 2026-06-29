package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.data.combat.ActiveBoss
import com.example.calibre_zenith.data.combat.BossTemplate
import com.example.calibre_zenith.ui.viewmodel.CombatViewModel
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel

private val CyberCyan   = Color(0xFF00F5FF)
private val CyberRed    = Color(0xFFFF003C)
private val CyberYellow = Color(0xFFFFE600)
private val DarkBg      = Color(0xFF0A0A0F)
private val PanelBg     = Color(0xFF12121A)
private val BorderColor = Color(0xFF2A2A3A)

@Composable
fun CombatScreen(
    pauseViewModel: PauseViewModel,
    combatViewModel: CombatViewModel
) {
    val activeBosses by combatViewModel.activeBosses.collectAsState(initial = emptyList())
    val bossTemplates by combatViewModel.bossTemplates.collectAsState(initial = emptyList())
    val attackingBossId by combatViewModel.attackingBossId.collectAsState()
    val defeatedBossName by combatViewModel.defeatedBossName.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚔ COMBAT ARENA",
                color = CyberCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = CyberCyan.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            if (defeatedBossName != null) {
                DefeatBanner(
                    bossName = defeatedBossName ?: "BOSS",
                    onContinue = { combatViewModel.clearDefeatedBoss() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activeBosses) { boss ->
                    val template = bossTemplates.find { it.id == boss.bossTemplateId }

                    ActiveCombatPanel(
                        boss = boss,
                        bossName = template?.name ?: "UNKNOWN BOSS",
                        maxHp = template?.baseHp ?: boss.currentHp,
                        isAttacking = attackingBossId == boss.id,
                        onAttack = {
                            combatViewModel.attackBoss(
                                activeBossId = boss.id,
                                templateId = boss.bossTemplateId,
                                damage = 10
                            )
                        },
                        onRemove = {
                            combatViewModel.deleteBossTemplate(boss.bossTemplateId)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BossSelectionPanel(
                        templates = bossTemplates,
                        onSpawn = { template ->
                            combatViewModel.spawnBoss(template.id, template.baseHp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { pauseViewModel.navigateToDashboard() },
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← BACK TO DASHBOARD", color = CyberCyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ActiveCombatPanel(
    boss: ActiveBoss,
    bossName: String,
    maxHp: Int,
    isAttacking: Boolean,
    onAttack: () -> Unit,
    onRemove: () -> Unit
) {
    val hpFraction = (boss.currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)

    val animatedHp by animateFloatAsState(
        targetValue = hpFraction,
        animationSpec = tween(400),
        label = "hp_bar"
    )

    val hpColor by animateColorAsState(
        targetValue = when {
            hpFraction > 0.5f -> Color(0xFF00FF88)
            hpFraction > 0.25f -> CyberYellow
            else -> CyberRed
        },
        animationSpec = tween(400),
        label = "hp_color"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "👾", fontSize = 48.sp)
                Text(
                    text = bossName.uppercase(),
                    color = CyberRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1A1A2A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedHp)
                    .fillMaxHeight()
                    .background(hpColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${boss.currentHp} / $maxHp", color = Color.Gray, fontSize = 10.sp)
            TextButton(onClick = onRemove) {
                Text("REMOVE", color = Color.Gray, fontSize = 10.sp)
            }
        }

        Button(
            onClick = onAttack,
            enabled = !isAttacking,
            colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isAttacking) "STRIKING..." else "STRIKE",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BossSelectionPanel(
    templates: List<BossTemplate>,
    onSpawn: (BossTemplate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPAWN NEW ENEMY",
            color = CyberYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        templates.forEach { template ->
            BossTemplateCard(
                template = template,
                onSpawn = onSpawn
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BossTemplateCard(
    template: BossTemplate,
    onSpawn: (BossTemplate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PanelBg)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = template.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "HP: ${template.baseHp}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = { onSpawn(template) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "FIGHT",
                color = DarkBg,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DefeatBanner(
    bossName: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .border(1.dp, CyberYellow, RoundedCornerShape(12.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "BOSS DEFEATED",
            color = CyberYellow,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$bossName has fallen.\nYour focus prevails.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("CONTINUE", color = DarkBg, fontWeight = FontWeight.Black)
        }
    }
}
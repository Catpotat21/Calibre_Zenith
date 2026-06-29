package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val activeBoss         by combatViewModel.activeBoss.collectAsState()
    val activeBossTemplate by combatViewModel.activeBossTemplate.collectAsState()
    val bossTemplates      by combatViewModel.bossTemplates.collectAsState()
    val isAttacking        by combatViewModel.isAttacking.collectAsState()
    val isBossDefeated     by combatViewModel.isBossDefeated.collectAsState()

    LaunchedEffect(Unit) {
        combatViewModel.seedDefaultBossesIfEmpty()
    }

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

            when {
                isBossDefeated -> {
                    DefeatBanner(onContinue = { combatViewModel.resetDefeatState() })
                }
                activeBoss != null -> {
                    val template = activeBossTemplate
                        ?: bossTemplates.find { it.id == activeBoss!!.bossTemplateId }
                    ActiveCombatPanel(
                        bossName    = template?.name ?: "UNKNOWN BOSS",
                        currentHp   = activeBoss!!.currentHp,
                        maxHp       = template?.baseHp ?: activeBoss!!.currentHp,
                        isAttacking = isAttacking,
                        onAttack    = { combatViewModel.attackBoss(10) }
                    )
                }
                else -> {
                    BossSelectionPanel(
                        templates = bossTemplates,
                        onSpawn   = { combatViewModel.spawnBoss(it.id, it.baseHp) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { pauseViewModel.navigateToDashboard() },
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(CyberCyan.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← BACK TO DASHBOARD", color = CyberCyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ActiveCombatPanel(
    bossName: String,
    currentHp: Int,
    maxHp: Int,
    isAttacking: Boolean,
    onAttack: () -> Unit
) {
    val hpFraction = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)

    val animatedHp by animateFloatAsState(
        targetValue  = hpFraction,
        animationSpec = tween(400),
        label        = "hp_bar"
    )
    val hpColor by animateColorAsState(
        targetValue = when {
            hpFraction > 0.5f  -> Color(0xFF00FF88)
            hpFraction > 0.25f -> CyberYellow
            else               -> CyberRed
        },
        animationSpec = tween(400),
        label = "hp_color"
    )
    val panelColor by animateColorAsState(
        targetValue   = if (isAttacking) Color(0xFF1A0010) else PanelBg,
        animationSpec = tween(150),
        label         = "panel_flash"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(panelColor)
                .border(
                    1.dp,
                    if (isAttacking) CyberRed else BorderColor,
                    RoundedCornerShape(12.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "👾", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text         = bossName.uppercase(),
                    color        = CyberRed,
                    fontSize     = 18.sp,
                    fontWeight   = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign    = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("HP", color = Color.Gray, fontSize = 12.sp)
                Text(
                    "$currentHp / $maxHp",
                    color      = hpColor,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1A1A2A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedHp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(hpColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick  = onAttack,
            enabled  = !isAttacking,
            colors   = ButtonDefaults.buttonColors(
                containerColor         = CyberRed,
                disabledContainerColor = CyberRed.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text          = if (isAttacking) "STRIKING..." else "⚔ ATTACK  (-10 HP)",
                color         = Color.White,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 2.sp
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
            text          = "SELECT YOUR ENEMY",
            color         = CyberYellow,
            fontSize      = 16.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(templates) { template ->
                BossTemplateCard(template = template, onSpawn = onSpawn)
            }
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
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = template.name.uppercase(),
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
            Text(
                text     = "HP: ${template.baseHp}",
                color    = Color.Gray,
                fontSize = 12.sp
            )
        }
        Button(
            onClick         = { onSpawn(template) },
            colors          = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape           = RoundedCornerShape(6.dp),
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("FIGHT", color = DarkBg, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DefeatBanner(onContinue: () -> Unit) {
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
            text          = "BOSS DEFEATED",
            color         = CyberYellow,
            fontSize      = 22.sp,
            fontWeight    = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = "The enemy has fallen.\nYour focus prevails.",
            color     = Color.Gray,
            fontSize  = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick  = onContinue,
            colors   = ButtonDefaults.buttonColors(containerColor = CyberYellow),
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp)
        ) {
            Text("FIGHT AGAIN", color = DarkBg, fontWeight = FontWeight.Black)
        }
    }
}
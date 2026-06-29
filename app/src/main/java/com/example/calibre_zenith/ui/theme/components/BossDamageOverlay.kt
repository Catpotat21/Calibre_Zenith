package com.example.calibre_zenith.ui.theme.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.ui.viewmodel.CombatViewModel
import com.example.calibre_zenith.ui.viewmodel.DamageEffect
import kotlinx.coroutines.delay

private val CyberRed = Color(0xFFFF003C)
private val CyberCyan = Color(0xFF00F5FF)
private val PanelBg = Color(0xFF12121A)

@Composable
fun BossDamageOverlay(viewModel: CombatViewModel) {
    var effect by remember { mutableStateOf<DamageEffect?>(null) }

    LaunchedEffect(Unit) {
        viewModel.damageEffect.collect {
            effect = it
            delay(3000)
            effect = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = effect != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
        ) {
            effect?.let { eff ->
                DamageCard(eff)
            }
        }
    }
}

@Composable
private fun DamageCard(effect: DamageEffect) {
    val hpFraction = (effect.currentHp.toFloat() / effect.maxHp.toFloat()).coerceIn(0f, 1f)
    val animatedHp by animateFloatAsState(
        targetValue = hpFraction,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "overlay_hp"
    )

    Column(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .border(1.dp, CyberRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CRITICAL HIT!",
            color = CyberRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(y = (-4).dp)
        )
        Text(
            text = effect.bossName.uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // HP Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedHp)
                    .fillMaxHeight()
                    .background(CyberRed)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${effect.currentHp}/${effect.maxHp} HP", color = Color.Gray, fontSize = 10.sp)
            Text("-${effect.damage}", color = CyberRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}
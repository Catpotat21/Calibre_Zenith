package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calibre_zenith.data.GlobalTags
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
fun BossCreationScreen(
    pauseViewModel: PauseViewModel,
    combatViewModel: CombatViewModel
) {
    val bossTemplates by combatViewModel.bossTemplates.collectAsState()
    val activeBosses  by combatViewModel.activeBosses.collectAsState()

    var showCreator by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BOSS WORKSHOP",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "CREATE & MANAGE ENEMIES",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row {
                    IconButton(onClick = { showCreator = true }) {
                        Icon(Icons.Default.Add, "Create Boss", tint = CyberCyan)
                    }
                    IconButton(onClick = { pauseViewModel.navigateToDashboard() }) {
                        Icon(Icons.Default.Home, "Dashboard", tint = CyberCyan)
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = CyberCyan.copy(alpha = 0.2f)
            )

            if (bossTemplates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👾", fontSize = 48.sp)
                        Text("NO BOSSES CREATED YET", color = Color.Gray, fontSize = 12.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bossTemplates) { template ->
                        val activeBoss = activeBosses.find { it.bossTemplateId == template.id }
                        BossTemplateRow(
                            template  = template,
                            isActive  = activeBoss != null,
                            currentHp = activeBoss?.currentHp,
                            onSpawn   = { combatViewModel.spawnBoss(template.id, template.baseHp) },
                            onDelete  = { combatViewModel.deleteBossTemplate(template.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { pauseViewModel.navigateToCombat() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⚔ ENTER COMBAT ARENA", color = DarkBg, fontWeight = FontWeight.Black)
            }
        }
    }

    if (showCreator) {
        BossCreatorDialog(
            onDismiss = { showCreator = false },
            onCreate  = { name, hp, url, tags ->
                combatViewModel.createBossTemplate(name, hp, url, tags)
                showCreator = false
            }
        )
    }
}

@Composable
private fun BossTemplateRow(
    template:  BossTemplate,
    isActive:  Boolean,
    currentHp: Int?,
    onSpawn:   () -> Unit,
    onDelete:  () -> Unit
) {
    val hpFraction = if (currentHp != null && template.baseHp > 0) currentHp.toFloat() / template.baseHp.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PanelBg)
            .border(1.dp, if (isActive) CyberRed.copy(alpha = 0.6f) else BorderColor, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A1A2A)), contentAlignment = Alignment.Center) {
                if (template.bossImageUrl.isNotBlank()) {
                    AsyncImage(model = template.bossImageUrl, contentDescription = template.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text("👾", fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name.uppercase(), color = if (isActive) CyberRed else Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("BASE HP: ${template.baseHp}", color = Color.Gray, fontSize = 11.sp)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.DarkGray) }
            if (!isActive) {
                IconButton(onClick = onSpawn) { Icon(Icons.Default.PlayArrow, null, tint = CyberYellow) }
            }
        }

        if (isActive && currentHp != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.Black)) {
                Box(modifier = Modifier.fillMaxWidth(hpFraction.coerceIn(0f, 1f)).fillMaxHeight().background(CyberRed))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BossCreatorDialog(
    onDismiss: () -> Unit,
    onCreate:  (name: String, hp: Int, imageUrl: String, tags: List<String>) -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var hpText   by remember { mutableStateOf("100") }
    var imageUrl by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }
    val tags     = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelBg,
        title = { Text("FORGE BOSS", color = CyberCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Boss Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hpText, onValueChange = { if (it.all { c -> c.isDigit() }) hpText = it }, label = { Text("Base HP") }, modifier = Modifier.fillMaxWidth())
                
                Text("ASSOCIATED TAGS", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    GlobalTags.predefined.forEach { tag ->
                        FilterChip(
                            selected = tags.contains(tag),
                            onClick = { if (tags.contains(tag)) tags.remove(tag) else tags.add(tag) },
                            label = { Text(tag, fontSize = 10.sp) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = tagInput, onValueChange = { tagInput = it }, label = { Text("Custom tag") }, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (tagInput.isNotBlank()) { tags.add(tagInput.trim()); tagInput = "" } }) {
                        Icon(Icons.Default.Add, null, tint = CyberCyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name, hpText.toIntOrNull() ?: 100, imageUrl, tags.toList()) }) {
                Text("FORGE")
            }
        }
    )
}
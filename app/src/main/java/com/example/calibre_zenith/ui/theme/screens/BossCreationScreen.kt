package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            // ── Header ─────────────────────────────────────────
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

            // ── Boss List ──────────────────────────────────────
            if (bossTemplates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO BOSSES CREATED YET",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tap + to forge your first enemy",
                            color = Color.DarkGray,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bossTemplates) { template ->
                        val isActive = activeBosses.any { it.bossTemplateId == template.id }
                        val activeBoss = activeBosses.find { it.bossTemplateId == template.id }
                        BossTemplateRow(
                            template  = template,
                            isActive  = isActive,
                            currentHp = activeBoss?.currentHp,
                            onSpawn   = { combatViewModel.spawnBoss(template.id, template.baseHp) },
                            onDelete  = { combatViewModel.deleteBossTemplate(template.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Go to Arena ────────────────────────────────────
            Button(
                onClick = { pauseViewModel.navigateToCombat() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "⚔ ENTER COMBAT ARENA",
                    color = DarkBg,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
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

// ── Boss Template Row ──────────────────────────────────────────
@Composable
private fun BossTemplateRow(
    template:  BossTemplate,
    isActive:  Boolean,
    currentHp: Int?,
    onSpawn:   () -> Unit,
    onDelete:  () -> Unit
) {
    val hpFraction = if (currentHp != null && template.baseHp > 0)
        currentHp.toFloat() / template.baseHp.toFloat()
    else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PanelBg)
            .border(
                1.dp,
                if (isActive) CyberRed.copy(alpha = 0.6f) else BorderColor,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Boss image or placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A2A))
                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (template.bossImageUrl.isNotBlank()) {
                    AsyncImage(
                        model              = template.bossImageUrl,
                        contentDescription = template.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Text("👾", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text          = template.name.uppercase(),
                    color         = if (isActive) CyberRed else Color.White,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 14.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text     = "BASE HP: ${template.baseHp}",
                    color    = Color.Gray,
                    fontSize = 11.sp
                )
                if (template.tagList().isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text       = "TAGS: ${template.tagList().joinToString(" · ")}",
                        color      = CyberCyan.copy(alpha = 0.7f),
                        fontSize   = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (!isActive) {
                    IconButton(onClick = onSpawn, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.PlayArrow,
                            "Spawn",
                            tint     = CyberYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint     = Color.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // HP bar when active
        if (isActive && currentHp != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ACTIVE",
                    color      = CyberRed,
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    "$currentHp / ${template.baseHp} HP",
                    color      = CyberRed,
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1A1A2A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpFraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberRed)
                )
            }
        }
    }
}

// ── Boss Creator Dialog ────────────────────────────────────────
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
        modifier         = Modifier.border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        title = {
            Text(
                "FORGE BOSS",
                color         = CyberCyan,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Black,
                letterSpacing = 3.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Boss Name", color = Color.Gray) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        cursorColor        = CyberCyan,
                        focusedLabelColor  = CyberCyan
                    )
                )

                // Base HP
                OutlinedTextField(
                    value         = hpText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) hpText = it },
                    label         = { Text("Base HP", color = Color.Gray) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        cursorColor        = CyberCyan,
                        focusedLabelColor  = CyberCyan
                    )
                )

                // Image URL
                OutlinedTextField(
                    value         = imageUrl,
                    onValueChange = { imageUrl = it },
                    label         = { Text("Image URL (optional)", color = Color.Gray) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        cursorColor        = CyberCyan,
                        focusedLabelColor  = CyberCyan
                    )
                )

                // Image preview
                if (imageUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A2A))
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model              = imageUrl,
                            contentDescription = "Boss preview",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    }
                }

                // Tags
                Text(
                    "ASSOCIATED TAGS",
                    color         = CyberCyan,
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily    = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value         = tagInput,
                        onValueChange = { tagInput = it },
                        label         = { Text("Add tag", color = Color.Gray) },
                        modifier      = Modifier.weight(1f),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            cursorColor        = CyberCyan
                        )
                    )
                    IconButton(onClick = {
                        val trimmed = tagInput.trim()
                        if (trimmed.isNotBlank() && !tags.contains(trimmed)) {
                            tags.add(trimmed)
                            tagInput = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, "Add tag", tint = CyberCyan)
                    }
                }

                // Tag chips
                if (tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan.copy(alpha = 0.1f))
                                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        tag.uppercase(),
                                        color      = CyberCyan,
                                        fontSize   = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint     = CyberCyan,
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clickable { tags.remove(tag) }   // ✅ Fixed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hp = hpText.toIntOrNull() ?: 100
                    if (name.isNotBlank()) {
                        onCreate(name.trim(), hp, imageUrl.trim(), tags.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape  = RoundedCornerShape(8.dp)
            ) {
                Text("FORGE", color = DarkBg, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}
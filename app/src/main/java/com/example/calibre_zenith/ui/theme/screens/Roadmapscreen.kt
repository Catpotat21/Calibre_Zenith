package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

// Luxury Styling
private val LuxBgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF0D0D11), Color(0xFF050507)))
private val LuxSurface = Color(0xFF141419)
private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxTextPrimary = Color(0xFFF0F0F5)
private val LuxTextMuted = Color(0xFFA2A2AB)

data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

@Composable
fun RoadmapScreen() {
    val milestones = remember {
        mutableStateListOf(
            Milestone(title = "Define core architecture", isCompleted = true),
            Milestone(title = "Implement Gemini API hook", isCompleted = true),
            Milestone(title = "Deploy tactile UI elements", isCompleted = false),
            Milestone(title = "Finalize execution phase", isCompleted = false)
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newMilestoneText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(LuxBgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = LuxAccentGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "TACTILE ROADMAP",
                    fontSize = 22.sp,
                    color = LuxAccentGold,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track your macro-level execution path.",
                fontSize = 13.sp,
                color = LuxTextMuted
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(milestones) { index, milestone ->
                    RoadmapNode(
                        milestone = milestone,
                        isLast = index == milestones.size - 1,
                        onToggle = {
                            milestones[index] = milestone.copy(isCompleted = !milestone.isCompleted)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
            containerColor = LuxAccentGold,
            contentColor = Color(0xFF050507)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Milestone")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = LuxSurface,
                titleContentColor = LuxTextPrimary,
                textContentColor = LuxTextMuted,
                title = { Text("NEW MILESTONE", fontFamily = FontFamily.Monospace, fontSize = 16.sp, letterSpacing = 2.sp) },
                text = {
                    OutlinedTextField(
                        value = newMilestoneText,
                        onValueChange = { newMilestoneText = it },
                        placeholder = { Text("e.g. Master Python APIs", color = LuxTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxAccentGold,
                            unfocusedBorderColor = LuxAccentGold.copy(alpha = 0.3f),
                            cursorColor = LuxAccentGold,
                            focusedTextColor = LuxTextPrimary,
                            unfocusedTextColor = LuxTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newMilestoneText.isNotBlank()) {
                            milestones.add(Milestone(title = newMilestoneText))
                            newMilestoneText = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("ADD TO PATH", color = LuxAccentGold, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("CANCEL", color = LuxTextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun RoadmapNode(milestone: Milestone, isLast: Boolean, onToggle: () -> Unit) {
    val nodeColor by animateColorAsState(if (milestone.isCompleted) LuxAccentGold else LuxSurface, label = "nodeColor")
    val textColor by animateColorAsState(if (milestone.isCompleted) LuxTextPrimary else LuxTextMuted, label = "textColor")
    val iconColor by animateColorAsState(if (milestone.isCompleted) Color(0xFF050507) else Color.Transparent, label = "iconColor")

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(nodeColor, CircleShape)
                    .border(2.dp, LuxAccentGold, CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }

            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(LuxAccentGold.copy(alpha = 0.3f)))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).clickable { onToggle() },
            colors = CardDefaults.cardColors(containerColor = LuxSurface),
            border = BorderStroke(1.dp, if (milestone.isCompleted) LuxAccentGold.copy(alpha = 0.5f) else Color.Transparent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (milestone.isCompleted) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = LuxAccentGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = milestone.title,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = if (milestone.isCompleted) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
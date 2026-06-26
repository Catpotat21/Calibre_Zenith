package com.example.calibre_zenith.ui.theme.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.data.RoadmapPersistence
import com.example.calibre_zenith.data.TaskNode
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.UUID

// =================================================================
// 🎨 LUXURY PALETTE & DYNAMIC HEIGHTS
// =================================================================
private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxDarkBg = Color(0xFF050507)
private val LuxSurface = Color(0xFF141419)

// =================================================================
// 🖥️ MAIN ROADMAP SCREEN WITH BREADCRUMB TRANSITIONS
// =================================================================
fun getFlairColor(flair: String): Color {
    val hash = flair.hashCode().coerceAtLeast(0)
    val colors = listOf(
        Color(0xFFD4AF37), // Champagne Gold
        Color(0xFFD94A4A), // Deep Crimson
        Color(0xFF4A90E2), // Cyber Blue
        Color(0xFF50E3C2), // Emerald Jade
        Color(0xFF9013FE)  // Royal Amethyst
    )
    return colors[hash % colors.size]
}

// =================================================================
// 🖥️ MAIN ROADMAP SCREEN WITH BREADCRUMB TRANSITIONS
// =================================================================
@Composable
fun RoadmapScreen(viewModel: PauseViewModel) {
    val context = LocalContext.current
    val rootNodes = viewModel.roadmapNodes
    var navigationStack by remember { mutableStateOf(listOf<TaskNode>()) }

    // Load persisted data on initialization
    LaunchedEffect(Unit) {
        viewModel.initializeRoadmap(context)
    }

    val currentView = navigationStack.lastOrNull()

    AnimatedContent(
        targetState = currentView,
        transitionSpec = {
            if (targetState != null) slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            else slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        },
        label = "PageTransition"
    ) { view ->
        if (view == null) {
            MainDashboard(
                viewModel = viewModel,
                nodes = rootNodes,
                onAdd = { rootNodes.add(TaskNode(initialTitle = it)); viewModel.triggerRoadmapSave(context) },
                onDelete = { rootNodes.remove(it); viewModel.triggerRoadmapSave(context) },
                onNavigate = { navigationStack = navigationStack + it }
            )
        } else {
            TaskDetailPage(
                node = view,
                onBack = { navigationStack = navigationStack.dropLast(1) },
                onLaunchTimer = {
                    viewModel.loadCognitiveSession(view.title, "Executing: ${view.title}", "Level: Focus", true)
                    viewModel.navigateToTimerScreen()
                },
                onNavigate = { childNode -> navigationStack = navigationStack + childNode },
                onDelete = { childNode -> view.children.remove(childNode); viewModel.triggerRoadmapSave(context) },
                onSaveTrigger = { viewModel.triggerRoadmapSave(context) }
            )
        }
    }
}

@Composable
fun MainDashboard(
    viewModel: PauseViewModel,
    nodes: List<TaskNode>,
    onAdd: (String) -> Unit,
    onDelete: (TaskNode) -> Unit,
    onNavigate: (TaskNode) -> Unit
) {
    var newTitle by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(LuxDarkBg).padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TACTICAL ROADMAP", color = LuxAccentGold, fontSize = 18.sp, letterSpacing = 4.sp, fontFamily = FontFamily.Monospace)
            IconButton(onClick = { viewModel.navigateToDashboard() }) {
                Icon(Icons.Default.Home, contentDescription = "Return to Dashboard", tint = LuxAccentGold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                modifier = Modifier.weight(1f),
                label = { Text("New Main Task", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxAccentGold,
                    focusedLabelColor = LuxAccentGold,
                    cursorColor = LuxAccentGold
                )
            )
            IconButton(onClick = { if (newTitle.isNotBlank()) { onAdd(newTitle); newTitle = "" } }) {
                Icon(Icons.Default.Add, null, tint = LuxAccentGold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 16.dp)) {
            items(nodes) { node ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(node) },
                    colors = CardDefaults.cardColors(containerColor = LuxSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(node.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { onDelete(node) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.DarkGray)
                            }
                        }

                        // Render Horizontal Flairs
                        if (node.flairs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(node.flairs) { flair ->
                                    Box(
                                        modifier = Modifier
                                            .background(getFlairColor(flair).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(1.dp, getFlairColor(flair).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(flair.uppercase(), color = getFlairColor(flair), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Render Schedule Badge
                        if (!node.scheduledDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = LuxAccentGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${node.scheduledDate} | ${node.scheduledTime ?: ""}${if (!node.scheduledEndTime.isNullOrBlank()) " - ${node.scheduledEndTime}" else ""}",
                                    color = LuxAccentGold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailPage(
    node: TaskNode,
    onBack: () -> Unit,
    onLaunchTimer: () -> Unit,
    onNavigate: (TaskNode) -> Unit,
    onDelete: (TaskNode) -> Unit,
    onSaveTrigger: () -> Unit
) {
    val context = LocalContext.current
    var details by remember { mutableStateOf(node.details) }
    var newSubTask by remember { mutableStateOf("") }

    // Scheduling Fields
    var scheduledDate by remember(node.scheduledDate) { mutableStateOf(node.scheduledDate ?: "") }
    var scheduledTime by remember(node.scheduledTime) { mutableStateOf(node.scheduledTime ?: "") }
    var scheduledEndTime by remember(node.scheduledEndTime) { mutableStateOf(node.scheduledEndTime ?: "") }

    // Flair Add Fields
    var customFlair by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val date = "$year-${String.format(java.util.Locale.US, "%02d", month + 1)}-${String.format(java.util.Locale.US, "%02d", dayOfMonth)}"
            scheduledDate = date
            node.scheduledDate = date
            onSaveTrigger()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun showTimePicker(isStartTime: Boolean) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val time = String.format(java.util.Locale.US, "%02d:%02d", hour, minute)
                if (isStartTime) {
                    scheduledTime = time
                    node.scheduledTime = time
                } else {
                    scheduledEndTime = time
                    node.scheduledEndTime = time
                }
                onSaveTrigger()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(LuxDarkBg).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Spacer(modifier = Modifier.width(8.dp))
            Text(node.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spacious Writing Canvas for context & details
        OutlinedTextField(
            value = details,
            onValueChange = { details = it; node.details = it; onSaveTrigger() },
            label = { Text("Task Description / Context", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LuxAccentGold,
                focusedLabelColor = LuxAccentGold,
                cursorColor = LuxAccentGold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- CALIBRE PLANNER SCHEDULER PROTOCOL ---
        Text("PLANNER INTEGRATION & SCHEDULING", color = LuxAccentGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1.2f).clickable { datePickerDialog.show() }) {
                OutlinedTextField(
                    value = scheduledDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Date", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = LuxAccentGold,
                        disabledLabelColor = LuxAccentGold,
                        disabledTextColor = Color.White
                    ),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = LuxAccentGold, modifier = Modifier.size(16.dp)) }
                )
            }
            Box(modifier = Modifier.weight(1f).clickable { showTimePicker(true) }) {
                OutlinedTextField(
                    value = scheduledTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Start", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = LuxAccentGold,
                        disabledLabelColor = LuxAccentGold,
                        disabledTextColor = Color.White
                    ),
                    trailingIcon = { Icon(Icons.Default.Schedule, null, tint = LuxAccentGold, modifier = Modifier.size(16.dp)) }
                )
            }
            Box(modifier = Modifier.weight(1f).clickable { showTimePicker(false) }) {
                OutlinedTextField(
                    value = scheduledEndTime,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("End", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = LuxAccentGold,
                        disabledLabelColor = LuxAccentGold,
                        disabledTextColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MANAGING FLAIRS & CAPSULES ---
        Text("MANAGE CUSTOM FLAIRS", color = LuxAccentGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customFlair,
                onValueChange = { customFlair = it },
                modifier = Modifier.weight(1f),
                label = { Text("Add custom label", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold, cursorColor = LuxAccentGold)
            )
            IconButton(onClick = {
                if (customFlair.isNotBlank() && !node.flairs.contains(customFlair.trim())) {
                    node.flairs.add(customFlair.trim())
                    customFlair = ""
                    onSaveTrigger()
                }
            }) {
                Icon(Icons.Default.Done, "Add Flair", tint = LuxAccentGold)
            }
        }

        if (node.flairs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(node.flairs) { flair ->
                    Box(
                        modifier = Modifier
                            .background(getFlairColor(flair).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, getFlairColor(flair), RoundedCornerShape(6.dp))
                            .clickable {
                                node.flairs.remove(flair)
                                onSaveTrigger()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(flair.uppercase(), color = getFlairColor(flair), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Close, null, tint = getFlairColor(flair), modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Launch Path to Cognitive Workspaces
        Button(
            onClick = onLaunchTimer,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold)
        ) {
            Text("ENGAGE COGNITION TIMER", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSubTask,
                onValueChange = { newSubTask = it },
                modifier = Modifier.weight(1f),
                label = { Text("Add Milestone / Sub-Task", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxAccentGold,
                    focusedLabelColor = LuxAccentGold,
                    cursorColor = LuxAccentGold
                )
            )
            IconButton(onClick = {
                if (newSubTask.isNotBlank()) {
                    node.children.add(TaskNode(initialTitle = newSubTask))
                    newSubTask = ""
                    onSaveTrigger()
                }
            }) {
                Icon(Icons.Default.Add, null, tint = LuxAccentGold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Infinite Nesting Enablement
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(node.children) { child ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(child) },
                    colors = CardDefaults.cardColors(containerColor = LuxSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = child.isCompleted,
                                onCheckedChange = {
                                    child.isCompleted = it
                                    onSaveTrigger()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = LuxAccentGold, checkmarkColor = Color.Black)
                            )
                            Column {
                                Text(
                                    text = child.title,
                                    color = if (child.isCompleted) Color.Gray else Color.White,
                                    fontWeight = if (child.isCompleted) FontWeight.Normal else FontWeight.Medium
                                )
                                if (child.flairs.isNotEmpty() || !child.scheduledDate.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${child.flairs.size} tags • ${if (child.scheduledDate != null) "Scheduled" else "No schedule"}",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { onDelete(child) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}
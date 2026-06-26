package com.example.calibre_zenith.ui.theme.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.notification.TaskAlarmScheduler
import com.example.calibre_zenith.data.RoadmapPersistence
import com.example.calibre_zenith.data.TaskNode
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

// =================================================================
// 🎨 LUXURY PALETTE (REDEFINED FOR COHESION)
// =================================================================
private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxDarkBg = Color(0xFF050507)
private val LuxSurface = Color(0xFF141419)

// Helper to convert TaskNode into DynamicPlannerTask for the legacy Alarm Scheduler
fun TaskNode.toDynamicTask(): DynamicPlannerTask? {
    val date = scheduledDate ?: return null
    val time = scheduledTime ?: return null
    val endTime = scheduledEndTime ?: ""

    return try {
        val dateParts = date.split("-")
        val timeParts = time.split(":")
        val endTimeParts = endTime.split(":")

        val cal = Calendar.getInstance().apply {
            set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
        }

        val sH = timeParts[0].toInt()
        val sM = timeParts[1].toInt()
        val eH = if (endTimeParts.size >= 2) endTimeParts[0].toInt() else (sH + 1).coerceIn(0, 23)
        val eM = if (endTimeParts.size >= 2) endTimeParts[1].toInt() else sM

        DynamicPlannerTask(
            id = id,
            title = title,
            microStep = if (details.isNotBlank()) details else "Commence Execution",
            frictionNotes = "Synced from Roadmap",
            dayOfYear = cal.get(Calendar.DAY_OF_YEAR),
            year = cal.get(Calendar.YEAR),
            startHour = sH,
            startMinute = sM,
            endHour = eH,
            endMinute = eM,
            isHighFriction = false
        )
    } catch (e: Exception) {
        null
    }
}

// Legacy Data Class to keep the Alarm Scheduler compatible without breaking dependencies
data class DynamicPlannerTask(
    val id: String,
    val title: String,
    val microStep: String,
    val frictionNotes: String,
    val dayOfYear: Int,
    val year: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isHighFriction: Boolean = false
)

@Composable
fun PlannerScreen(viewModel: PauseViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val triggerHaptic = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    val roadmapNodes = viewModel.roadmapNodes
    var navigationStack by remember { mutableStateOf(listOf<TaskNode>()) }

    // Calendar & Timeline Scopes
    val baseCalendar = remember { Calendar.getInstance() }
    var selectedDayOfYear by remember { mutableIntStateOf(baseCalendar.get(Calendar.DAY_OF_YEAR)) }
    var selectedYear by remember { mutableIntStateOf(baseCalendar.get(Calendar.YEAR)) }

    // Load persisted Roadmap data into shared ViewModel state if not already done
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
        label = "PlannerTransition"
    ) { viewNode ->
        if (viewNode == null) {
            TimelineDashboard(
                viewModel = viewModel,
                nodes = roadmapNodes,
                selectedDayOfYear = selectedDayOfYear,
                selectedYear = selectedYear,
                onDaySelected = { d, y -> selectedDayOfYear = d; selectedYear = y },
                onNavigate = { navigationStack = navigationStack + it },
                onDelete = { node -> roadmapNodes.remove(node); viewModel.triggerRoadmapSave(context) },
                onAdd = { title, date, start, end ->
                    val newNode = TaskNode(
                        initialTitle = title,
                        initialScheduledDate = date,
                        initialScheduledTime = start,
                        initialScheduledEndTime = end
                    )
                    roadmapNodes.add(newNode)
                    viewModel.triggerRoadmapSave(context)
                    // Also schedule alarm
                    newNode.toDynamicTask()?.let { TaskAlarmScheduler(context).scheduleTaskAlerts(it) }
                }
            )
        } else {
            TaskDetailPage(
                node = viewNode,
                onBack = { navigationStack = navigationStack.dropLast(1) },
                onLaunchTimer = {
                    viewModel.loadCognitiveSession(viewNode.title, "Executing: ${viewNode.title}", "Level: Focus", true)
                    viewModel.navigateToTimerScreen()
                },
                onNavigate = { childNode -> navigationStack = navigationStack + childNode },
                onDelete = { childNode -> viewNode.children.remove(childNode); viewModel.triggerRoadmapSave(context) },
                onSaveTrigger = { viewModel.triggerRoadmapSave(context) }
            )
        }
    }
}

@Composable
fun TimelineDashboard(
    viewModel: PauseViewModel,
    nodes: List<TaskNode>,
    selectedDayOfYear: Int,
    selectedYear: Int,
    onDaySelected: (Int, Int) -> Unit,
    onNavigate: (TaskNode) -> Unit,
    onDelete: (TaskNode) -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var showCreator by remember { mutableStateOf(false) }

    // Flat list of scheduled nodes for the timeline (Observe SnapshotStateList changes)
    val allScheduled by remember {
        derivedStateOf {
            val list = mutableListOf<TaskNode>()
            fun extract(n: TaskNode) {
                if (!n.scheduledDate.isNullOrBlank()) list.add(n)
                n.children.forEach { extract(it) }
            }
            nodes.forEach { extract(it) }
            list
        }
    }

    val activeDayNodes by remember(selectedDayOfYear, selectedYear) {
        derivedStateOf {
            allScheduled.filter { node ->
                val dateParts = node.scheduledDate?.split("-") ?: return@filter false
                if (dateParts.size != 3) return@filter false
                val cal = Calendar.getInstance().apply {
                    set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
                }
                cal.get(Calendar.DAY_OF_YEAR) == selectedDayOfYear && cal.get(Calendar.YEAR) == selectedYear
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(LuxDarkBg)) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MISSION RUNWAY", color = LuxAccentGold, fontSize = 20.sp, letterSpacing = 4.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("SPATIAL TIME CANVAS", color = Color.Gray, fontSize = 9.sp, letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
            }
            Row {
                IconButton(onClick = { showCreator = true }) {
                    Icon(Icons.Default.Add, "Add Task", tint = LuxAccentGold)
                }
                IconButton(onClick = { viewModel.navigateToDashboard() }) {
                    Icon(Icons.Default.Home, "Dashboard", tint = LuxAccentGold)
                }
            }
        }

        // --- 7-DAY ROW ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (0..6).forEach { offset ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, offset)
                val isSelected = cal.get(Calendar.DAY_OF_YEAR) == selectedDayOfYear && cal.get(Calendar.YEAR) == selectedYear

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) LuxAccentGold else LuxSurface, RoundedCornerShape(12.dp))
                        .clickable { onDaySelected(cal.get(Calendar.DAY_OF_YEAR), cal.get(Calendar.YEAR)) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US).uppercase(),
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- TIMELINE ---
        Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Column {
                for (hour in 0..23) {
                    Row(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                        Text(
                            text = String.format(Locale.US, "%02d:00", hour),
                            color = Color.DarkGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(50.dp).padding(top = 4.dp)
                        )
                        Box(modifier = Modifier.fillMaxSize().border(0.5.dp, Color.Gray.copy(alpha = 0.1f)))
                    }
                }
            }

            activeDayNodes.forEach { node ->
                val startParts = node.scheduledTime?.split(":") ?: return@forEach
                val endParts = node.scheduledEndTime?.split(":")
                val sH = startParts[0].toFloat()
                val sM = startParts[1].toFloat()
                val eH = if (endParts != null && endParts.size >= 2) endParts[0].toFloat() else sH + 1
                val eM = if (endParts != null && endParts.size >= 2) endParts[1].toFloat() else sM

                val startOffset = (sH + sM / 60f) * 90
                val height = ((eH + eM / 60f) - (sH + sM / 60f)) * 90

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 50.dp)
                        .offset(y = startOffset.dp)
                        .height(height.dp)
                        .padding(2.dp)
                        .background(LuxAccentGold.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, LuxAccentGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .border(width = 4.dp, color = LuxAccentGold, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .clickable { onNavigate(node) }
                        .padding(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(node.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                text = "${node.scheduledTime} - ${node.scheduledEndTime ?: ""}",
                                color = LuxAccentGold,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { onDelete(node) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCreator) {
        TaskCreationDialog(
            onDismiss = { showCreator = false },
            onConfirm = { t, d, s, e ->
                onAdd(t, d, s, e)
                showCreator = false
            }
        )
    }
}

@Composable
fun TaskCreationDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LuxSurface,
        modifier = Modifier.border(1.dp, LuxAccentGold.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        title = { Text("ARCHITECT OBJECTIVE", color = LuxAccentGold, fontFamily = FontFamily.Monospace, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Objective Name", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold, cursorColor = LuxAccentGold)
                )

                // Date Picker Trigger
                Box(modifier = Modifier.fillMaxWidth().clickable {
                    DatePickerDialog(context, { _, y, m, d ->
                        date = "$y-${String.format(Locale.US, "%02d", m + 1)}-${String.format(Locale.US, "%02d", d)}"
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Date", color = Color.Gray) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = LuxAccentGold, disabledTextColor = Color.White)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f).clickable {
                        TimePickerDialog(context, { _, h, m ->
                            start = String.format(Locale.US, "%02d:%02d", h, m)
                        }, 9, 0, true).show()
                    }) {
                        OutlinedTextField(
                            value = start,
                            onValueChange = {},
                            label = { Text("Start", color = Color.Gray) },
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = LuxAccentGold, disabledTextColor = Color.White)
                        )
                    }
                    Box(modifier = Modifier.weight(1f).clickable {
                        TimePickerDialog(context, { _, h, m ->
                            end = String.format(Locale.US, "%02d:%02d", h, m)
                        }, 10, 0, true).show()
                    }) {
                        OutlinedTextField(
                            value = end,
                            onValueChange = {},
                            label = { Text("End", color = Color.Gray) },
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = LuxAccentGold, disabledTextColor = Color.White)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && date.isNotBlank() && start.isNotBlank()) onConfirm(title, date, start, end) },
                colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold)
            ) {
                Text("ENGAGE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

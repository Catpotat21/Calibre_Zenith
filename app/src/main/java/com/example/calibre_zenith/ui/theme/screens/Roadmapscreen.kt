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
import com.example.calibre_zenith.data.GlobalTags
import com.example.calibre_zenith.data.TaskNode
import com.example.calibre_zenith.ui.viewmodel.CombatViewModel
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import java.util.Calendar

private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxDarkBg = Color(0xFF050507)
private val LuxSurface = Color(0xFF141419)

fun getFlairColor(flair: String): Color {
    val hash = flair.hashCode().coerceAtLeast(0)
    val colors = listOf(
        Color(0xFFD4AF37),
        Color(0xFFD94A4A),
        Color(0xFF4A90E2),
        Color(0xFF50E3C2),
        Color(0xFF9013FE)
    )
    return colors[hash % colors.size]
}

@Composable
fun RoadmapScreen(viewModel: PauseViewModel, combatViewModel: CombatViewModel) {
    val context = LocalContext.current
    val rootNodes = viewModel.roadmapNodes
    var navigationStack by remember { mutableStateOf(listOf<TaskNode>()) }

    fun getEffectiveTags(node: TaskNode, stack: List<TaskNode>): List<String> {
        val parentTags = stack.flatMap { it.flairs }.distinct()
        val ownTags = node.flairs.toList()
        val combined = (parentTags + ownTags).distinct()
        
        android.util.Log.d("RoadmapScreen", "Effective Tags for '${node.title}': Parent=$parentTags, Own=$ownTags, Combined=$combined")
        return combined
    }

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
                combatViewModel = combatViewModel,
                nodes = rootNodes,
                onAdd = { rootNodes.add(TaskNode(initialTitle = it)); viewModel.triggerRoadmapSave(context) },
                onDelete = { rootNodes.remove(it); viewModel.triggerRoadmapSave(context) },
                onNavigate = { navigationStack = navigationStack + it },
                getEffectiveTags = { node -> getEffectiveTags(node, emptyList()) }
            )
        } else {
            TaskDetailPage(
                node = view,
                combatViewModel = combatViewModel,
                onBack = { navigationStack = navigationStack.dropLast(1) },
                onLaunchTimer = {
                    viewModel.loadCognitiveSession(view.title, "Executing: ${view.title}", "Level: Focus", true)
                    viewModel.navigateToTimerScreen()
                },
                onNavigate = { childNode -> navigationStack = navigationStack + childNode },
                onDelete = { childNode -> view.children.remove(childNode); viewModel.triggerRoadmapSave(context) },
                onSaveTrigger = { viewModel.triggerRoadmapSave(context) },
                getEffectiveTags = { child -> getEffectiveTags(child, navigationStack + view) }
            )
        }
    }
}

@Composable
fun MainDashboard(
    viewModel: PauseViewModel,
    combatViewModel: CombatViewModel,
    nodes: List<TaskNode>,
    onAdd: (String) -> Unit,
    onDelete: (TaskNode) -> Unit,
    onNavigate: (TaskNode) -> Unit,
    getEffectiveTags: (TaskNode) -> List<String>
) {
    var newTaskTitle by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(LuxDarkBg).padding(24.dp)) {
        Text("TACTICAL ROADMAP", color = LuxAccentGold, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                modifier = Modifier.weight(1f),
                label = { Text("New Objective", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold, cursorColor = LuxAccentGold)
            )
            IconButton(onClick = { if (newTaskTitle.isNotBlank()) { onAdd(newTaskTitle); newTaskTitle = "" } }) {
                Icon(Icons.Default.Add, null, tint = LuxAccentGold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(nodes) { node ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(node) },
                    colors = CardDefaults.cardColors(containerColor = LuxSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxAccentGold.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = node.isCompleted,
                            onCheckedChange = { 
                                node.isCompleted = it
                                if (it) {
                                    val tags = getEffectiveTags(node)
                                    combatViewModel.onTaskCompleted(tags, node.hpDrain)
                                }
                                viewModel.triggerRoadmapSave(context)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = LuxAccentGold)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(node.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            
                            val inheritedTags = getEffectiveTags(node)
                            val timeRange = if (!node.scheduledTime.isNullOrBlank()) {
                                " [${node.scheduledTime}${if (!node.scheduledEndTime.isNullOrBlank()) " - ${node.scheduledEndTime}" else ""}]"
                            } else ""
                            
                            val subStepText = if (node.children.isNotEmpty()) {
                                "${node.children.count { it.isCompleted }}/${node.children.size} Steps"
                            } else ""

                            if (subStepText.isNotEmpty() || inheritedTags.isNotEmpty() || timeRange.isNotEmpty()) {
                                val secondLine = listOfNotNull(
                                    subStepText.takeIf { it.isNotEmpty() },
                                    inheritedTags.joinToString(", ").takeIf { it.isNotEmpty() }
                                ).joinToString(" • ")
                                
                                Text("$secondLine$timeRange", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = { onDelete(node) }) { Icon(Icons.Default.Delete, null, tint = Color.DarkGray) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.navigateToDashboard() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LuxSurface)
        ) {
            Text("RETURN TO COMMAND", color = LuxAccentGold)
        }
    }
}

@Composable
fun TaskDetailPage(
    node: TaskNode,
    combatViewModel: CombatViewModel,
    onBack: () -> Unit,
    onLaunchTimer: () -> Unit,
    onNavigate: (TaskNode) -> Unit,
    onDelete: (TaskNode) -> Unit,
    onSaveTrigger: () -> Unit,
    getEffectiveTags: (TaskNode) -> List<String>
) {
    val context = LocalContext.current
    var details by remember { mutableStateOf(node.details) }
    var newSubTask by remember { mutableStateOf("") }
    var hpDrainText by remember { mutableStateOf(node.hpDrain.toString()) }
    var scheduledDate by remember(node.scheduledDate) { mutableStateOf(node.scheduledDate ?: "") }
    var scheduledTime by remember(node.scheduledTime) { mutableStateOf(node.scheduledTime ?: "") }
    var scheduledEndTime by remember(node.scheduledEndTime) { mutableStateOf(node.scheduledEndTime ?: "") }
    var customFlair by remember { mutableStateOf("") }
    var showTagPicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        val date = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
        scheduledDate = date
        node.scheduledDate = date
        onSaveTrigger()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    fun showTimePicker(isStart: Boolean) {
        TimePickerDialog(context, { _, h, m ->
            val time = String.format("%02d:%02d", h, m)
            if (isStart) { node.scheduledTime = time; scheduledTime = time }
            else { node.scheduledEndTime = time; scheduledEndTime = time }
            onSaveTrigger()
        }, 12, 0, true).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(LuxDarkBg).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text(node.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = details,
            onValueChange = { details = it; node.details = it; onSaveTrigger() },
            label = { Text("Objective Details", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold, cursorColor = LuxAccentGold)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("HP IMPACT", color = LuxAccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Slider(
                value = node.hpDrain.toFloat(),
                onValueChange = { 
                    node.hpDrain = it.toInt()
                    hpDrainText = it.toInt().toString()
                    onSaveTrigger()
                },
                valueRange = 0f..100f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = LuxAccentGold, activeTrackColor = LuxAccentGold)
            )
            OutlinedTextField(
                value = hpDrainText,
                onValueChange = { 
                    hpDrainText = it
                    it.toIntOrNull()?.let { hp -> 
                        node.hpDrain = hp
                        onSaveTrigger()
                    }
                },
                modifier = Modifier.width(60.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            Text("HP", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("SCHEDULING", color = LuxAccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = scheduledDate, onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f).clickable { datePickerDialog.show() }, enabled = false, label = { Text("Date") })
            OutlinedTextField(value = scheduledTime, onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f).clickable { showTimePicker(true) }, enabled = false, label = { Text("Start") })
            OutlinedTextField(value = scheduledEndTime, onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f).clickable { showTimePicker(false) }, enabled = false, label = { Text("End") })
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("TAGS", color = LuxAccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customFlair,
                onValueChange = { customFlair = it },
                modifier = Modifier.weight(1f),
                label = { Text("Add Tag") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold)
            )
            IconButton(onClick = { if (customFlair.isNotBlank()) { node.flairs.add(customFlair.trim()); customFlair = ""; onSaveTrigger() } }) {
                Icon(Icons.Default.Done, null, tint = LuxAccentGold)
            }
            IconButton(onClick = { showTagPicker = true }) {
                Icon(Icons.Default.Label, null, tint = LuxAccentGold)
            }
        }

        if (node.flairs.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                items(node.flairs) { flair ->
                    AssistChip(
                        onClick = { node.flairs.remove(flair); onSaveTrigger() },
                        label = { Text(flair.uppercase(), fontSize = 10.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = newSubTask, onValueChange = { newSubTask = it }, modifier = Modifier.weight(1f), label = { Text("New Sub-Step") })
            IconButton(onClick = { if (newSubTask.isNotBlank()) { node.children.add(TaskNode(initialTitle = newSubTask)); newSubTask = ""; onSaveTrigger() } }) {
                Icon(Icons.Default.Add, null, tint = LuxAccentGold)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(node.children) { child ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigate(child) }, colors = CardDefaults.cardColors(containerColor = LuxSurface)) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = child.isCompleted,
                            onCheckedChange = { 
                                child.isCompleted = it
                                if (it) {
                                    val inheritedTags = getEffectiveTags(child)
                                    android.util.Log.d("RoadmapScreen", "Subtask completed. Inherited tags: $inheritedTags")
                                    combatViewModel.onTaskCompleted(inheritedTags, child.hpDrain)
                                }
                                onSaveTrigger()
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(child.title, color = if (child.isCompleted) Color.Gray else Color.White)
                            
                            val inheritedTags = getEffectiveTags(child)
                            val timeRange = if (!child.scheduledTime.isNullOrBlank()) {
                                " [${child.scheduledTime}${if (!child.scheduledEndTime.isNullOrBlank()) " - ${child.scheduledEndTime}" else ""}]"
                            } else ""

                            if (inheritedTags.isNotEmpty() || timeRange.isNotEmpty()) {
                                Text(
                                    text = "${inheritedTags.joinToString(", ")}$timeRange",
                                    color = LuxAccentGold.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        IconButton(onClick = { node.children.remove(child); onSaveTrigger() }) { Icon(Icons.Default.Delete, null, tint = Color.DarkGray) }
                    }
                }
            }
        }
    }

    if (showTagPicker) {
        AlertDialog(
            onDismissRequest = { showTagPicker = false },
            title = { Text("Select Tags") },
            text = {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    GlobalTags.predefined.forEach { tag ->
                        FilterChip(
                            selected = node.flairs.contains(tag),
                            onClick = {
                                if (node.flairs.contains(tag)) node.flairs.remove(tag)
                                else node.flairs.add(tag)
                                onSaveTrigger()
                            },
                            label = { Text(tag) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTagPicker = false }) { Text("OK") } }
        )
    }
}
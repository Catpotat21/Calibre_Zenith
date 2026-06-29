package com.example.calibre_zenith.ui.theme.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.data.DynamicPlannerTask
import com.example.calibre_zenith.data.TaskNode
import com.example.calibre_zenith.data.toDynamicTask
import com.example.calibre_zenith.ui.viewmodel.CombatViewModel
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import java.util.*

private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxDarkBg = Color(0xFF050507)
private val LuxSurface = Color(0xFF141419)

@Composable
fun PlannerScreen(viewModel: PauseViewModel, combatViewModel: CombatViewModel) {
    val context = LocalContext.current
    val nodes = viewModel.roadmapNodes
    var selectedDayOfYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    LaunchedEffect(Unit) {
        viewModel.initializeRoadmap(context)
    }

    TimelineDashboard(
        viewModel = viewModel,
        combatViewModel = combatViewModel,
        nodes = nodes,
        selectedDayOfYear = selectedDayOfYear,
        selectedYear = selectedYear,
        onDaySelected = { d, y -> selectedDayOfYear = d; selectedYear = y },
        onDelete = { node ->
            fun removeFromList(list: MutableList<TaskNode>, target: TaskNode): Boolean {
                if (list.remove(target)) return true
                for (item in list) {
                    if (removeFromList(item.children, target)) return true
                }
                return false
            }
            removeFromList(nodes, node)
            viewModel.triggerRoadmapSave(context)
        },
        onNavigate = { /* navigate to details if needed */ },
        onAdd = { title, date, start, end ->
            val newNode = TaskNode(
                initialTitle = title,
                initialScheduledDate = date,
                initialScheduledTime = start,
                initialScheduledEndTime = end
            )
            nodes.add(newNode)
            viewModel.triggerRoadmapSave(context)
        },
        context = context
    )
}

@Composable
fun TimelineDashboard(
    viewModel: PauseViewModel,
    combatViewModel: CombatViewModel,
    nodes: List<TaskNode>,
    selectedDayOfYear: Int,
    selectedYear: Int,
    onDaySelected: (Int, Int) -> Unit,
    onDelete: (TaskNode) -> Unit,
    onNavigate: (TaskNode) -> Unit,
    onAdd: (String, String, String, String) -> Unit,
    context: Context
) {
    var showCreator by remember { mutableStateOf(false) }

    val activeDayNodes = remember(nodes, selectedDayOfYear, selectedYear) {
        val list = mutableListOf<TaskNode>()
        fun extract(node: TaskNode) {
            if (!node.scheduledDate.isNullOrBlank()) {
                val parts = node.scheduledDate!!.split("-")
                if (parts.size == 3) {
                    val cal = Calendar.getInstance()
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    if (cal.get(Calendar.DAY_OF_YEAR) == selectedDayOfYear && cal.get(Calendar.YEAR) == selectedYear) {
                        list.add(node)
                    }
                }
            }
            node.children.forEach { extract(it) }
        }
        nodes.forEach { extract(it) }
        list
    }

    Column(modifier = Modifier.fillMaxSize().background(LuxDarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("STRATEGIC PLANNER", color = LuxAccentGold, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("TEMPORAL SCHEDULING UNIT", color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
            }
            IconButton(onClick = { showCreator = true }) {
                Icon(Icons.Default.Add, null, tint = LuxAccentGold)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (offset in -3..3) {
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
                            fontWeight = FontWeight.Bold
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

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Column {
                for (hour in 0..23) {
                    Row(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                        Text(
                            text = String.format("%02d:00", hour),
                            color = Color.DarkGray,
                            fontSize = 11.sp,
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 50.dp)
                        .offset(y = startOffset.dp)
                        .height(height.dp)
                        .padding(2.dp)
                        .clickable { onNavigate(node) },
                    colors = CardDefaults.cardColors(containerColor = LuxAccentGold.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxAccentGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = node.isCompleted,
                            onCheckedChange = { 
                                node.isCompleted = it
                                if (it) combatViewModel.onTaskCompleted(node.flairs.toList(), node.hpDrain)
                                viewModel.triggerRoadmapSave(context)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = LuxAccentGold)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(node.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${node.scheduledTime} - ${node.scheduledEndTime ?: ""}", color = LuxAccentGold, fontSize = 9.sp)
                        }
                        IconButton(onClick = { onDelete(node) }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = { viewModel.navigateToDashboard() },
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LuxSurface)
        ) {
            Text("BACK TO BRIDGE", color = LuxAccentGold)
        }
    }

    if (showCreator) {
        TaskCreationDialog(onDismiss = { showCreator = false }, onConfirm = onAdd)
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
        title = { Text("NEW PLANNER ENTRY", color = LuxAccentGold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Objective") })
                OutlinedTextField(value = date, onValueChange = {}, label = { Text("Date") }, readOnly = true, modifier = Modifier.clickable {
                    DatePickerDialog(context, { _, y, m, d -> date = "$y-${m+1}-$d" }, 2024, 0, 1).show()
                }, enabled = false)
                Row {
                    OutlinedTextField(value = start, onValueChange = {}, label = { Text("Start") }, modifier = Modifier.weight(1f).clickable {
                        TimePickerDialog(context, { _, h, m -> start = "$h:$m" }, 9, 0, true).show()
                    }, enabled = false)
                    OutlinedTextField(value = end, onValueChange = {}, label = { Text("End") }, modifier = Modifier.weight(1f).clickable {
                        TimePickerDialog(context, { _, h, m -> end = "$h:$m" }, 10, 0, true).show()
                    }, enabled = false)
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) { onConfirm(title, date, start, end); onDismiss() } }) {
                Text("CONFIRM")
            }
        }
    )
}
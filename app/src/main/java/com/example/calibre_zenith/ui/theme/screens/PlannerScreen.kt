package com.example.calibre_zenith.ui.theme.screens

// Resolves the 'Unresolved reference PauseViewModel' issue

// Resolves 'Property delegate must have a getValue method' for collectAsState()
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.calibre_zenith.ui.theme.SurfacePressed
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import java.util.Calendar
import java.util.Locale

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
    val androidContext = LocalContext.current
    val systemHaptic = LocalHapticFeedback.current
    val triggerHaptic = { systemHaptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    // Core Background Scheduler Engine
    val alarmScheduler = remember { TaskAlarmScheduler(androidContext) }

    // Calendar & Timeline Scopes
    val baseCalendarInstance = remember { Calendar.getInstance() }
    var selectedDayOfYear by remember { mutableStateOf(baseCalendarInstance.get(Calendar.DAY_OF_YEAR)) }
    var selectedYear by remember { mutableStateOf(baseCalendarInstance.get(Calendar.YEAR)) }

    var showMonthDialog by remember { mutableStateOf(false) }
    var monthViewOffset by remember { mutableStateOf(0) }
    var showCreatorSheet by remember { mutableStateOf(false) }

    // Creator Form States
    var taskTitle by remember { mutableStateOf("") }
    var taskMicroTrigger by remember { mutableStateOf("") }
    var taskFrictionByPass by remember { mutableStateOf("") }
    var startHourInt by remember { mutableStateOf(9) }
    var startMinuteInt by remember { mutableStateOf(0) }
    var endHourInt by remember { mutableStateOf(10) }
    var endMinuteInt by remember { mutableStateOf(0) }
    var highFrictionSelected by remember { mutableStateOf(false) }

    // Calculate a perfect 7-Day Horizontal Window pinned from current selection profile
    val incomingDays = remember(selectedDayOfYear, selectedYear) {
        (0..6).map { offset ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.DAY_OF_YEAR, selectedDayOfYear)
            cal.add(Calendar.DAY_OF_YEAR, offset)
            cal
        }
    }

    // Global Interactive Task List Memory Storage
    val tasksList = remember {
        mutableStateListOf(
            DynamicPlannerTask(
                "1", "Setup Organ Perfusion Data Pipeline",
                "Open terminal and execute 'git checkout -b parsing-engine'",
                "Unorganized data formats in raw log file streams",
                Calendar.getInstance().get(Calendar.DAY_OF_YEAR),
                Calendar.getInstance().get(Calendar.YEAR), 9, 30, 11, 0, false
            ),
            DynamicPlannerTask(
                "2", "Deconstruct Expression Script Errors",
                "Load Seurat cluster matrix objects into variable 'ds_raw'",
                "Script environment version conflicts and missing cluster indexes",
                Calendar.getInstance().get(Calendar.DAY_OF_YEAR),
                Calendar.getInstance().get(Calendar.YEAR), 14, 0, 15, 45, true
            )
        )
    }

    val activeDayTasks = tasksList.filter { it.dayOfYear == selectedDayOfYear && it.year == selectedYear }

    // Helper utility to expose the native structural clock wheel dial
    val launchClockDialPicker = { initialHour: Int, initialMinute: Int, onSelected: (Int, Int) -> Unit ->
        TimePickerDialog(
            androidContext,
            { _, pickedHour, pickedMinute -> onSelected(pickedHour, pickedMinute) },
            initialHour,
            initialMinute,
            false
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // --- HEADER CONTROL REGION ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MISSION RUNWAY",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SPATIAL TIME CANVAS",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, SurfacePressed, RoundedCornerShape(12.dp))
                            .clickable { triggerHaptic(); monthViewOffset = 0; showMonthDialog = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text("MONTH", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
                            .size(40.dp)
                            .clickable {
                                triggerHaptic()
                                taskTitle = ""
                                taskMicroTrigger = ""
                                taskFrictionByPass = ""
                                startHourInt = 9
                                startMinuteInt = 0
                                endHourInt = 10
                                endMinuteInt = 0
                                highFrictionSelected = false
                                showCreatorSheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Normal)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, SurfacePressed, RoundedCornerShape(12.dp))
                            .clickable { triggerHaptic(); viewModel.navigateToDashboard() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text("MENU", color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- EVENLY DISTRIBUTED 7-DAY HORIZONTAL WEEK ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                incomingDays.forEach { calInstance ->
                    val isSelected = calInstance.get(Calendar.DAY_OF_YEAR) == selectedDayOfYear &&
                            calInstance.get(Calendar.YEAR) == selectedYear

                    val dayName = calInstance.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) ?: ""
                    val dayNum = calInstance.get(Calendar.DAY_OF_MONTH).toString()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else SurfacePressed,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                triggerHaptic()
                                selectedDayOfYear = calInstance.get(Calendar.DAY_OF_YEAR)
                                selectedYear = calInstance.get(Calendar.YEAR)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayName.uppercase(),
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.secondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayNum,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 24-HOUR SPATIAL TIMELINE INTERACTIVE CANVAS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    for (hour in 0..23) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .clickable {
                                    triggerHaptic()
                                    taskTitle = ""
                                    taskMicroTrigger = ""
                                    taskFrictionByPass = ""
                                    startHourInt = hour
                                    startMinuteInt = 0
                                    endHourInt = (hour + 1).coerceIn(0, 23)
                                    endMinuteInt = 0
                                    highFrictionSelected = false
                                    showCreatorSheet = true
                                },
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d:00", hour),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .width(55.dp)
                                    .padding(top = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(0.5.dp, SurfacePressed.copy(alpha = 0.15f))
                            )
                        }
                    }
                }

                // Foreground Precision Render Overlay Tasks
                activeDayTasks.forEach { task ->
                    val startFloat = task.startHour + (task.startMinute / 60f)
                    val endFloat = task.endHour + (task.endMinute / 60f)

                    val topOffset = (startFloat * 85).dp
                    val blockHeight = ((endFloat - startFloat) * 85).dp

                    val accentColor = if (task.isHighFriction) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 55.dp)
                            .offset(y = topOffset)
                            .height(blockHeight)
                            .padding(2.dp)
                            .background(accentColor.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .border(width = 4.dp, color = accentColor, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                            .clickable { triggerHaptic() }
                            .padding(10.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = task.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d - %02d:%02d", task.startHour, task.startMinute, task.endHour, task.endMinute),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "LAUNCH CORE: ${task.microStep}",
                                color = MaterialTheme.colorScheme.secondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                            if (task.frictionNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "RESISTANCE PROFILE: ${task.frictionNotes}",
                                    color = Color(0xFFFFCC80),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MONTH DIALOG ---
        if (showMonthDialog) {
            val workingMonthCal = remember(monthViewOffset) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, monthViewOffset)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal
            }

            val monthLabel = workingMonthCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) ?: ""
            val yearLabel = workingMonthCal.get(Calendar.YEAR).toString()
            val totalDaysInMonth = workingMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val startDayOfWeekAdjustment = workingMonthCal.get(Calendar.DAY_OF_WEEK) - 1

            AlertDialog(
                onDismissRequest = { showMonthDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(1.dp, SurfacePressed, RoundedCornerShape(24.dp)),
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMonthDialog = false }) {
                        Text("CLOSE OVERLAY", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { triggerHaptic(); monthViewOffset-- }) {
                            Text("<", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "${monthLabel.uppercase()} $yearLabel", fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { triggerHaptic(); monthViewOffset++ }) {
                            Text(">", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { abbr ->
                                Text(abbr, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        var rawDayCounter = 1
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (rowIndex in 0 until 6) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (columnIndex in 0..6) {
                                        val cellIndex = (rowIndex * 7) + columnIndex
                                        if (cellIndex < startDayOfWeekAdjustment || rawDayCounter > totalDaysInMonth) {
                                            Box(modifier = Modifier.weight(1f))
                                        } else {
                                            val capturedDay = rawDayCounter
                                            val targetCal = Calendar.getInstance().apply {
                                                add(Calendar.MONTH, monthViewOffset)
                                                set(Calendar.DAY_OF_MONTH, capturedDay)
                                            }
                                            val isMatch = (targetCal.get(Calendar.DAY_OF_YEAR) == selectedDayOfYear && targetCal.get(Calendar.YEAR) == selectedYear)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .background(color = if (isMatch) MaterialTheme.colorScheme.primary else Color.Transparent, shape = RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        triggerHaptic()
                                                        selectedDayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)
                                                        selectedYear = targetCal.get(Calendar.YEAR)
                                                        showMonthDialog = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = capturedDay.toString(), color = if (isMatch) Color.Black else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                            }
                                            rawDayCounter++
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // --- TASK CREATOR SHEET ---
        AnimatedVisibility(
            visible = showCreatorSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showCreatorSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .border(1.dp, SurfacePressed, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clickable(enabled = false) {}
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "ARCHITECT RUNWAY OBJECTIVE", color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Objective Blueprint Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskMicroTrigger,
                        onValueChange = { taskMicroTrigger = it },
                        label = { Text("60-Sec Launch Trigger Matrix") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Action word start point...") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskFrictionByPass,
                        onValueChange = { taskFrictionByPass = it },
                        label = { Text("Anticipated Cognitive Resistance / Friction Profile") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Identify why you might avoid this task...") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("SPATIAL TEMPORAL BOUNDS", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                                .border(1.dp, SurfacePressed, RoundedCornerShape(12.dp))
                                .clickable {
                                    triggerHaptic()
                                    launchClockDialPicker(startHourInt, startMinuteInt) { h, m ->
                                        startHourInt = h
                                        startMinuteInt = m
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("START TIME", color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", startHourInt, startMinuteInt),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                                .border(1.dp, SurfacePressed, RoundedCornerShape(12.dp))
                                .clickable {
                                    triggerHaptic()
                                    launchClockDialPicker(endHourInt, endMinuteInt) { h, m ->
                                        endHourInt = h
                                        endMinuteInt = m
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("END TIME", color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", endHourInt, endMinuteInt),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                            .clickable { highFrictionSelected = !highFrictionSelected }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Critical Force Friction Rating", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Highlights block edge red to flag complex tasks", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                        }
                        Switch(checked = highFrictionSelected, onCheckedChange = { highFrictionSelected = it })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            triggerHaptic()
                            if (taskTitle.isNotBlank() && taskMicroTrigger.isNotBlank()) {
                                val generatedTask = DynamicPlannerTask(
                                    id = System.currentTimeMillis().toString(),
                                    title = taskTitle,
                                    microStep = taskMicroTrigger,
                                    frictionNotes = taskFrictionByPass,
                                    dayOfYear = selectedDayOfYear,
                                    year = selectedYear,
                                    startHour = startHourInt,
                                    startMinute = startMinuteInt,
                                    endHour = endHourInt,
                                    endMinute = endMinuteInt,
                                    isHighFriction = highFrictionSelected
                                )

                                // Local State updates
                                tasksList.add(generatedTask)

                                // Hand over to kernel background alarm pipeline
                                alarmScheduler.scheduleTaskAlerts(generatedTask)

                                showCreatorSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("ENGAGE INTO TIMELINE", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
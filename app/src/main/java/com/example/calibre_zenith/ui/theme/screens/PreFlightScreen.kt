package com.example.calibre_zenith.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.ui.theme.screens.GeneratingScreen
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import kotlinx.coroutines.launch

@Composable
fun PreFlightScreen(viewModel: PauseViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(1) }

    val taskName by viewModel.taskName.collectAsState()
    val microStep by viewModel.microStep.collectAsState()
    val selectedMonster by viewModel.selectedMonster.collectAsState()
    val selectedHook by viewModel.selectedHook.collectAsState()
    val sessionDuration by viewModel.sessionDurationSeconds.collectAsState()

    val isOptimizing = viewModel.isGeminiOptimizing

    if (isOptimizing) {
        GeneratingScreen()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NEURO-CALIBRATION MATRIX",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PHASE 0$currentStep // 05",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    if (i <= currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "SlideMatrixTransition"
                ) { step ->
                    when (step) {
                        1 -> ConfigurationStepCard(
                            title = "TARGET OBJECTIVE / TASK",
                            subtitle = "Define the complex engineering or research task.",
                            value = taskName,
                            onValueChange = { viewModel.updateTaskName(it) },
                            placeholder = "e.g., Learning Python..."
                        )
                        2 -> ConfigurationStepCard(
                            title = "60-SECOND MECHANICAL MICRO-STEP",
                            subtitle = "The smallest, zero-resistance physical input action.",
                            value = microStep,
                            onValueChange = { viewModel.updateMicroStep(it) },
                            placeholder = "e.g., Opening the IDE, running one simple code..."
                        )
                        3 -> Tactile3DPresetSelectionCard(
                            title = "COGNITIVE RESISTANCE FRICTION PROFILE",
                            subtitle = "Isolate the mental friction architecture.",
                            presets = listOf("Fear of Failure", "Task Inertia", "Distraction Cascade", "Executive Fatigue"),
                            currentValue = selectedMonster,
                            onValueChange = { viewModel.updateSelectedMonster(it) },
                            placeholder = "Define custom cognitive friction..."
                        )
                        4 -> Tactile3DPresetSelectionCard(
                            title = "ENVIRONMENTAL ATTENTION HOOK",
                            subtitle = "Your anchoring reference focal point.",
                            presets = listOf("Terminal Interface", "Noise Isolation Block", "Analog Notebook", "Clean Workspace Matrix"),
                            currentValue = selectedHook,
                            onValueChange = { viewModel.updateSelectedHook(it) },
                            placeholder = "Define custom sensory hook..."
                        )
                        5 -> FixedTactile3DDurationCard(
                            currentDurationSeconds = sessionDuration,
                            onDurationChange = { viewModel.updateSessionDurationSeconds(it) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentStep > 1) {
                    Box(modifier = Modifier.weight(1f)) {
                        Tactile3DNavigationButton(
                            text = "BACK",
                            isSelected = false,
                            onClick = { currentStep-- }
                        )
                    }
                }

                Box(modifier = Modifier.weight(2f)) {
                    Tactile3DNavigationButton(
                        text = if (currentStep == 5) "LAUNCH IGNITION" else "CONTINUE",
                        isSelected = true,
                        onClick = {
                            if (currentStep < 5) {
                                currentStep++
                            } else {
                                coroutineScope.launch {
                                    try {
                                        viewModel.launchCognitiveSessionWorkflow(context)
                                        viewModel.currentScreen = "Timer"
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "FAULT: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigurationStepCard(title: String, subtitle: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
                innerTextField()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Tactile3DPresetSelectionCard(title: String, subtitle: String, presets: List<String>, currentValue: String, onValueChange: (String) -> Unit, placeholder: String) {
    var isCustomMode by remember { mutableStateOf(!presets.contains(currentValue) && currentValue.isNotEmpty()) }
    var customText by remember { mutableStateOf(if (isCustomMode) currentValue else "") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            presets.forEach { preset ->
                val isSelected = !isCustomMode && currentValue == preset
                Box(modifier = Modifier.fillMaxWidth(0.47f)) {
                    Tactile3DTile(text = preset.uppercase(), isSelected = isSelected, onClick = { isCustomMode = false; onValueChange(preset) })
                }
            }
            Box(modifier = Modifier.fillMaxWidth(0.47f)) {
                Tactile3DTile(text = "CUSTOM OVERRIDE", isSelected = isCustomMode, onClick = { isCustomMode = true; onValueChange(customText) })
            }
        }
        AnimatedVisibility(visible = isCustomMode) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                BasicTextField(
                    value = customText,
                    onValueChange = { customText = it; onValueChange(it) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun FixedTactile3DDurationCard(currentDurationSeconds: Int, onDurationChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "CALIBRATED FOCUS RUNTIME BLOCK", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Isolate target mechanical sprint duration.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("2 Min" to 120, "5 Min" to 300, "10 Min" to 600).forEach { (label, durationSeconds) ->
                Box(modifier = Modifier.weight(1f)) {
                    Tactile3DTile(text = label, isSelected = currentDurationSeconds == durationSeconds, onClick = { onDurationChange(durationSeconds) })
                }
            }
        }
    }
}

@Composable
fun Tactile3DTile(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val liveTravelOffset by animateDpAsState(targetValue = if (isSelected) 4.dp else 0.dp, animationSpec = tween(durationMillis = 100), label = "MechanicalTravel")
    Box(modifier = Modifier.padding(bottom = 4.dp, end = 4.dp).clickable { onClick() }) {
        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).background(Color.Black, shape = RoundedCornerShape(4.dp)))
        Box(modifier = Modifier.offset(x = liveTravelOffset, y = liveTravelOffset).background(color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)).border(width = 1.5.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 14.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun Tactile3DNavigationButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val navigationTravelOffset by animateDpAsState(targetValue = if (isSelected) 3.dp else 0.dp, animationSpec = tween(durationMillis = 100), label = "NavButtonTravel")
    Box(modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 3.dp, end = 3.dp).clickable { onClick() }) {
        Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).background(Color.Black, shape = RoundedCornerShape(6.dp)))
        Box(modifier = Modifier.offset(x = navigationTravelOffset, y = navigationTravelOffset).fillMaxSize().background(color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp)).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
            Text(text = text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
        }
    }
}
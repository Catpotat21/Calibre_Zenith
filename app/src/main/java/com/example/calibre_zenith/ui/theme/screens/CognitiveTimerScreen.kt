package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.data.CyberCatMatrix
import com.example.calibre_zenith.ui.components.CatCaricatureVectorDraw
import com.example.calibre_zenith.ui.components.TactileCatPopNode
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

// Define the exact state flow of the session
enum class SessionPhase {
    SETUP,
    COGNITIVE_RADAR,
    MICRO_COMMITMENT,
    CHECKPOINT,
    DEEP_WORK_SETUP, // New phase for post-initiation parameterization
    DEEP_WORK,
    AFFIRMATION_EXIT
}

@Composable
fun CognitiveTimerScreen(viewModel: PauseViewModel) {
    var currentPhase by remember { mutableStateOf(SessionPhase.SETUP) }

    // Global Session State
    var exactGoal by remember { mutableStateOf("") }
    var sixtySecondActivity by remember { mutableStateOf("") }

    // Pre-emptive Friction & Meaning State
    var selectedFriction by remember { mutableStateOf("") }
    var customFriction by remember { mutableStateOf("") }
    var selectedMeaning by remember { mutableStateOf("") }
    var customMeaning by remember { mutableStateOf("") }

    var cognitiveMinutes by remember { mutableFloatStateOf(3f) }

    // Deferred Deep Work State
    var taskEndGoal by remember { mutableStateOf("") }
    var targetTaskDurationMinutes by remember { mutableFloatStateOf(25f) }

    // Intercept Viewmodel timer for the tactile grid phase
    LaunchedEffect(viewModel.secondsLeft) {
        if (currentPhase == SessionPhase.COGNITIVE_RADAR && viewModel.secondsLeft == 0) {
            currentPhase = SessionPhase.MICRO_COMMITMENT
        }
    }

    when (currentPhase) {
        SessionPhase.SETUP -> {
            GuidedSetupPhase(
                exactGoal = exactGoal,
                onExactGoalChange = { exactGoal = it },
                sixtySecondActivity = sixtySecondActivity,
                onSixtySecondActivityChange = { sixtySecondActivity = it },
                selectedFriction = selectedFriction,
                onFrictionChange = { selectedFriction = it },
                customFriction = customFriction,
                onCustomFrictionChange = { customFriction = it },
                selectedMeaning = selectedMeaning,
                onMeaningChange = { selectedMeaning = it },
                customMeaning = customMeaning,
                onCustomMeaningChange = { customMeaning = it },
                cognitiveMinutes = cognitiveMinutes,
                onCognitiveTimeChange = { cognitiveMinutes = it },
                onLaunch = {
                    val finalFriction = if (selectedFriction == "Custom...") customFriction else selectedFriction
                    val finalMeaning = if (selectedMeaning == "Custom...") customMeaning else selectedMeaning

                    viewModel.launchTileEngine(
                        title = exactGoal,
                        trigger = sixtySecondActivity,
                        resistance = finalFriction,
                        meaning = finalMeaning,
                        durationMinutes = cognitiveMinutes.toInt()
                    )
                    currentPhase = SessionPhase.COGNITIVE_RADAR
                }
            )
        }
        SessionPhase.COGNITIVE_RADAR -> {
            ActiveGameEnginePhase(viewModel)
        }
        SessionPhase.MICRO_COMMITMENT -> {
            MicroCommitmentPhase(
                onTimeUp = { currentPhase = SessionPhase.CHECKPOINT }
            )
        }
        SessionPhase.CHECKPOINT -> {
            CheckpointPhase(
                onContinue = { currentPhase = SessionPhase.DEEP_WORK_SETUP },
                onBreak = { currentPhase = SessionPhase.AFFIRMATION_EXIT }
            )
        }
        SessionPhase.DEEP_WORK_SETUP -> {
            DeferredDeepWorkSetupPhase(
                endGoal = taskEndGoal,
                onEndGoalChange = { taskEndGoal = it },
                taskMinutes = targetTaskDurationMinutes,
                onTaskTimeChange = { targetTaskDurationMinutes = it },
                onLaunchDeepWork = { currentPhase = SessionPhase.DEEP_WORK }
            )
        }
        SessionPhase.DEEP_WORK -> {
            DeepWorkExecutionPhase(
                endGoal = taskEndGoal,
                durationMinutes = targetTaskDurationMinutes.toInt(),
                onSessionCompleteOrAborted = {
                    currentPhase = SessionPhase.SETUP
                    viewModel.clearSessionAndGoBack()
                }
            )
        }
        SessionPhase.AFFIRMATION_EXIT -> {
            AffirmationExitPhase(
                onAcknowledge = {
                    currentPhase = SessionPhase.SETUP
                    viewModel.clearSessionAndGoBack()
                }
            )
        }
    }
}

// =================================================================
// 📝 PHASE A: GUIDED SETUP (No Deep Work Params here)
// =================================================================
@Composable
fun GuidedSetupPhase(
    exactGoal: String,
    onExactGoalChange: (String) -> Unit,
    sixtySecondActivity: String,
    onSixtySecondActivityChange: (String) -> Unit,
    selectedFriction: String,
    onFrictionChange: (String) -> Unit,
    customFriction: String,
    onCustomFrictionChange: (String) -> Unit,
    selectedMeaning: String,
    onMeaningChange: (String) -> Unit,
    customMeaning: String,
    onCustomMeaningChange: (String) -> Unit,
    cognitiveMinutes: Float,
    onCognitiveTimeChange: (Float) -> Unit,
    onLaunch: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 4

    val frictionOptions = listOf("Task Overwhelm", "Perfectionism", "Starting Inertia", "Fear of Failure", "Custom...")
    val meaningOptions = listOf("Action Overrides Anxiety", "Momentum over Perfection", "I Honor My Intentions", "Custom...")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "INITIATION SEQUENCE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { step.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "SetupAnimation"
            ) { currentStep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (currentStep) {
                            1 -> StepInput(title = "EXACT GOAL", label = "What is the task?", value = exactGoal, onValueChange = onExactGoalChange)
                            2 -> StepInput(title = "60-SECOND ACTIVITY", label = "What is the bare minimum first step?", value = sixtySecondActivity, onValueChange = onSixtySecondActivityChange)
                            3 -> {
                                StepChips(title = "IDENTIFY THE FRICTION", options = frictionOptions, selected = selectedFriction, onSelected = onFrictionChange)
                                if (selectedFriction == "Custom...") {
                                    OutlinedTextField(value = customFriction, onValueChange = onCustomFrictionChange, label = { Text("What is holding you back?") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                StepChips(title = "THE MEANING", options = meaningOptions, selected = selectedMeaning, onSelected = onMeaningChange)
                                if (selectedMeaning == "Custom...") {
                                    OutlinedTextField(value = customMeaning, onValueChange = onCustomMeaningChange, label = { Text("What do you prove by starting?") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                                }
                            }
                            4 -> StepSliderInput(title = "RADAR DURATION (MIN)", value = cognitiveMinutes, valueRange = 1f..10f, steps = 9, unitLabel = "MIN", onValueChange = onCognitiveTimeChange)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (step > 1) {
                OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("BACK", fontFamily = FontFamily.Monospace)
                }
            }

            val isNextEnabled = when(step) {
                1 -> exactGoal.isNotBlank()
                2 -> sixtySecondActivity.isNotBlank()
                3 -> selectedFriction.isNotBlank() && (selectedFriction != "Custom..." || customFriction.isNotBlank()) &&
                        selectedMeaning.isNotBlank() && (selectedMeaning != "Custom..." || customMeaning.isNotBlank())
                else -> true
            }

            Button(
                onClick = { if (step < totalSteps) step++ else onLaunch() },
                modifier = Modifier.weight(2f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isNextEnabled
            ) {
                Text(if (step < totalSteps) "NEXT" else "SYSTEM START", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =================================================================
// 🎮 PHASE B: ACTIVE TACTILE GAME ENGINE
// =================================================================
@Composable
fun ActiveGameEnginePhase(viewModel: PauseViewModel) {
    // (Unchanged from previous iteration)
    LaunchedEffect(key1 = viewModel.secondsLeft) {
        if (viewModel.secondsLeft > 0) { delay(1.seconds); viewModel.tickDownOneSecond() }
    }

    val minutes = viewModel.secondsLeft / 60
    val seconds = viewModel.secondsLeft % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val safeCatProfile = CyberCatMatrix.matrix.first()
    var activeGridIndex by remember { mutableIntStateOf((0..8).random()) }
    var lightUpProgressTarget by remember { mutableStateOf(1f) }

    val illuminationAlpha by animateFloatAsState(
        targetValue = lightUpProgressTarget,
        animationSpec = tween(durationMillis = 1800),
        finishedListener = { finalAlpha ->
            if (finalAlpha == 0f) {
                activeGridIndex = (0..8).filter { it != activeGridIndex }.random()
                lightUpProgressTarget = 1f
            }
        },
        label = "AlphaAnim"
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF07080D)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("COGNITIVE RADAR ACTIVE", color = Color(0xFFEF5350), letterSpacing = 2.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        Text(timeString, fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(9) { index ->
                    val isCurrentTarget = index == activeGridIndex
                    Box(modifier = Modifier.size(84.dp).shadow(if (isCurrentTarget) (6.dp * illuminationAlpha) else 0.dp, RoundedCornerShape(18.dp), spotColor = safeCatProfile.glowColor)) {
                        TactileCatPopNode(
                            profile = safeCatProfile,
                            isActive = isCurrentTarget && illuminationAlpha > 0.1f,
                            enabled = isCurrentTarget && illuminationAlpha >= 1.0f,
                            onTap = { if (isCurrentTarget && illuminationAlpha >= 1.0f) lightUpProgressTarget = 0f }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.clearSessionAndGoBack() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ABORT", color = MaterialTheme.colorScheme.onErrorContainer, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// ⏱️ PHASE C: 60-SECOND MICRO-COMMITMENT
// =================================================================
@Composable
fun MicroCommitmentPhase(onTimeUp: () -> Unit) {
    var secondsLeft by remember { mutableIntStateOf(60) }

    LaunchedEffect(key1 = secondsLeft) {
        if (secondsLeft > 0) {
            delay(1.seconds)
            secondsLeft--
        } else {
            onTimeUp()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B0D16)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DO THE BARE MINIMUM",
            color = Color(0xFF00FFCC),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Execute your 60-second activity now. You have permission to quit after this.",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "$secondsLeft",
            color = Color.White,
            fontSize = 96.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

// =================================================================
// 🚦 PHASE D: THE CHECKPOINT
// =================================================================
@Composable
fun CheckpointPhase(onContinue: () -> Unit, onBreak: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07080D)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SEAL BROKEN",
            color = Color(0xFFFFB300),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "You survived the first 60 seconds. The hardest part is over. What is your call?",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(70.dp).shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF00FFCC)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC).copy(alpha = 0.1f)),
            border = BorderStroke(2.dp, Color(0xFF00FFCC)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("MOMENTUM ACHIEVED // KEEP GOING", color = Color(0xFF00FFCC), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onBreak,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("HONOR THE LIMIT // I NEED TO STOP", color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace)
        }
    }
}

// =================================================================
// ⚙️ PHASE E: DEFERRED DEEP WORK SETUP (Progressive Disclosure)
// =================================================================
@Composable
fun DeferredDeepWorkSetupPhase(
    endGoal: String,
    onEndGoalChange: (String) -> Unit,
    taskMinutes: Float,
    onTaskTimeChange: (Float) -> Unit,
    onLaunchDeepWork: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF06070B)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MOMENTUM SECURED.",
            color = Color(0xFF00FFCC),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Since you're already moving, let's define the finish line.",
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11131F)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("SPECIFIC END GOAL", fontWeight = FontWeight.Bold, color = Color(0xFF00FFCC), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                OutlinedTextField(
                    value = endGoal,
                    onValueChange = onEndGoalChange,
                    label = { Text("What does 'done' look like?", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00FFCC)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text("TARGET EXECUTION TIME", fontWeight = FontWeight.Bold, color = Color(0xFF00FFCC), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Text("${taskMinutes.toInt()} MINUTES", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, fontSize = 32.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White)
                Slider(
                    value = taskMinutes,
                    onValueChange = onTaskTimeChange,
                    valueRange = 5f..120f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00FFCC), activeTrackColor = Color(0xFF00FFCC))
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onLaunchDeepWork,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
            shape = RoundedCornerShape(16.dp),
            enabled = endGoal.isNotBlank()
        ) {
            Text("ENGAGE DEEP WORK", color = Color(0xFF06070B), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        }
    }
}

// =================================================================
// ⚡ PHASE F: DEEP WORK
// =================================================================
@Composable
fun DeepWorkExecutionPhase(endGoal: String, durationMinutes: Int, onSessionCompleteOrAborted: () -> Unit) {
    var taskSecondsLeft by remember { mutableIntStateOf(durationMinutes * 60) }
    LaunchedEffect(key1 = taskSecondsLeft) {
        if (taskSecondsLeft > 0) { delay(1.seconds); taskSecondsLeft-- }
    }
    val formattedTaskTime = String.format(Locale.getDefault(), "%02d:%02d", taskSecondsLeft / 60, taskSecondsLeft % 60)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF06070B)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("DEEP WORK RUNNING", color = Color(0xFF00FFCC), letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(36.dp))
        Text(formattedTaskTime, fontSize = 72.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White)
        Spacer(modifier = Modifier.height(40.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF11131F)), border = BorderStroke(1.5.dp, Color(0xFFFFB300).copy(alpha = 0.8f))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TARGET OUTPUT:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFFB300))
                Spacer(modifier = Modifier.height(8.dp))
                Text(endGoal.uppercase(), color = Color.White, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
        Button(onClick = onSessionCompleteOrAborted, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D161B)), border = BorderStroke(1.dp, Color(0xFFEF5350))) {
            Text(if (taskSecondsLeft == 0) "COMPLETE SESSION" else "ABORT BLOCK", color = Color(0xFFEF5350), fontFamily = FontFamily.Monospace)
        }
    }
}

// =================================================================
// 🏆 PHASE G: AFFIRMATION EXIT
// =================================================================
@Composable
fun AffirmationExitPhase(onAcknowledge: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B0D16)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EXECUTIVE DYSFUNCTION OVERRIDDEN.",
            color = Color(0xFF00FFCC),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            lineHeight = 36.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "You successfully broke the inertia. You showed up, looked the resistance in the eye, and executed for 60 seconds. Rest is productive now.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(60.dp))
        Button(
            onClick = onAcknowledge,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ACKNOWLEDGE & DISCONNECT", color = Color(0xFF0B0D16), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        }
    }
}

// =================================================================
// 🧱 UTILITIES
// =================================================================
@Composable
fun StepSliderInput(title: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, steps: Int, unitLabel: String, onValueChange: (Float) -> Unit) {
    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    Text("${value.toInt()} $unitLabel", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, fontSize = 36.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
}

@Composable
fun StepInput(title: String, label: String, value: String, onValueChange: (String) -> Unit) {
    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label, fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
}

@Composable
fun StepChips(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onSelected(option) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Text(option, modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
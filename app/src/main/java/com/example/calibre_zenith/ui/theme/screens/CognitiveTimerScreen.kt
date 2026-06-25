package com.example.calibre_zenith.ui.theme.screens

import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.data.CyberCatMatrix
import com.example.calibre_zenith.ui.components.CatCaricatureVectorDraw
import com.example.calibre_zenith.ui.components.TactileCatPopNode
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

// =================================================================
// 🎨 LUXURY PALETTE & STYLING CONSTANTS
// =================================================================
private val LuxBgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF0D0D11), Color(0xFF050507)))
private val LuxSurface = Color(0xFF141419)
private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxTextPrimary = Color(0xFFF0F0F5)
private val LuxTextMuted = Color(0xFFA2A2AB)
private val LuxBorderSubtle = LuxAccentGold.copy(alpha = 0.15f)

enum class SessionPhase {
    SETUP, COGNITIVE_RADAR, MICRO_COMMITMENT, CHECKPOINT, DEEP_WORK_SETUP, DEEP_WORK, REALIZATION_CHECK_IN, AFFIRMATION_EXIT
}

@Composable
fun CognitiveTimerScreen(viewModel: PauseViewModel) {
    var currentPhase by remember { mutableStateOf(SessionPhase.SETUP) }

    // Global Session State
    var exactGoal by remember { mutableStateOf("") }
    var sixtySecondActivity by remember { mutableStateOf("") }
    var selectedFriction by remember { mutableStateOf("") }
    var customFriction by remember { mutableStateOf("") }
    var selectedMeaning by remember { mutableStateOf("") }
    var customMeaning by remember { mutableStateOf("") }
    var cognitiveMinutes by remember { mutableFloatStateOf(3f) }

    var taskEndGoal by remember { mutableStateOf("") }
    var targetTaskDurationMinutes by remember { mutableFloatStateOf(25f) }

    // ── NEW: sub-goals list lives here so it survives phase transitions ──
    val taskSubGoals = remember { mutableStateListOf<String>() }

    var realizationLog by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.secondsLeft) {
        if (currentPhase == SessionPhase.COGNITIVE_RADAR && viewModel.secondsLeft == 0) {
            currentPhase = SessionPhase.MICRO_COMMITMENT
        }
    }

    when (currentPhase) {
        SessionPhase.SETUP -> {
            GuidedSetupPhase(
                exactGoal = exactGoal, onExactGoalChange = { exactGoal = it },
                sixtySecondActivity = sixtySecondActivity, onSixtySecondActivityChange = { sixtySecondActivity = it },
                selectedFriction = selectedFriction, onFrictionChange = { selectedFriction = it },
                customFriction = customFriction, onCustomFrictionChange = { customFriction = it },
                selectedMeaning = selectedMeaning, onMeaningChange = { selectedMeaning = it },
                customMeaning = customMeaning, onCustomMeaningChange = { customMeaning = it },
                cognitiveMinutes = cognitiveMinutes, onCognitiveTimeChange = { cognitiveMinutes = it },
                onLaunch = {
                    val finalFriction = if (selectedFriction == "Custom...") customFriction else selectedFriction
                    val finalMeaning = if (selectedMeaning == "Custom...") customMeaning else selectedMeaning
                    viewModel.launchTileEngine(exactGoal, sixtySecondActivity, finalFriction, finalMeaning, cognitiveMinutes.toInt())
                    currentPhase = SessionPhase.COGNITIVE_RADAR
                }
            )
        }
        SessionPhase.COGNITIVE_RADAR -> ActiveGameEnginePhase(viewModel)
        SessionPhase.MICRO_COMMITMENT -> MicroCommitmentPhase(onTimeUp = { currentPhase = SessionPhase.CHECKPOINT })
        SessionPhase.CHECKPOINT -> CheckpointPhase(
            onContinue = { currentPhase = SessionPhase.DEEP_WORK_SETUP },
            onBreak = { currentPhase = SessionPhase.REALIZATION_CHECK_IN }
        )

        // ── MODIFIED: pass sub-goals callbacks ──
        SessionPhase.DEEP_WORK_SETUP -> DeferredDeepWorkSetupPhase(
            endGoal = taskEndGoal,
            onEndGoalChange = { taskEndGoal = it },
            taskMinutes = targetTaskDurationMinutes,
            onTaskTimeChange = { targetTaskDurationMinutes = it },
            subGoals = taskSubGoals,
            onAddSubGoal = { taskSubGoals.add("") },
            onSubGoalChange = { index, value -> taskSubGoals[index] = value },
            onRemoveSubGoal = { taskSubGoals.removeAt(it) },
            onLaunchDeepWork = { currentPhase = SessionPhase.DEEP_WORK }
        )

        // ── MODIFIED: pass filtered sub-goals list ──
        SessionPhase.DEEP_WORK -> DeepWorkExecutionPhase(
            endGoal = taskEndGoal,
            durationMinutes = targetTaskDurationMinutes.toInt(),
            subGoals = taskSubGoals.filter { it.isNotBlank() },
            onSessionCompleteOrAborted = { currentPhase = SessionPhase.REALIZATION_CHECK_IN }
        )

        SessionPhase.REALIZATION_CHECK_IN -> RealizationCheckInPhase(
            realizationText = realizationLog,
            onRealizationChange = { realizationLog = it },
            onComplete = { currentPhase = SessionPhase.AFFIRMATION_EXIT }
        )

        SessionPhase.AFFIRMATION_EXIT -> AffirmationExitPhase({
            // Reset everything on full session completion
            taskSubGoals.clear()
            currentPhase = SessionPhase.SETUP
            viewModel.clearSessionAndGoBack()
        })
    }
}

// =================================================================
// PHASE A: GUIDED SETUP  (unchanged)
// =================================================================
@Composable
fun GuidedSetupPhase(exactGoal: String, onExactGoalChange: (String) -> Unit, sixtySecondActivity: String, onSixtySecondActivityChange: (String) -> Unit, selectedFriction: String, onFrictionChange: (String) -> Unit, customFriction: String, onCustomFrictionChange: (String) -> Unit, selectedMeaning: String, onMeaningChange: (String) -> Unit, customMeaning: String, onCustomMeaningChange: (String) -> Unit, cognitiveMinutes: Float, onCognitiveTimeChange: (Float) -> Unit, onLaunch: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 4
    var isVisible by remember { mutableStateOf(false) }

    val frictionOptions = listOf("Task Overwhelm", "Perfectionism", "Starting Inertia", "Fear of Failure", "Custom...")
    val meaningOptions = listOf("Action Overrides Anxiety", "Momentum over Perfection", "I Honor My Intentions", "Custom...")

    LaunchedEffect(Unit) { delay(100); isVisible = true }

    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(1200)) + slideInVertically(initialOffsetY = { -20 })) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "CALIBRE ZENITH", fontSize = 10.sp, color = LuxTextMuted, letterSpacing = 6.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "INITIATION SEQUENCE", fontSize = 14.sp, color = LuxAccentGold, letterSpacing = 4.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(progress = { step.toFloat() / totalSteps.toFloat() }, modifier = Modifier.fillMaxWidth().height(2.dp), color = LuxAccentGold, trackColor = LuxSurface)
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedContent(targetState = step, transitionSpec = { if (targetState > initialState) { (slideInHorizontally { it / 2 } + fadeIn(tween(400))).togetherWith(slideOutHorizontally { -it / 2 } + fadeOut(tween(400))) } else { (slideInHorizontally { -it / 2 } + fadeIn(tween(400))).togetherWith(slideOutHorizontally { it / 2 } + fadeOut(tween(400))) } }, label = "SetupAnimation") { currentStep ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LuxSurface), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, LuxBorderSubtle)) {
                    Column(modifier = Modifier.padding(28.dp).verticalScroll(rememberScrollState())) {
                        when (currentStep) {
                            1 -> StepInput("EXACT GOAL", "What is the task?", exactGoal, onExactGoalChange)
                            2 -> StepInput("60-SECOND ACTIVITY", "What is the bare minimum first step?", sixtySecondActivity, onSixtySecondActivityChange)
                            3 -> {
                                StepChips("IDENTIFY THE FRICTION", frictionOptions, selectedFriction, onFrictionChange)
                                if (selectedFriction == "Custom...") LuxTextField(customFriction, onCustomFrictionChange, "What is holding you back?")
                                Spacer(modifier = Modifier.height(32.dp))
                                StepChips("THE MEANING", meaningOptions, selectedMeaning, onMeaningChange)
                                if (selectedMeaning == "Custom...") LuxTextField(customMeaning, onCustomMeaningChange, "What do you prove by starting?")
                            }
                            4 -> StepSliderInput("RADAR DURATION (MIN)", cognitiveMinutes, 1f..10f, 9, "MIN", onCognitiveTimeChange)
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (step > 1) { OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, LuxBorderSubtle), colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxTextPrimary)) { Text("BACK", letterSpacing = 2.sp, fontSize = 12.sp) } }
            val isNextEnabled = when(step) { 1 -> exactGoal.isNotBlank(); 2 -> sixtySecondActivity.isNotBlank(); 3 -> selectedFriction.isNotBlank() && (selectedFriction != "Custom..." || customFriction.isNotBlank()) && selectedMeaning.isNotBlank() && (selectedMeaning != "Custom..." || customMeaning.isNotBlank()); else -> true }
            Button(onClick = { if (step < totalSteps) step++ else onLaunch() }, modifier = Modifier.weight(2f).height(56.dp), shape = RoundedCornerShape(12.dp), enabled = isNextEnabled, colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold, contentColor = Color(0xFF050507), disabledContainerColor = LuxSurface, disabledContentColor = LuxTextMuted)) { Text(if (step < totalSteps) "NEXT" else "SYSTEM START", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp) }
        }
    }
}

// =================================================================
// PHASE B: ACTIVE GAME ENGINE  (unchanged)
// =================================================================
@Composable
fun ActiveGameEnginePhase(viewModel: PauseViewModel) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    LaunchedEffect(key1 = viewModel.secondsLeft) { if (viewModel.secondsLeft > 0) { delay(1.seconds); viewModel.tickDownOneSecond() } }

    val minutes = viewModel.secondsLeft / 60
    val seconds = viewModel.secondsLeft % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val dynamicAffirmations by viewModel.directivesState.collectAsState()
    var currentAffirmation by remember { mutableStateOf("ACTION PRECEDES MOMENTUM.") }

    LaunchedEffect(key1 = dynamicAffirmations) {
        var counter = 0
        val activePool = dynamicAffirmations.ifEmpty { listOf("ACTION PRECEDES MOMENTUM. YOU DO NOT NEED CLARITY TO BEGIN.", "YOUR UNIVERSE HAS SHRUNK TO EXACTLY THIS STEP. THE REST IS OFFLINE.") }
        if (activePool.isNotEmpty()) {
            currentAffirmation = activePool[counter % activePool.size]
            while (true) { delay(7000L); counter++; currentAffirmation = activePool[counter % activePool.size] }
        }
    }

    val safeCatProfile = CyberCatMatrix.matrix.first()
    var activeGridIndex by remember { mutableIntStateOf((0..8).random()) }
    var lightUpProgressTarget by remember { mutableStateOf(1f) }
    var isFadingOutFast by remember { mutableStateOf(false) }

    val illuminationAlpha by animateFloatAsState(targetValue = lightUpProgressTarget, animationSpec = tween(durationMillis = if (isFadingOutFast) 100 else 1000), finishedListener = { finalAlpha -> if (finalAlpha == 0f) { isFadingOutFast = false; activeGridIndex = (0..8).filter { it != activeGridIndex }.random(); lightUpProgressTarget = 1f } }, label = "AlphaAnim")

    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        if (viewModel.isGeminiOptimizing) { Text("OPTIMIZING COGNITIVE INTEGRITY...", color = LuxAccentGold.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 10.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace) } else { Text("COGNITIVE RADAR ACTIVE", color = LuxAccentGold, letterSpacing = 3.sp, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(modifier = Modifier.height(24.dp))
        Text(timeString, fontSize = 56.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace, color = LuxTextPrimary, letterSpacing = 4.sp)
        Spacer(modifier = Modifier.weight(0.5f))
        AnimatedContent(targetState = currentAffirmation, transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) }, label = "AffirmationFade") { text -> Text(text = text.uppercase(), color = LuxTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, lineHeight = 18.sp, modifier = Modifier.padding(horizontal = 16.dp)) }
        Spacer(modifier = Modifier.weight(0.5f))
        val density = LocalDensity.current.density
        Box(modifier = Modifier.graphicsLayer { cameraDistance = 14f * density }.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
            LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), userScrollEnabled = false, contentPadding = PaddingValues(8.dp)) {
                items(9) { index ->
                    val isCurrentTarget = index == activeGridIndex
                    val isFullyLoaded = isCurrentTarget && illuminationAlpha >= 0.99f
                    val popScale by animateFloatAsState(targetValue = if (isFullyLoaded) 1.06f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "ReadyPop")
                    Box(modifier = Modifier.size(84.dp).graphicsLayer { scaleX = if (isCurrentTarget) popScale else 1f; scaleY = if (isCurrentTarget) popScale else 1f }.shadow(elevation = if (isFullyLoaded) 16.dp else 0.dp, shape = RoundedCornerShape(18.dp), spotColor = LuxAccentGold, ambientColor = LuxAccentGold.copy(alpha = 0.5f)).background(LuxSurface, RoundedCornerShape(18.dp)).border(width = if (isFullyLoaded) 2.dp else 1.dp, color = if (isFullyLoaded) LuxAccentGold else LuxBorderSubtle, shape = RoundedCornerShape(18.dp))) {
                        TactileCatPopNode(profile = safeCatProfile.copy(glowColor = LuxAccentGold), isActive = isCurrentTarget && illuminationAlpha > 0.1f, enabled = isFullyLoaded, onTap = { if (isFullyLoaded) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); view.playSoundEffect(SoundEffectConstants.CLICK); isFadingOutFast = true; lightUpProgressTarget = 0f } })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { viewModel.clearSessionAndGoBack() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFF4A1515))) { Text("ABORT SEQUENCE", color = Color(0xFFD94A4A), letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

// =================================================================
// PHASE C: MICRO-COMMITMENT  (unchanged)
// =================================================================
@Composable
fun MicroCommitmentPhase(onTimeUp: () -> Unit) {
    var secondsLeft by remember { mutableIntStateOf(60) }
    LaunchedEffect(key1 = secondsLeft) { if (secondsLeft > 0) { delay(1.seconds); secondsLeft-- } else { onTimeUp() } }
    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("DO THE BARE MINIMUM", color = LuxAccentGold, fontSize = 20.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Execute your 60-second activity now. You have permission to quit after this.", color = LuxTextMuted, textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(64.dp))
        Text(String.format(Locale.getDefault(), "%02d", secondsLeft), color = LuxTextPrimary, fontSize = 110.sp, fontWeight = FontWeight.ExtraLight, fontFamily = FontFamily.Monospace, letterSpacing = 8.sp)
    }
}

// =================================================================
// PHASE D: CHECKPOINT  (unchanged)
// =================================================================
@Composable
fun CheckpointPhase(onContinue: () -> Unit, onBreak: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("SEAL BROKEN", color = LuxTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Light, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("You survived the first 60 seconds. The hardest part is over. What is your call?", color = LuxTextMuted, textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(56.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold), shape = RoundedCornerShape(16.dp)) { Text("MOMENTUM ACHIEVED // KEEP GOING", color = Color(0xFF050507), letterSpacing = 1.sp, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBreak, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent), border = BorderStroke(1.dp, LuxBorderSubtle), shape = RoundedCornerShape(16.dp)) { Text("HONOR THE LIMIT // I NEED TO STOP", color = LuxTextMuted, letterSpacing = 1.sp, fontSize = 13.sp) }
    }
}

// =================================================================
// PHASE E: DEEP WORK SETUP  ── MODIFIED: sub-goals support ──
// =================================================================
@Composable
fun DeferredDeepWorkSetupPhase(
    endGoal: String,
    onEndGoalChange: (String) -> Unit,
    taskMinutes: Float,
    onTaskTimeChange: (Float) -> Unit,
    subGoals: List<String>,
    onAddSubGoal: () -> Unit,
    onSubGoalChange: (Int, String) -> Unit,
    onRemoveSubGoal: (Int) -> Unit,
    onLaunchDeepWork: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxBgGradient)
            .verticalScroll(rememberScrollState())   // scrollable so sub-goals never overflow
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Text("MOMENTUM SECURED", color = LuxAccentGold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Since you're already moving, let's define the finish line.", color = LuxTextMuted, textAlign = TextAlign.Center, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LuxSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LuxBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {

                // ── Main task ──────────────────────────────────────────
                Text("SPECIFIC END GOAL", fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LuxTextField(endGoal, onEndGoalChange, "What does 'done' look like?")

                Spacer(modifier = Modifier.height(28.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LuxBorderSubtle))
                Spacer(modifier = Modifier.height(28.dp))

                // ── Milestones (optional) ──────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MILESTONES", fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
                        Text("optional", fontSize = 10.sp, color = LuxTextMuted, letterSpacing = 1.sp)
                    }
                    // Sleek + button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(LuxAccentGold.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                            .border(1.dp, LuxAccentGold.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .clickable { onAddSubGoal() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add milestone",
                            tint = LuxAccentGold,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                if (subGoals.isEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Tap + to break the goal into milestones",
                        fontSize = 11.sp,
                        color = LuxTextMuted.copy(alpha = 0.45f),
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                    subGoals.forEachIndexed { index, subGoal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bullet dot
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(LuxAccentGold.copy(alpha = 0.55f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = subGoal,
                                onValueChange = { onSubGoalChange(index, it) },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text("Milestone ${index + 1}", color = LuxTextMuted, fontSize = 12.sp)
                                },
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = LuxTextPrimary),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuxAccentGold.copy(alpha = 0.55f),
                                    unfocusedBorderColor = LuxBorderSubtle,
                                    cursorColor = LuxAccentGold,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                            // Remove button
                            IconButton(
                                onClick = { onRemoveSubGoal(index) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove milestone",
                                    tint = LuxTextMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LuxBorderSubtle))
                Spacer(modifier = Modifier.height(28.dp))

                // ── Duration ───────────────────────────────────────────
                Text("TARGET EXECUTION TIME", fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
                Text(
                    "${taskMinutes.toInt()} MIN",
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace,
                    color = LuxAccentGold
                )
                Slider(
                    value = taskMinutes,
                    onValueChange = onTaskTimeChange,
                    valueRange = 5f..120f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = LuxAccentGold,
                        activeTrackColor = LuxAccentGold,
                        inactiveTrackColor = LuxSurface,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onLaunchDeepWork,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuxAccentGold,
                disabledContainerColor = LuxSurface
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = endGoal.isNotBlank()
        ) {
            Text(
                "ENGAGE DEEP WORK",
                color = if (endGoal.isNotBlank()) Color(0xFF050507) else LuxTextMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// =================================================================
// PHASE F: DEEP WORK EXECUTION  ── MODIFIED: crossable checklist ──
// =================================================================
@Composable
fun DeepWorkExecutionPhase(
    endGoal: String,
    durationMinutes: Int,
    subGoals: List<String>,
    onSessionCompleteOrAborted: () -> Unit
) {
    var taskSecondsLeft by remember { mutableIntStateOf(durationMinutes * 60) }
    LaunchedEffect(key1 = taskSecondsLeft) {
        if (taskSecondsLeft > 0) { delay(1.seconds); taskSecondsLeft-- }
    }
    val formattedTaskTime = String.format(Locale.getDefault(), "%02d:%02d", taskSecondsLeft / 60, taskSecondsLeft % 60)

    // Per-item completion state (keyed on list size so a re-enter resets it)
    var mainTaskDone by remember { mutableStateOf(false) }
    val subGoalsDone = remember(subGoals.size) {
        mutableStateListOf<Boolean>().also { list ->
            repeat(subGoals.size) { list.add(false) }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DEEP WORK RUNNING", color = LuxAccentGold, letterSpacing = 4.sp, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            formattedTaskTime,
            fontSize = 80.sp,
            fontWeight = FontWeight.ExtraLight,
            fontFamily = FontFamily.Monospace,
            color = LuxTextPrimary,
            letterSpacing = 6.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LuxSurface),
            border = BorderStroke(1.dp, LuxBorderSubtle),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("TARGET OUTPUT", fontSize = 10.sp, color = LuxTextMuted, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // ── Main task row ──────────────────────────────────────
                TaskCheckRow(
                    text = endGoal.uppercase(),
                    isDone = mainTaskDone,
                    onToggle = { mainTaskDone = !mainTaskDone },
                    isMainTask = true
                )

                // ── Sub-goal rows (indented) ───────────────────────────
                if (subGoals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    subGoals.forEachIndexed { index, subGoal ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.padding(start = 10.dp)) {
                            TaskCheckRow(
                                text = subGoal,
                                isDone = subGoalsDone.getOrElse(index) { false },
                                onToggle = {
                                    if (index < subGoalsDone.size) {
                                        subGoalsDone[index] = !subGoalsDone[index]
                                    }
                                },
                                isMainTask = false
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
        OutlinedButton(
            onClick = onSessionCompleteOrAborted,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (taskSecondsLeft == 0) LuxAccentGold else Color(0xFF4A1515))
        ) {
            Text(
                if (taskSecondsLeft == 0) "COMPLETE SESSION" else "ABORT BLOCK",
                color = if (taskSecondsLeft == 0) LuxAccentGold else Color(0xFFD94A4A),
                letterSpacing = 2.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =================================================================
// PHASE G: REALIZATION CHECK-IN  (unchanged)
// =================================================================
@Composable
fun RealizationCheckInPhase(
    realizationText: String,
    onRealizationChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("POST-EXECUTION DEBRIEF", color = LuxAccentGold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Take a moment to record the friction you felt, how you overcame it, and what you learned.", color = LuxTextMuted, textAlign = TextAlign.Center, fontSize = 13.sp, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = LuxSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LuxBorderSubtle)
        ) {
            OutlinedTextField(
                value = realizationText,
                onValueChange = onRealizationChange,
                placeholder = { Text("Log your feelings and realizations here...", color = LuxTextMuted, fontSize = 14.sp) },
                modifier = Modifier.fillMaxSize().padding(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = LuxTextPrimary, lineHeight = 22.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = LuxAccentGold
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("FINISH DEBRIEF", color = Color(0xFF050507), letterSpacing = 2.sp, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// =================================================================
// PHASE H: AFFIRMATION EXIT  (unchanged)
// =================================================================
@Composable
fun AffirmationExitPhase(onAcknowledge: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("EXECUTIVE DYSFUNCTION\nOVERRIDDEN", color = LuxTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Text("You successfully broke the inertia. You showed up, looked the resistance in the eye, and executed. Rest is productive now.", color = LuxTextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(64.dp))
        Button(onClick = onAcknowledge, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold), shape = RoundedCornerShape(16.dp)) { Text("ACKNOWLEDGE & DISCONNECT", color = Color(0xFF050507), letterSpacing = 1.sp, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
    }
}

// =================================================================
// 🧱 LUXURY UTILITIES  (unchanged)
// =================================================================
@Composable
fun StepSliderInput(title: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, steps: Int, unitLabel: String, onValueChange: (Float) -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
    Text("${value.toInt()} $unitLabel", modifier = Modifier.fillMaxWidth().padding(top = 16.dp), textAlign = TextAlign.Center, fontSize = 32.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace, color = LuxAccentGold)
    Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps, colors = SliderDefaults.colors(thumbColor = LuxAccentGold, activeTrackColor = LuxAccentGold, inactiveTrackColor = Color(0xFF232329), activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent))
}

@Composable
fun StepInput(title: String, label: String, value: String, onValueChange: (String) -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
    Spacer(modifier = Modifier.height(12.dp))
    LuxTextField(value, onValueChange, label)
}

@Composable
fun StepChips(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
    Spacer(modifier = Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(modifier = Modifier.fillMaxWidth().clickable { onSelected(option) }, color = if (isSelected) LuxAccentGold.copy(alpha = 0.1f) else Color.Transparent, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (isSelected) LuxAccentGold else LuxBorderSubtle)) {
                Text(option, modifier = Modifier.padding(16.dp), fontSize = 13.sp, color = if (isSelected) LuxAccentGold else LuxTextPrimary, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun LuxTextField(value: String, onValueChange: (String) -> Unit, label: String, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = LuxTextMuted, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = LuxTextPrimary),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold, unfocusedBorderColor = LuxBorderSubtle, cursorColor = LuxAccentGold, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
    )
}

// =================================================================
// ✅ NEW: REUSABLE CROSSABLE TASK ROW
// =================================================================
@Composable
private fun TaskCheckRow(
    text: String,
    isDone: Boolean,
    onToggle: () -> Unit,
    isMainTask: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.Top
    ) {
        // Custom checkbox
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(if (isMainTask) 20.dp else 16.dp)
                .background(
                    color = if (isDone) LuxAccentGold else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (isDone) LuxAccentGold else LuxTextMuted.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF050507),
                    modifier = Modifier.size(if (isMainTask) 12.dp else 9.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(if (isMainTask) 12.dp else 10.dp))
        Text(
            text = text,
            color = if (isDone) LuxTextMuted else LuxTextPrimary,
            fontSize = if (isMainTask) 14.sp else 13.sp,
            fontWeight = if (isMainTask) FontWeight.Medium else FontWeight.Normal,
            lineHeight = 20.sp,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}
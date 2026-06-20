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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
private val LuxAccentGold = Color(0xFFD4AF37) // Champagne Gold
private val LuxTextPrimary = Color(0xFFF0F0F5)
private val LuxTextMuted = Color(0xFFA2A2AB)
private val LuxBorderSubtle = LuxAccentGold.copy(alpha = 0.15f)

enum class SessionPhase {
    SETUP, COGNITIVE_RADAR, MICRO_COMMITMENT, CHECKPOINT, DEEP_WORK_SETUP, DEEP_WORK, AFFIRMATION_EXIT
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
        SessionPhase.CHECKPOINT -> CheckpointPhase(onContinue = { currentPhase = SessionPhase.DEEP_WORK_SETUP }, onBreak = { currentPhase = SessionPhase.AFFIRMATION_EXIT })
        SessionPhase.DEEP_WORK_SETUP -> DeferredDeepWorkSetupPhase(taskEndGoal, { taskEndGoal = it }, targetTaskDurationMinutes, { targetTaskDurationMinutes = it }, { currentPhase = SessionPhase.DEEP_WORK })
        SessionPhase.DEEP_WORK -> DeepWorkExecutionPhase(taskEndGoal, targetTaskDurationMinutes.toInt(), { currentPhase = SessionPhase.SETUP; viewModel.clearSessionAndGoBack() })
        SessionPhase.AFFIRMATION_EXIT -> AffirmationExitPhase({ currentPhase = SessionPhase.SETUP; viewModel.clearSessionAndGoBack() })
    }
}

// =================================================================
// 📝 PHASE A: GUIDED SETUP (Polished Landing)
// =================================================================
@Composable
fun GuidedSetupPhase(
    exactGoal: String, onExactGoalChange: (String) -> Unit,
    sixtySecondActivity: String, onSixtySecondActivityChange: (String) -> Unit,
    selectedFriction: String, onFrictionChange: (String) -> Unit,
    customFriction: String, onCustomFrictionChange: (String) -> Unit,
    selectedMeaning: String, onMeaningChange: (String) -> Unit,
    customMeaning: String, onCustomMeaningChange: (String) -> Unit,
    cognitiveMinutes: Float, onCognitiveTimeChange: (Float) -> Unit,
    onLaunch: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 4
    var isVisible by remember { mutableStateOf(false) }

    val frictionOptions = listOf("Task Overwhelm", "Perfectionism", "Starting Inertia", "Fear of Failure", "Custom...")
    val meaningOptions = listOf("Action Overrides Anxiety", "Momentum over Perfection", "I Honor My Intentions", "Custom...")

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxBgGradient)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(1200)) + slideInVertically(initialOffsetY = { -20 })) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CALIBRE ZENITH",
                    fontSize = 10.sp,
                    color = LuxTextMuted,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "INITIATION SEQUENCE",
                    fontSize = 14.sp,
                    color = LuxAccentGold,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { step.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = LuxAccentGold,
            trackColor = LuxSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 2 } + fadeIn(tween(400))).togetherWith(slideOutHorizontally { -it / 2 } + fadeOut(tween(400)))
                    } else {
                        (slideInHorizontally { -it / 2 } + fadeIn(tween(400))).togetherWith(slideOutHorizontally { it / 2 } + fadeOut(tween(400)))
                    }
                },
                label = "SetupAnimation"
            ) { currentStep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LuxSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, LuxBorderSubtle)
                ) {
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
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuxBorderSubtle), colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxTextPrimary)
                ) { Text("BACK", letterSpacing = 2.sp, fontSize = 12.sp) }
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
                modifier = Modifier.weight(2f).height(56.dp), shape = RoundedCornerShape(12.dp), enabled = isNextEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold, contentColor = Color(0xFF050507), disabledContainerColor = LuxSurface, disabledContentColor = LuxTextMuted)
            ) { Text(if (step < totalSteps) "NEXT" else "SYSTEM START", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp) }
        }
    }
}

// =================================================================
// 🎮 PHASE B: ACTIVE TACTILE GAME ENGINE (Dynamic Gemini Affirmations)
// =================================================================
@Composable
fun ActiveGameEnginePhase(viewModel: PauseViewModel) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    LaunchedEffect(key1 = viewModel.secondsLeft) {
        if (viewModel.secondsLeft > 0) { delay(1.seconds); viewModel.tickDownOneSecond() }
    }

    val minutes = viewModel.secondsLeft / 60
    val seconds = viewModel.secondsLeft % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    // ✨ Gemini-Backed Marquee Animation Pipeline
    var currentAffirmation by remember { mutableStateOf("ACTION PRECEDES MOMENTUM.") }
    LaunchedEffect(key1 = viewModel.affirmations) {
        var counter = 0
        // Utilize the real-time repository pool or graceful local safety nets
        val activePool = viewModel.affirmations.ifEmpty {
            listOf(
                "ACTION PRECEDES MOMENTUM. YOU DO NOT NEED CLARITY TO BEGIN.",
                "YOUR UNIVERSE HAS SHRUNK TO EXACTLY THIS STEP. THE REST IS OFFLINE.",
                "GIVE YOURSELF ABSOLUTE CLEARANCE TO PRODUCE A CHAOTIC FIRST ATTEMPT.",
                "UNDERSTIMULATION IS TEMPORARY DISCOMFORT. IT CANNOT HOLD YOU BACK.",
                "YOU DO NOT OWE THIS HOUR A FINISHED PROJECT. JUST THIS SEGMENT."
            )
        }

        currentAffirmation = activePool[counter % activePool.size]
        while (true) {
            delay(7000L)
            counter++
            currentAffirmation = activePool[counter % activePool.size]
        }
    }

    val safeCatProfile = CyberCatMatrix.matrix.first()
    var activeGridIndex by remember { mutableIntStateOf((0..8).random()) }
    var lightUpProgressTarget by remember { mutableStateOf(1f) }
    var isFadingOutFast by remember { mutableStateOf(false) }

    val illuminationAlpha by animateFloatAsState(
        targetValue = lightUpProgressTarget,
        animationSpec = tween(durationMillis = if (isFadingOutFast) 100 else 1000),
        finishedListener = { finalAlpha ->
            if (finalAlpha == 0f) {
                isFadingOutFast = false
                activeGridIndex = (0..8).filter { it != activeGridIndex }.random()
                lightUpProgressTarget = 1f
            }
        },
        label = "AlphaAnim"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Visual indicator that optimization parameters are calculating
        if (viewModel.isGeminiOptimizing) {
            Text("OPTIMIZING COGNITIVE INTEGRITY...", color = LuxAccentGold.copy(alpha = 0.7f), letterSpacing = 2.sp, fontSize = 10.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace)
        } else {
            Text("COGNITIVE RADAR ACTIVE", color = LuxAccentGold, letterSpacing = 3.sp, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(timeString, fontSize = 56.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace, color = LuxTextPrimary, letterSpacing = 4.sp)
        Spacer(modifier = Modifier.weight(0.5f))

        AnimatedContent(
            targetState = currentAffirmation,
            transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) },
            label = "AffirmationFade"
        ) { text ->
            Text(
                text = text.uppercase(),
                color = LuxTextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        val density = LocalDensity.current.density
        // Layout bounds expanded dynamically to fully isolate elevation and shadow paths
        Box(modifier = Modifier.graphicsLayer { cameraDistance = 14f * density }.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(9) { index ->
                    val isCurrentTarget = index == activeGridIndex
                    val isFullyLoaded = isCurrentTarget && illuminationAlpha >= 0.99f

                    val popScale by animateFloatAsState(
                        targetValue = if (isFullyLoaded) 1.06f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "ReadyPop"
                    )

                    Box(modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            scaleX = if (isCurrentTarget) popScale else 1f
                            scaleY = if (isCurrentTarget) popScale else 1f
                        }
                        .shadow(
                            elevation = if (isFullyLoaded) 16.dp else 0.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = LuxAccentGold,
                            ambientColor = LuxAccentGold.copy(alpha = 0.5f)
                        )
                        .background(LuxSurface, RoundedCornerShape(18.dp))
                        .border(
                            width = if (isFullyLoaded) 2.dp else 1.dp,
                            color = if (isFullyLoaded) LuxAccentGold else LuxBorderSubtle,
                            shape = RoundedCornerShape(18.dp)
                        )
                    ) {
                        TactileCatPopNode(
                            profile = safeCatProfile.copy(glowColor = LuxAccentGold),
                            isActive = isCurrentTarget && illuminationAlpha > 0.1f,
                            enabled = isFullyLoaded,
                            onTap = {
                                if (isFullyLoaded) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    isFadingOutFast = true
                                    lightUpProgressTarget = 0f
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { viewModel.clearSessionAndGoBack() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF4A1515))
        ) {
            Text("ABORT SEQUENCE", color = Color(0xFFD94A4A), letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// ⏱️ PHASE C: MICRO-COMMITMENT
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
// 🚦 PHASE D: CHECKPOINT
// =================================================================
@Composable
fun CheckpointPhase(onContinue: () -> Unit, onBreak: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("SEAL BROKEN", color = LuxTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Light, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("You survived the first 60 seconds. The hardest part is over. What is your call?", color = LuxTextMuted, textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(56.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(72.dp), colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold), shape = RoundedCornerShape(16.dp)) {
            Text("MOMENTUM ACHIEVED // KEEP GOING", color = Color(0xFF050507), letterSpacing = 1.sp, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onBreak, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent), border = BorderStroke(1.dp, LuxBorderSubtle), shape = RoundedCornerShape(16.dp)) {
            Text("HONOR THE LIMIT // I NEED TO STOP", color = LuxTextMuted, letterSpacing = 1.sp, fontSize = 13.sp)
        }
    }
}

// =================================================================
// ⚙️ PHASE E: DEFERRED DEEP WORK SETUP
// =================================================================
@Composable
fun DeferredDeepWorkSetupPhase(endGoal: String, onEndGoalChange: (String) -> Unit, taskMinutes: Float, onTaskTimeChange: (Float) -> Unit, onLaunchDeepWork: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("MOMENTUM SECURED", color = LuxAccentGold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Since you're already moving, let's define the finish line.", color = LuxTextMuted, textAlign = TextAlign.Center, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(48.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LuxSurface), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, LuxBorderSubtle)) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text("SPECIFIC END GOAL", fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LuxTextField(endGoal, onEndGoalChange, "What does 'done' look like?")
                Spacer(modifier = Modifier.height(40.dp))
                Text("TARGET EXECUTION TIME", fontWeight = FontWeight.SemiBold, color = LuxTextPrimary, fontSize = 11.sp, letterSpacing = 2.sp)
                Text("${taskMinutes.toInt()} MIN", modifier = Modifier.fillMaxWidth().padding(top = 16.dp), textAlign = TextAlign.Center, fontSize = 36.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace, color = LuxAccentGold)
                Slider(value = taskMinutes, onValueChange = onTaskTimeChange, valueRange = 5f..120f, steps = 23, colors = SliderDefaults.colors(thumbColor = LuxAccentGold, activeTrackColor = LuxAccentGold, inactiveTrackColor = LuxSurface, activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent))
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onLaunchDeepWork, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold, disabledContainerColor = LuxSurface), shape = RoundedCornerShape(16.dp), enabled = endGoal.isNotBlank()) {
            Text("ENGAGE DEEP WORK", color = if(endGoal.isNotBlank()) Color(0xFF050507) else LuxTextMuted, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// =================================================================
// ⚡ PHASE F: DEEP WORK
// =================================================================
@Composable
fun DeepWorkExecutionPhase(endGoal: String, durationMinutes: Int, onSessionCompleteOrAborted: () -> Unit) {
    var taskSecondsLeft by remember { mutableIntStateOf(durationMinutes * 60) }
    LaunchedEffect(key1 = taskSecondsLeft) { if (taskSecondsLeft > 0) { delay(1.seconds); taskSecondsLeft-- } }
    val formattedTaskTime = String.format(Locale.getDefault(), "%02d:%02d", taskSecondsLeft / 60, taskSecondsLeft % 60)

    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("DEEP WORK RUNNING", color = LuxAccentGold, letterSpacing = 4.sp, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(40.dp))
        Text(formattedTaskTime, fontSize = 80.sp, fontWeight = FontWeight.ExtraLight, fontFamily = FontFamily.Monospace, color = LuxTextPrimary, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(56.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LuxSurface), border = BorderStroke(1.dp, LuxBorderSubtle), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text("TARGET OUTPUT", fontSize = 10.sp, color = LuxTextMuted, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(endGoal.uppercase(), color = LuxTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, letterSpacing = 1.sp)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
        OutlinedButton(onClick = onSessionCompleteOrAborted, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (taskSecondsLeft == 0) LuxAccentGold else Color(0xFF4A1515))) {
            Text(if (taskSecondsLeft == 0) "COMPLETE SESSION" else "ABORT BLOCK", color = if (taskSecondsLeft == 0) LuxAccentGold else Color(0xFFD94A4A), letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// 🏆 PHASE G: AFFIRMATION EXIT
// =================================================================
@Composable
fun AffirmationExitPhase(onAcknowledge: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(LuxBgGradient).padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("EXECUTIVE DYSFUNCTION\nOVERRIDDEN", color = LuxTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Text("You successfully broke the inertia. You showed up, looked the resistance in the eye, and executed. Rest is productive now.", color = LuxTextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(64.dp))
        Button(onClick = onAcknowledge, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = LuxAccentGold), shape = RoundedCornerShape(16.dp)) {
            Text("ACKNOWLEDGE & DISCONNECT", color = Color(0xFF050507), letterSpacing = 1.sp, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// =================================================================
// 🧱 LUXURY UTILITIES
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
fun LuxTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(label, color = LuxTextMuted, fontSize = 13.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = LuxTextPrimary), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxAccentGold, unfocusedBorderColor = LuxBorderSubtle, cursorColor = LuxAccentGold, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
}
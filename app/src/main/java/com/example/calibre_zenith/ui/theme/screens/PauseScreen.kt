package com.example.calibre_zenith.ui.theme.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.ui.theme.SurfacePressed
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PauseScreen(
    viewModel: PauseViewModel,
    onLaunchMicroStep: (() -> Unit)? = null
) {
    // secondsLeft & affirmations are plain mutableStateOf properties on the ViewModel,
    // not StateFlow - read them directly.
    val secondsLeft = viewModel.secondsLeft
    val affirmations = viewModel.affirmations

    // taskName IS a real StateFlow on the ViewModel, so collectAsState() is correct here.
    val taskName by viewModel.taskName.collectAsState()

    var activeTileIndex by remember { mutableIntStateOf(Random.nextInt(0, 9)) }
    var isFullyRendered by remember { mutableStateOf(false) }
    var currentAffirmation by remember {
        mutableStateOf("Locate the glowing caricature node to ground motor tracking.")
    }
    var affirmationIndex by remember { mutableIntStateOf(0) }

    val systemHaptic = LocalHapticFeedback.current
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    LaunchedEffect(activeTileIndex) {
        isFullyRendered = false
        delay(1900L)
        isFullyRendered = true
    }

    LaunchedEffect(affirmations, secondsLeft) {
        if (affirmations.isNotEmpty() && secondsLeft > 0) {
            while (secondsLeft > 0) {
                val next = affirmations[affirmationIndex % affirmations.size]
                currentAffirmation = next
                affirmationIndex++
                val words = next.trim().split("\\s+".toRegex()).size
                delay(maxOf(2800L, 1200L + words * 220L))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(54.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ZEN COCKPIT // ${taskName.uppercase()}",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(1.dp, SurfacePressed, RoundedCornerShape(20.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentAffirmation,
                transitionSpec = {
                    (fadeIn(tween(600)) + slideInVertically(tween(600)) { height -> height / 4 })
                        .togetherWith(fadeOut(tween(400)))
                },
                label = "AffirmationText"
            ) { text ->
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(1.dp, SurfacePressed, RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in 0 until 3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            TactileCatPopNode(
                                profile = CyberCatMatrix.matrix[index],
                                isActive = index == activeTileIndex,
                                enabled = isFullyRendered && secondsLeft > 0,
                                onTap = {
                                    systemHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 45)
                                    if (index == activeTileIndex) {
                                        activeTileIndex =
                                            (0..8).filter { it != activeTileIndex }.random()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val isButtonEnabled = secondsLeft == 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = if (isButtonEnabled) {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFF00CCA3)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = if (isButtonEnabled) 0.dp else 1.dp,
                        color = SurfacePressed,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable(enabled = isButtonEnabled) {
                        onLaunchMicroStep?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isButtonEnabled) {
                        "LAUNCH INITIAL MICRO-STEP"
                    } else {
                        "BLEEDING RESIDUAL MOTOR ENERGY..."
                    },
                    color = if (isButtonEnabled) Color.Black else MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class CatProfile(
    val emoji: String,
    val label: String
)

object CyberCatMatrix {
    val matrix = listOf(
        CatProfile("🐱", "FOCUS"),
        CatProfile("🐯", "DRIVE"),
        CatProfile("🦁", "POWER"),
        CatProfile("🐻", "CALM"),
        CatProfile("🐼", "FLOW"),
        CatProfile("🦊", "SHARP"),
        CatProfile("🐺", "LOCK"),
        CatProfile("🐸", "ZEN"),
        CatProfile("🦋", "SYNC")
    )
}

@Composable
fun TactileCatPopNode(
    profile: CatProfile,
    isActive: Boolean,
    enabled: Boolean,
    onTap: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "TileScale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.background,
                RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else SurfacePressed,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = profile.emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = profile.label,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
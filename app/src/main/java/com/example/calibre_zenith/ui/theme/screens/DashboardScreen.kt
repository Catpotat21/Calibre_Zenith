package com.example.calibre_zenith.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel

// =================================================================
// 🎨 SHARED LUXURY PALETTE
// =================================================================
private val LuxAccentGold = Color(0xFFD4AF37)
private val LuxDarkBg = Color(0xFF050507)
private val LuxSurface = Color(0xFF141419)

@Composable
fun DashboardScreen(viewModel: PauseViewModel) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxDarkBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "CALIBRE ZENITH",
            color = LuxAccentGold,
            fontSize = 28.sp,
            letterSpacing = 6.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SELECT OPERATIONAL WORKSPACE",
            color = Color.Gray.copy(alpha = 0.7f),
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        // --- INTERFACE HUB ACTIONS ---

        DashboardButton(
            text = "PLANNER CANVAS",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.navigateToPlanner()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DashboardButton(
            text = "COGNITIVE TIMER",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.startManualSession()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DashboardButton(
            text = "TACTICAL ROADMAP",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.currentScreen = "Roadmap"
            },
            isSecondary = true
        )
    }
}

@Composable
fun DashboardButton(
    text: String,
    onClick: () -> Unit,
    isSecondary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) LuxSurface else LuxAccentGold,
            contentColor = if (isSecondary) LuxAccentGold else Color.Black
        ),
        border = if (isSecondary) androidx.compose.foundation.BorderStroke(1.dp, LuxAccentGold.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

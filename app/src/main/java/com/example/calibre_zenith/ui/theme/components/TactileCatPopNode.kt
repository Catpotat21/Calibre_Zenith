package com.example.calibre_zenith.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calibre_zenith.data.CatProfile
import com.example.calibre_zenith.ui.theme.SurfacePressed

@Composable
fun TactileCatPopNode(
    profile: CatProfile,
    isActive: Boolean,
    enabled: Boolean,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedOffsetY by animateDpAsState(targetValue = if (isPressed && enabled) 0.dp else (-5).dp, label = "Sink")
    val animatedTileColor by animateColorAsState(targetValue = if (isActive) profile.wakeColor else SurfacePressed, animationSpec = tween(1900), label = "Tile")
    val animatedBaseColor by animateColorAsState(targetValue = if (isActive) profile.sleepColor else Color(0xFF101016), animationSpec = tween(1900), label = "Base")
    val nodeScale by animateFloatAsState(targetValue = if (isPressed && enabled) 0.95f else 1.0f, label = "Scale")

    Box(
        modifier = Modifier
            .size(86.dp)
            .scale(nodeScale)
            .background(color = animatedBaseColor, shape = RoundedCornerShape(18.dp))
            // Accessibility Semantics Injection
            .clearAndSetSemantics {
                contentDescription = "Tactile grid entry. ${profile.breed} node. Status is ${if (isActive) "Glowing Active" else "Dormant Locked"}."
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = animatedOffsetY)
                .background(color = animatedTileColor, shape = RoundedCornerShape(18.dp))
                .border(width = 1.dp, color = if (isActive) profile.glowColor.copy(alpha = 0.6f) else Color.Transparent, shape = RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onTap),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CatCaricatureVectorDraw(id = profile.id, strokeColor = if (isActive) Color.Black else MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isActive) profile.breed.uppercase() else "STILL",
                    color = if (isActive) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSecondary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
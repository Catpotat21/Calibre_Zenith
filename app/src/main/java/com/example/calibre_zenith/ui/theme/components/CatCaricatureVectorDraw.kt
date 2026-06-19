package com.example.calibre_zenith.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CatCaricatureVectorDraw(id: String, strokeColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val headPath = Path().apply {
            moveTo(w * 0.2f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.2f)
            lineTo(w * 0.4f, h * 0.38f)
            lineTo(w * 0.6f, h * 0.38f)
            lineTo(w * 0.85f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.75f)
            quadraticTo(w * 0.5f, h * 0.95f, w * 0.2f, h * 0.75f)
            close()
        }
        drawPath(path = headPath, color = strokeColor, style = Stroke(width = 3.5f))

        // Universal Whiskers & Nose setup
        drawPath(path = Path().apply {
            moveTo(w * 0.47f, h * 0.65f)
            lineTo(w * 0.53f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.69f)
            close()
        }, color = strokeColor)

        drawLine(strokeColor, Offset(w * 0.5f, h * 0.69f), Offset(w * 0.46f, h * 0.74f), strokeWidth = 2.5f)
        drawLine(strokeColor, Offset(w * 0.5f, h * 0.69f), Offset(w * 0.54f, h * 0.74f), strokeWidth = 2.5f)

        when (id) {
            "calico" -> {
                drawCircle(strokeColor, radius = 3f, center = Offset(w * 0.34f, h * 0.53f))
                drawCircle(strokeColor, radius = 3f, center = Offset(w * 0.66f, h * 0.53f))
            }
            "tuxedo" -> {
                drawLine(strokeColor, Offset(w * 0.28f, h * 0.51f), Offset(w * 0.4f, h * 0.55f), strokeWidth = 3.5f)
                drawLine(strokeColor, Offset(w * 0.72f, h * 0.51f), Offset(w * 0.6f, h * 0.55f), strokeWidth = 3.5f)
            }
            else -> {
                drawCircle(strokeColor, radius = 4f, center = Offset(w * 0.35f, h * 0.54f))
                drawCircle(strokeColor, radius = 4f, center = Offset(w * 0.65f, h * 0.54f))
            }
        }
    }
}
package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.EyeExpression
import kotlin.math.sin

@Composable
fun PixelArtEyes(
    color: Color,
    expression: EyeExpression,
    isGenerating: Boolean,
    isTargeted: Boolean,
    content: String? = null,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 68.dp
) {
    // Dynamic sentiment / topic extraction from model content
    val lowerContent = remember(content) { content?.lowercase() ?: "" }
    val isCriticalContent = remember(lowerContent) {
        lowerContent.contains("risk") || lowerContent.contains("vulnerability") ||
                lowerContent.contains("fallacy") || lowerContent.contains("flaw") || lowerContent.contains("attack")
    }
    val isLateralContent = remember(lowerContent) {
        lowerContent.contains("paradigm") || lowerContent.contains("novel") ||
                lowerContent.contains("quantum") || lowerContent.contains("invert") || lowerContent.contains("breakthrough")
    }
    val isDataContent = remember(lowerContent) {
        lowerContent.contains("topology") || lowerContent.contains("data") ||
                lowerContent.contains("benchmark") || lowerContent.contains("invariant") || lowerContent.contains("metric")
    }
    val isHumanContent = remember(lowerContent) {
        lowerContent.contains("human") || lowerContent.contains("ergonomic") ||
                lowerContent.contains("resonance") || lowerContent.contains("friction") || lowerContent.contains("fatigue")
    }

    // High-speed generation pulse & saccade when model is producing content
    val infiniteTransition = rememberInfiniteTransition(label = "eye_anim")
    val saccadeTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isGenerating) 450 else 2400, easing = LinearEasing)
        ),
        label = "saccade"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isGenerating) 300 else 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Periodic blink animation
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3800
                1f at 0
                1f at 3500
                0.05f at 3650
                1f at 3800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Scanline animation for analytical / data content
    val scanlineCol by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Outer aura when generating, targeted, or model text is active
        if (isGenerating || isTargeted || !content.isNullOrBlank()) {
            val auraRadius = if (isGenerating) w * 0.48f * glowAlpha else w * 0.44f
            val auraColor = when {
                isTargeted -> color.copy(alpha = 0.45f)
                isGenerating -> color.copy(alpha = 0.25f * glowAlpha)
                else -> color.copy(alpha = 0.12f)
            }
            drawCircle(
                color = auraColor,
                radius = auraRadius,
                center = Offset(w / 2f, h / 2f)
            )
        }

        // Calculate dynamic eyelid vertical height based on content
        val squintFactor = when {
            isCriticalContent || expression == EyeExpression.SKEPTICAL -> 0.65f
            isLateralContent || expression == EyeExpression.CURIOUS -> 1.15f
            else -> 1.0f
        }

        val eyeWidth = w * 0.36f
        val eyeHeight = (h * 0.28f * squintFactor * blinkProgress).coerceAtLeast(2f)
        val eyeY = (h - eyeHeight) / 2f
        val leftEyeX = w * 0.10f
        val rightEyeX = w * 0.54f

        val pixelCols = 6
        val pixelRows = 4
        val pixelW = eyeWidth / pixelCols
        val pixelH = (eyeHeight / pixelRows).coerceAtLeast(1.5f)

        // Saccade horizontal pupil shift based on content stream
        val pupilOffsetCol = if (isGenerating) {
            if (sin(saccadeTime.toDouble()) > 0.3) 1 else 0
        } else if (content != null) {
            (content.length % 2)
        } else {
            0
        }

        // Draw Left and Right Pixel Eyes
        listOf(leftEyeX, rightEyeX).forEachIndexed { eyeIndex, startX ->
            for (row in 0 until pixelRows) {
                for (col in 0 until pixelCols) {
                    val px = startX + col * pixelW
                    val py = eyeY + row * pixelH

                    val isBorder = row == 0 || row == pixelRows - 1 || col == 0 || col == pixelCols - 1

                    // Dynamic Pupil Pixel calculation reacting to model output
                    val isPupil = when {
                        isDataContent || expression == EyeExpression.ANALYTICAL -> {
                            // Moving scanline pupil
                            val activeScanCol = scanlineCol.toInt().coerceIn(1, 4)
                            row in 1..2 && col == activeScanCol
                        }
                        isCriticalContent || expression == EyeExpression.SKEPTICAL -> {
                            // Thin focused horizontal pupil slit
                            row == 1 && (col == 2 + pupilOffsetCol || col == 3)
                        }
                        isLateralContent || expression == EyeExpression.CURIOUS -> {
                            // Dilated tall pupil
                            (row == 1 || row == 2) && (col in 2..4)
                        }
                        isHumanContent || expression == EyeExpression.EMPATHIC -> {
                            // Soft centered rounded pupil
                            row in 1..2 && col in (2 + pupilOffsetCol)..(3 + pupilOffsetCol).coerceAtMost(4)
                        }
                        expression == EyeExpression.QUANTUM -> {
                            // High-entropy alternating checker
                            (row + col + (saccadeTime * 2).toInt()) % 2 == 0 && row in 1..2 && col in 1..4
                        }
                        else -> {
                            // Standard expressive pupil with saccade shift
                            row in 1..2 && (col == (2 + pupilOffsetCol).coerceIn(1, 4) || col == (3 + pupilOffsetCol).coerceIn(1, 4))
                        }
                    }

                    if (isBorder) {
                        drawRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(px, py),
                            size = Size(pixelW - 0.5f, pixelH - 0.5f)
                        )
                    } else if (isPupil) {
                        val pupilColor = when {
                            isGenerating -> color.copy(alpha = glowAlpha)
                            isTargeted -> color
                            else -> color.copy(alpha = 0.9f)
                        }
                        drawRect(
                            color = pupilColor,
                            topLeft = Offset(px, py),
                            size = Size(pixelW - 0.5f, pixelH - 0.5f)
                        )
                        // Specular highlight spark
                        if (row == 1 && col == 2) {
                            drawRect(
                                color = Color.White.copy(alpha = if (isGenerating) 0.95f else 0.75f),
                                topLeft = Offset(px + pixelW * 0.2f, py + pixelH * 0.2f),
                                size = Size(pixelW * 0.5f, pixelH * 0.5f)
                            )
                        }
                    } else {
                        // Sclera
                        drawRect(
                            color = Color(0xFF070B14),
                            topLeft = Offset(px, py),
                            size = Size(pixelW - 0.5f, pixelH - 0.5f)
                        )
                    }
                }
            }
        }
    }
}

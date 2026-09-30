package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ConcentricTarget(
    color: Color,
    isTargeted: Boolean,
    hasContributedThisRound: Boolean,
    personaName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "target_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_pulse"
    )

    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "reticle_rot"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("target_${personaName.lowercase().replace(" ", "_")}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = sizeDp / 2),
                role = Role.Button,
                onClickLabel = "Directly address $personaName",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width * 0.42f

            // Outer ring
            val outerRadius = if (isTargeted) maxRadius * pulseScale else maxRadius
            val ringAlpha = if (isTargeted) 0.95f else if (hasContributedThisRound) 0.35f else 0.7f

            drawCircle(
                color = color.copy(alpha = ringAlpha),
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isTargeted) 2.2f else 1.2f)
            )

            // Middle ring
            drawCircle(
                color = color.copy(alpha = ringAlpha * 0.75f),
                radius = outerRadius * 0.65f,
                center = center,
                style = Stroke(width = 1.0f)
            )

            // Center target bullseye pip
            drawCircle(
                color = if (isTargeted) color else color.copy(alpha = 0.85f),
                radius = if (isTargeted) 5f else 3.5f,
                center = center
            )

            // Crosshair ticks
            val tickLength = 4f
            listOf(
                Offset(center.x - outerRadius, center.y) to Offset(center.x - outerRadius + tickLength, center.y),
                Offset(center.x + outerRadius - tickLength, center.y) to Offset(center.x + outerRadius, center.y),
                Offset(center.x, center.y - outerRadius) to Offset(center.x, center.y - outerRadius + tickLength),
                Offset(center.x, center.y + outerRadius - tickLength) to Offset(center.x, center.y + outerRadius)
            ).forEach { (start, end) ->
                drawLine(
                    color = color.copy(alpha = ringAlpha),
                    start = start,
                    end = end,
                    strokeWidth = 1.2f
                )
            }
        }
    }
}

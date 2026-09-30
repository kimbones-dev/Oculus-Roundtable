package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Persona
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.OculusSurface

@Composable
fun ThoughtBubble(
    persona: Persona,
    text: String?,
    isGenerating: Boolean,
    onFocus: () -> Unit,
    onAppendToCanvas: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (text.isNullOrBlank() && !isGenerating) {
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubble_pulse")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = modifier
            .widthIn(min = 110.dp, max = 165.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), spotColor = persona.color)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.4.dp,
                color = if (isGenerating) persona.color.copy(alpha = borderGlow) else persona.color.copy(alpha = 0.75f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(OculusSurface.copy(alpha = 0.95f))
            .clickable(
                onClick = onFocus,
                onClickLabel = "Expand ${persona.name}'s thought"
            )
            .testTag("thought_bubble_${persona.id}"),
        colors = CardDefaults.cardColors(containerColor = OculusSurface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = persona.name,
                    color = persona.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.8.dp,
                        color = persona.color
                    )
                } else if (!text.isNullOrBlank()) {
                    IconButton(
                        onClick = { onAppendToCanvas(text) },
                        modifier = Modifier
                            .size(22.dp)
                            .testTag("append_${persona.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Append to canvas",
                            tint = persona.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            if (isGenerating) {
                Text(
                    text = "Synthesizing vector...",
                    color = MatrixGreenDim,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2
                )
            } else if (!text.isNullOrBlank()) {
                Text(
                    text = text,
                    color = MatrixGreen,
                    fontSize = 11.5.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

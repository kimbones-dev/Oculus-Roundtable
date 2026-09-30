package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Persona
import com.example.ui.theme.*

@Composable
fun RoundProgressBar(
    currentRound: Int,
    totalRounds: Int,
    activePersonas: List<Persona>,
    contributedFlags: Map<String, Boolean>,
    generatingIds: Set<String>,
    directTargetId: String?,
    onNodeClick: (Persona) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalNodes = activePersonas.size
    val contributedCount = activePersonas.count { contributedFlags[it.id] == true }
    val isThresholdMet = activePersonas.isNotEmpty() && contributedCount == totalNodes
    val progressFraction = if (totalNodes > 0) contributedCount.toFloat() / totalNodes.toFloat() else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_gen")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        color = OculusSurfaceVariant.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, OculusBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("round_progress_bar")
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Header Row: Large round number & Threshold state
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ROUND $currentRound",
                        color = MatrixGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = " OF $totalRounds",
                        color = MatrixGreenDim,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isThresholdMet) MatrixGreen.copy(alpha = 0.2f) else OculusSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isThresholdMet) MatrixGreen else OculusBorder
                    )
                ) {
                    Text(
                        text = if (isThresholdMet) "★ THRESHOLD SATISFIED" else "$contributedCount/$totalNodes CONTRIBUTIONS",
                        color = if (isThresholdMet) MatrixGreen else MatrixGreenDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Progress Track
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MatrixGreen,
                trackColor = Color(0xFF131A29),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Node Pips Row: Visual ease of node progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Moderator User Node Pip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = UserNodeCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UserNodeCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(UserNodeCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOU",
                            color = UserNodeCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // AI Node Pips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activePersonas.forEach { persona ->
                        val hasContributed = contributedFlags[persona.id] == true
                        val isGenerating = generatingIds.contains(persona.id)
                        val isDirectTarget = directTargetId == persona.id

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasContributed -> MatrixGreen.copy(alpha = 0.25f)
                                        isGenerating -> persona.color.copy(alpha = 0.35f * pulseAlpha)
                                        else -> OculusSurface
                                    }
                                )
                                .border(
                                    width = if (isDirectTarget) 2.5.dp else 1.2.dp,
                                    color = when {
                                        isDirectTarget -> Color.White
                                        hasContributed -> MatrixGreen
                                        isGenerating -> persona.color
                                        else -> persona.color.copy(alpha = 0.6f)
                                    },
                                    shape = CircleShape
                                )
                                .clickable { onNodeClick(persona) }
                                .testTag("progress_pip_${persona.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasContributed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "${persona.name} contributed",
                                    tint = MatrixGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isDirectTarget) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "Targeted",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = persona.name.take(2).uppercase(),
                                    color = persona.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

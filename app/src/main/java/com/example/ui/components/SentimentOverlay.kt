package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.domain.model.SentimentTelemetry
import com.example.ui.theme.*

@Composable
fun SentimentOverlay(
    telemetry: SentimentTelemetry,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MatrixGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("sentiment_overlay_panel"),
        color = OculusSurface.copy(alpha = 0.90f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            // Streamlined Ticker Bar with Large Monospace Matrix Green
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (telemetry.consensusScore > 0.7f) MatrixGreen
                                else if (telemetry.tensionScore > 0.6f) TelemetryCritical
                                else TelemetryWarning
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TELEMETRY",
                        color = MatrixGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = telemetry.dominantPolarity,
                        color = MatrixGreenDim,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CON: ${(telemetry.consensusScore * 100).toInt()}%",
                        color = MatrixGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "TEN: ${(telemetry.tensionScore * 100).toInt()}%",
                        color = if (telemetry.tensionScore > 0.6f) TelemetryCritical else MatrixGreenDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${telemetry.dialecticVelocity} wpm",
                        color = MatrixGreenDim,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Expanded Recent Sentiment Shifts
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = MatrixGreen.copy(alpha = 0.3f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (telemetry.recentLogEntries.isEmpty()) {
                        Text(
                            text = "Awaiting initial node dialectic signals...",
                            color = MatrixGreenDim,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        telemetry.recentLogEntries.take(4).forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${entry.personaName} • ${entry.toneLabel}",
                                    color = MatrixGreen,
                                    fontSize = 12.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.2f", entry.polarityScore),
                                    color = if (entry.polarityScore > 0.6f) MatrixGreen else TelemetryCritical,
                                    fontSize = 12.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

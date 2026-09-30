package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.Persona
import com.example.ui.theme.*

@Composable
fun RosterManagementDialog(
    currentRound: Int,
    activeRoster: List<Persona>,
    stagedRoster: List<Persona>,
    allAvailablePersonas: List<Persona>,
    onSaveStagedRoster: (List<Persona>) -> Unit,
    onOpenPersonaCreator: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIds by remember { mutableStateOf(stagedRoster.map { it.id }.toSet()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, MatrixGreen, RoundedCornerShape(20.dp))
                .testTag("roster_management_dialog"),
            color = OculusBackground.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ROSTER ORCHESTRATION",
                                color = MatrixGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Round #$currentRound: Locked • Edits take effect in Round #${currentRound + 1}",
                                color = MatrixGreenDim,
                                fontSize = 13.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User / Moderator Node Status Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OculusSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UserNodeCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(UserNodeCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MODERATOR NODE (YOU)",
                                color = UserNodeCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Permanent anchor of the roundtable. Controls turn resolution & synthesis.",
                                color = MatrixGreenDim,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = UserNodeCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ANCHOR",
                                color = UserNodeCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Roster Count Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI NODES: ${selectedIds.size}/7 (MAX 7)",
                        color = if (selectedIds.size in 1..7) MatrixGreen else TelemetryCritical,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = onOpenPersonaCreator,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OculusSurfaceVariant,
                            contentColor = MatrixGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("create_custom_node_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Create Custom Node", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Available Personas List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allAvailablePersonas) { persona ->
                        val isSelected = selectedIds.contains(persona.id)
                        val isLockedInCurrentRound = activeRoster.any { it.id == persona.id }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OculusSurfaceVariant else OculusSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) persona.color else OculusBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        // Allow deselecting if at least 1 remains
                                        if (selectedIds.size > 1) {
                                            selectedIds = selectedIds - persona.id
                                        }
                                    } else {
                                        // Max 7
                                        if (selectedIds.size < 7) {
                                            selectedIds = selectedIds + persona.id
                                        }
                                    }
                                }
                                .testTag("roster_item_${persona.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox icon
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) persona.color else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Colored indicator pip
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(persona.color)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = persona.name,
                                            color = persona.color,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${persona.role})",
                                            color = MatrixGreen,
                                            fontSize = 13.sp
                                        )
                                        if (persona.isCustom) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = persona.color.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "CUSTOM",
                                                    color = persona.color,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Lens: ${persona.defaultLens}",
                                        color = MatrixGreenDim,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }

                                if (isLockedInCurrentRound) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = "Active in R#$currentRound",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OculusBorder)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            val newStagedPersonas = allAvailablePersonas.filter { selectedIds.contains(it.id) }
                            onSaveStagedRoster(newStagedPersonas)
                            onDismiss()
                        },
                        enabled = selectedIds.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("apply_roster_changes_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixGreen,
                            contentColor = OculusBackground
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stage Roster for R#${currentRound + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

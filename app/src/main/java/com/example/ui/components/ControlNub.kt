package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.entity.CanvasSnapshotEntity
import com.example.data.local.entity.SharedFrameEntity
import com.example.domain.model.Persona
import com.example.ui.theme.*

@Composable
fun ControlNub(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    currentRound: Int,
    totalRounds: Int,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    onQuickSnapshot: () -> Unit,
    onExportMarkdown: () -> Unit,
    onExportJson: () -> Unit,
    snapshots: List<CanvasSnapshotEntity>,
    onRestoreSnapshot: (CanvasSnapshotEntity) -> Unit,
    sharedFrames: List<SharedFrameEntity>,
    onSelectFrame: (SharedFrameEntity) -> Unit,
    activeRoster: List<Persona>,
    stagedRoster: List<Persona>,
    onOpenRosterManager: () -> Unit,
    onOpenPersonaCreator: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Session", "Roster", "Snapshots", "Frames", "Export")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Collapsible Handle Nub (Always Visible at bottom)
        Surface(
            modifier = Modifier
                .width(260.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .border(1.5.dp, MatrixGreen, RoundedCornerShape(21.dp))
                .clickable(
                    onClick = onToggleExpand,
                    onClickLabel = if (isExpanded) "Collapse Control Nub" else "Expand Control Nub"
                )
                .testTag("control_nub_handle"),
            color = OculusSurface.copy(alpha = 0.96f),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Draggable indicator bar
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MatrixGreen.copy(alpha = 0.7f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ROUND $currentRound/$totalRounds",
                        color = MatrixGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = MatrixGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Branch Icon
                IconButton(
                    onClick = onQuickSnapshot,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("nub_quick_branch_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ForkRight,
                        contentDescription = "Quick Snapshot",
                        tint = MatrixGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Expanded Panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.2.dp, MatrixGreen, RoundedCornerShape(20.dp))
                    .testTag("control_nub_expanded_panel"),
                color = OculusSurface.copy(alpha = 0.98f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Navigation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = MatrixGreen,
                        edgePadding = 0.dp,
                        divider = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (selectedTab == index) MatrixGreen else MatrixGreenDim
                                    )
                                },
                                modifier = Modifier.testTag("nub_tab_$title")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> SessionSettingsTab(
                                currentRound = currentRound,
                                totalRounds = totalRounds,
                                temperature = temperature,
                                onTemperatureChange = onTemperatureChange,
                                onQuickSnapshot = onQuickSnapshot
                            )
                            1 -> RosterTab(
                                currentRound = currentRound,
                                activeRoster = activeRoster,
                                stagedRoster = stagedRoster,
                                onOpenRosterManager = onOpenRosterManager,
                                onOpenPersonaCreator = onOpenPersonaCreator
                            )
                            2 -> SnapshotsTab(
                                snapshots = snapshots,
                                onQuickSnapshot = onQuickSnapshot,
                                onRestoreSnapshot = onRestoreSnapshot
                            )
                            3 -> FramesTab(
                                sharedFrames = sharedFrames,
                                onSelectFrame = onSelectFrame
                            )
                            4 -> ExportTab(
                                onExportMarkdown = onExportMarkdown,
                                onExportJson = onExportJson
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionSettingsTab(
    currentRound: Int,
    totalRounds: Int,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    onQuickSnapshot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "LIVE SESSION PARAMETERS",
            color = MatrixGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = OculusBackground,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Baseline Temperature", color = MatrixGreen, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        String.format("%.2f", temperature),
                        color = MatrixGreen,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0.1f..1.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = MatrixGreen,
                        activeTrackColor = MatrixGreen,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.testTag("temperature_slider")
                )
                Text(
                    text = if (temperature < 0.45f) "Rigid analytical scrutiny" else if (temperature > 0.85f) "High lateral divergence" else "Balanced synthesis matrix",
                    color = MatrixGreenDim,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onQuickSnapshot,
            colors = ButtonDefaults.buttonColors(containerColor = OculusSurfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("branch_snapshot_panel_btn")
        ) {
            Icon(Icons.Default.ForkRight, contentDescription = null, modifier = Modifier.size(18.dp), tint = MatrixGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Branch Snapshot of Canvas", fontSize = 14.sp, color = MatrixGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RosterTab(
    currentRound: Int,
    activeRoster: List<Persona>,
    stagedRoster: List<Persona>,
    onOpenRosterManager: () -> Unit,
    onOpenPersonaCreator: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Status notice
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OculusBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ROSTER STATUS: ROUND #$currentRound (LOCKED)",
                    color = MatrixGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Active: ${activeRoster.size}/7 Nodes (+ You). Roster edits may be made anytime and take effect in Round #${currentRound + 1}.",
                    color = MatrixGreenDim,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenRosterManager,
                colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen, contentColor = OculusBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("manage_roster_btn")
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Manage Roster", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenPersonaCreator,
                border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MatrixGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("guided_persona_creator_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Node", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Nodes in Current Round
        Text(
            text = "LOCKED NODES (ROUND #$currentRound):",
            color = MatrixGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        activeRoster.forEach { persona ->
            Surface(
                color = OculusBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(persona.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${persona.name} (${persona.role})",
                        color = persona.color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = persona.defaultLens.take(24),
                        color = MatrixGreenDim,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SnapshotsTab(
    snapshots: List<CanvasSnapshotEntity>,
    onQuickSnapshot: () -> Unit,
    onRestoreSnapshot: (CanvasSnapshotEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SNAPSHOT BRANCHES (${snapshots.size})",
                color = MatrixGreen,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onQuickSnapshot, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Snapshot", tint = MatrixGreen, modifier = Modifier.size(20.dp))
            }
        }

        if (snapshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No canvas snapshot branches saved yet.", color = MatrixGreenDim, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(snapshots) { snap ->
                    Surface(
                        color = OculusBackground,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = snap.branchTag,
                                    color = MatrixGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Round #${snap.roundNumber} • ${snap.content.take(50)}...",
                                    color = MatrixGreenDim,
                                    fontSize = 12.sp
                                )
                            }
                            TextButton(onClick = { onRestoreSnapshot(snap) }) {
                                Text("Restore", fontSize = 13.sp, color = MatrixGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FramesTab(
    sharedFrames: List<SharedFrameEntity>,
    onSelectFrame: (SharedFrameEntity) -> Unit
) {
    if (sharedFrames.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No shared frames committed yet.\nComplete Round 1 to synthesize the first frame.",
                color = MatrixGreenDim,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sharedFrames) { frame ->
                Surface(
                    color = OculusBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectFrame(frame) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Shared Frame — Round #${frame.roundNumber}",
                            color = MatrixGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = frame.frameContent.take(110) + "...",
                            color = MatrixGreenDim,
                            fontSize = 13.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportTab(
    onExportMarkdown: () -> Unit,
    onExportJson: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "SESSION DATA EXPORT",
            color = MatrixGreen,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Export the complete session transcript, sequential Updated Shared Frames, and sentiment analysis telemetry via Android Share Intent.",
            color = MatrixGreenDim,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Button(
            onClick = onExportMarkdown,
            colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen, contentColor = OculusBackground),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("export_markdown_btn")
        ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Full Session (Markdown)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        OutlinedButton(
            onClick = onExportJson,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MatrixGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("export_json_btn")
        ) {
            Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Structured Data (JSON)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

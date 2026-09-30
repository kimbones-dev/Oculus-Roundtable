package com.example.ui.roundtable

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.EyeExpression
import com.example.domain.model.Persona
import com.example.domain.model.RoundState
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.RoundtableViewModel
import com.example.util.ExportHelper

@Composable
fun RoundtableScreen(
    viewModel: RoundtableViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by viewModel.currentSession.collectAsState()
    val roundState by viewModel.roundState.collectAsState()
    val activeRoster by viewModel.activeRoster.collectAsState()
    val stagedRoster by viewModel.stagedRoster.collectAsState()
    val allAvailablePersonas by viewModel.allAvailablePersonas.collectAsState()
    val generatingPersonas by viewModel.generatingPersonas.collectAsState()
    val latestThoughts by viewModel.latestThoughtByPersona.collectAsState()
    val telemetry by viewModel.sentimentTelemetry.collectAsState()
    val canvasContent by viewModel.centralCanvasContent.collectAsState()
    val snapshots by viewModel.snapshots.collectAsState()
    val sharedFrames by viewModel.sharedFrames.collectAsState()
    val directTarget by viewModel.directTargetPersona.collectAsState()
    val focusedTranscript by viewModel.focusedTranscript.collectAsState()
    val isControlNubExpanded by viewModel.isControlNubExpanded.collectAsState()
    val baselineTemperature by viewModel.baselineTemperature.collectAsState()

    var promptInput by remember { mutableStateOf("") }
    var showSnapshotTagPrompt by remember { mutableStateOf(false) }
    var customSnapshotTag by remember { mutableStateOf("") }
    var showRosterManager by remember { mutableStateOf(false) }
    var showPersonaCreator by remember { mutableStateOf(false) }

    val activeFlags = (roundState as? RoundState.Active)?.flags ?: emptyMap()
    val isThresholdMet = (roundState as? RoundState.Active)?.isMinimumThresholdMet ?: false
    val completedCount = (roundState as? RoundState.Active)?.completedCount ?: 0
    val totalActiveNodes = activeRoster.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OculusBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background Cyber-Grid Canvas Lines
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0F172A).copy(alpha = 0.4f),
                                OculusBackground
                            ),
                            radius = 900f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 54.dp) // space for Control Nub
            ) {
                // 1. TOP HEADER & STREAMLINED OVERLAY
                Surface(
                    color = OculusSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.4f))
                ) {
                    Column {
                        // Title bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "THE OCULUS ROUNDTABLE",
                                    color = MatrixGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isThresholdMet) MatrixGreen.copy(alpha = 0.2f) else OculusSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isThresholdMet) MatrixGreen else MatrixGreenDim.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = if (isThresholdMet) "THRESHOLD MET: ELASTIC" else "$completedCount/$totalActiveNodes NODES",
                                        color = if (isThresholdMet) MatrixGreen else MatrixGreenDim,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Roster Quick Action
                            IconButton(
                                onClick = { showRosterManager = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("top_roster_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = "Manage Roster",
                                    tint = MatrixGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Streamlined Sentiment Analysis Overlay
                        SentimentOverlay(telemetry = telemetry)

                        // 2. VISUAL EASE OF PROGRESS BAR & ACTIVE NODE PIPS
                        RoundProgressBar(
                            currentRound = session?.currentRound ?: 1,
                            totalRounds = session?.totalRounds ?: 10,
                            activePersonas = activeRoster,
                            contributedFlags = activeFlags,
                            generatingIds = generatingPersonas,
                            directTargetId = directTarget?.id,
                            onNodeClick = { persona ->
                                viewModel.setDirectTarget(persona)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. THE AI NODES (MIDGROUND ARC) WITH CONTENT-ANIMATED EYES
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Moderator Node (You) in the Arc
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(100.dp)
                                .testTag("node_user_moderator")
                        ) {
                            Box(
                                modifier = Modifier.height(58.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = UserNodeCyan.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, UserNodeCyan)
                                ) {
                                    Text(
                                        text = "MODERATOR",
                                        color = UserNodeCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            PixelArtEyes(
                                color = UserNodeCyan,
                                expression = EyeExpression.SYNTHESIS,
                                isGenerating = false,
                                isTargeted = false,
                                content = "Operator Anchor",
                                sizeDp = 52.dp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            ConcentricTarget(
                                color = UserNodeCyan,
                                isTargeted = false,
                                hasContributedThisRound = true,
                                personaName = "You",
                                onClick = {
                                    Toast.makeText(context, "You are the session anchor.", Toast.LENGTH_SHORT).show()
                                },
                                sizeDp = 44.dp
                            )

                            Text(
                                text = "You (Mod)",
                                color = UserNodeCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Orchestrator",
                                color = MatrixGreenDim,
                                fontSize = 11.sp
                            )
                        }

                        // AI Nodes from current active roster (1 to 7)
                        activeRoster.forEach { persona ->
                            val isGenerating = generatingPersonas.contains(persona.id)
                            val isTargeted = directTarget?.id == persona.id
                            val hasContributed = activeFlags[persona.id] == true
                            val thought = latestThoughts[persona.id]

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(114.dp)
                                    .testTag("node_${persona.id}")
                            ) {
                                // Thought Bubble above eyes
                                Box(
                                    modifier = Modifier
                                        .height(58.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    ThoughtBubble(
                                        persona = persona,
                                        text = thought?.content,
                                        isGenerating = isGenerating,
                                        onFocus = {
                                            thought?.let { viewModel.setFocusedTranscript(it) }
                                        },
                                        onAppendToCanvas = { text ->
                                            viewModel.appendToCanvas("[${persona.name} (${persona.role})]:\n$text")
                                            Toast.makeText(context, "Appended to Central Canvas", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Pixel Art Expressive Eyes with CONTENT-REACTIVE ANIMATION!
                                PixelArtEyes(
                                    color = persona.color,
                                    expression = persona.defaultExpression,
                                    isGenerating = isGenerating,
                                    isTargeted = isTargeted,
                                    content = thought?.content,
                                    sizeDp = 54.dp,
                                    modifier = Modifier.clickable {
                                        viewModel.setDirectTarget(persona)
                                    }
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Concentric Target Beneath Eyes
                                ConcentricTarget(
                                    color = persona.color,
                                    isTargeted = isTargeted,
                                    hasContributedThisRound = hasContributed,
                                    personaName = persona.name,
                                    onClick = {
                                        viewModel.setDirectTarget(persona)
                                    },
                                    sizeDp = 44.dp
                                )

                                // Label & Role Badge with Large Fonts
                                Text(
                                    text = persona.name,
                                    color = if (isTargeted) persona.color else MatrixGreen,
                                    fontSize = 13.sp,
                                    fontWeight = if (isTargeted) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = if (hasContributed) "✓ Contributed" else "○ Pending",
                                    color = if (hasContributed) MatrixGreen else MatrixGreenDim.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. THE CENTRAL CANVAS (FLOATING IN CENTER OF ARC)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                ) {
                    CentralCanvas(
                        content = canvasContent,
                        onContentChange = { viewModel.updateCanvas(it) },
                        onQuickSnapshot = { showSnapshotTagPrompt = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 5. ELASTIC ROUND CONTROLS & PROMPT INJECTION BAR
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    // Turn Resolution Bar (Revealed when minimum threshold of active flags is met)
                    AnimatedVisibility(
                        visible = isThresholdMet,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, MatrixGreen, RoundedCornerShape(12.dp)),
                            color = OculusSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MatrixGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "THRESHOLD MET ($totalActiveNodes/$totalActiveNodes NODES)",
                                            color = MatrixGreen,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Round is elastic: continue prompt injection or call the turn.",
                                            color = MatrixGreenDim,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.callTheTurn() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MatrixGreen,
                                        contentColor = OculusBackground
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("call_the_turn_btn")
                                ) {
                                    Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call the Turn", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Direct Target Notice (if a node is targeted via DirectAddressEvent)
                    if (directTarget != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = directTarget!!.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TARGETING: ${directTarget!!.name.uppercase()} (${directTarget!!.role})",
                                    color = directTarget!!.color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            TextButton(
                                onClick = { viewModel.setDirectTarget(directTarget!!) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Cancel Target", color = MatrixGreenDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Moderator Input Bar with Large Fonts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = {
                                Text(
                                    text = if (directTarget != null) "Address ${directTarget!!.name} directly..." else "Inject prompt to active nodes...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
                                color = MatrixGreen,
                                fontFamily = FontFamily.Monospace
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = directTarget?.color ?: MatrixGreen,
                                unfocusedBorderColor = OculusBorder,
                                focusedTextColor = MatrixGreen,
                                unfocusedTextColor = MatrixGreen,
                                focusedContainerColor = OculusSurface,
                                unfocusedContainerColor = OculusSurface
                            ),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("moderator_prompt_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val text = promptInput.trim()
                                promptInput = ""
                                viewModel.sendModeratorPrompt(text.ifBlank { "Advance dialectic on session topic." })
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(directTarget?.color ?: MatrixGreen)
                                .testTag("send_prompt_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Prompt",
                                tint = OculusBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 6. THE CONTROL NUB (BOTTOM CENTER)
            ControlNub(
                isExpanded = isControlNubExpanded,
                onToggleExpand = { viewModel.toggleControlNub() },
                currentRound = session?.currentRound ?: 1,
                totalRounds = session?.totalRounds ?: 10,
                temperature = baselineTemperature,
                onTemperatureChange = { viewModel.setTemperature(it) },
                onQuickSnapshot = { showSnapshotTagPrompt = true },
                onExportMarkdown = {
                    session?.let { s ->
                        ExportHelper.exportSessionAsMarkdown(
                            context = context,
                            session = s,
                            transcripts = viewModel.transcriptsForCurrentRound.value,
                            sharedFrames = sharedFrames,
                            snapshots = snapshots,
                            sentiment = telemetry,
                            canvasContent = canvasContent
                        )
                    }
                },
                onExportJson = {
                    session?.let { s ->
                        ExportHelper.exportSessionAsJson(
                            context = context,
                            session = s,
                            transcripts = viewModel.transcriptsForCurrentRound.value,
                            sharedFrames = sharedFrames,
                            snapshots = snapshots,
                            sentiment = telemetry,
                            canvasContent = canvasContent
                        )
                    }
                },
                snapshots = snapshots,
                onRestoreSnapshot = { snap ->
                    viewModel.restoreSnapshot(snap)
                    Toast.makeText(context, "Restored '${snap.branchTag}' to Canvas", Toast.LENGTH_SHORT).show()
                },
                sharedFrames = sharedFrames,
                onSelectFrame = { frame ->
                    viewModel.appendToCanvas("\n\n### Frame #${frame.roundNumber} Reference\n${frame.frameContent}")
                    Toast.makeText(context, "Appended Frame #${frame.roundNumber} to Canvas", Toast.LENGTH_SHORT).show()
                },
                activeRoster = activeRoster,
                stagedRoster = stagedRoster,
                onOpenRosterManager = { showRosterManager = true },
                onOpenPersonaCreator = { showPersonaCreator = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 7. ROSTER MANAGEMENT DIALOG
            if (showRosterManager) {
                RosterManagementDialog(
                    currentRound = session?.currentRound ?: 1,
                    activeRoster = activeRoster,
                    stagedRoster = stagedRoster,
                    allAvailablePersonas = allAvailablePersonas,
                    onSaveStagedRoster = { newStaged ->
                        viewModel.updateStagedRoster(newStaged)
                        Toast.makeText(context, "Staged roster updated for Round #${(session?.currentRound ?: 1) + 1}", Toast.LENGTH_SHORT).show()
                    },
                    onOpenPersonaCreator = {
                        showRosterManager = false
                        showPersonaCreator = true
                    },
                    onDismiss = { showRosterManager = false }
                )
            }

            // 8. GUIDED CUSTOM PERSONA CREATOR DIALOG
            if (showPersonaCreator) {
                GuidedPersonaCreatorDialog(
                    onDismiss = { showPersonaCreator = false },
                    onSavePersona = { newPersona ->
                        viewModel.saveCustomPersona(newPersona)
                        Toast.makeText(context, "Created '${newPersona.name}' and added to staged roster", Toast.LENGTH_SHORT).show()
                        showPersonaCreator = false
                    }
                )
            }

            // 9. THOUGHT FOCUS MODAL (When a thought bubble is tapped)
            focusedTranscript?.let { transcript ->
                val persona = allAvailablePersonas.find { it.id == transcript.personaId } ?: Persona.DEFAULTS.first()
                ThoughtFocusModal(
                    persona = persona,
                    thoughtText = transcript.content,
                    sentimentLabel = transcript.sentimentLabel,
                    sentimentScore = transcript.sentimentScore,
                    isDirectAddress = transcript.isDirectAddress,
                    onAppendToCanvas = { text ->
                        viewModel.appendToCanvas("[${persona.name}]:\n$text")
                        Toast.makeText(context, "Appended to Canvas", Toast.LENGTH_SHORT).show()
                    },
                    onDirectAddress = { p ->
                        viewModel.setDirectTarget(p)
                    },
                    onDismiss = { viewModel.setFocusedTranscript(null) }
                )
            }

            // 10. CHECKPOINT SCREEN: UPDATED SHARED FRAME
            val currentState = roundState
            if (currentState is RoundState.Checkpoint) {
                UpdatedSharedFrameDialog(
                    roundNumber = currentState.roundNumber,
                    proposedFrame = currentState.proposedFrame,
                    onCommit = { editedSummary, moderatorNotes ->
                        viewModel.commitSharedFrame(editedSummary, moderatorNotes)
                        Toast.makeText(context, "Committed Updated Shared Frame for Round #${currentState.roundNumber + 1}", Toast.LENGTH_LONG).show()
                    },
                    onCancel = { viewModel.cancelTurnResolution() }
                )
            }

            // 11. RESOLVING SPINNER OVERLAY
            if (currentState is RoundState.Resolving) {
                Dialog(onDismissRequest = {}) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, MatrixGreen, RoundedCornerShape(16.dp)),
                        color = OculusBackground.copy(alpha = 0.98f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MatrixGreen,
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "STATE REDUCTION IN PROGRESS",
                                color = MatrixGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.statusMessage,
                                color = MatrixGreenDim,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 12. SNAPSHOT BRANCH TAG PROMPT DIALOG
            if (showSnapshotTagPrompt) {
                AlertDialog(
                    onDismissRequest = { showSnapshotTagPrompt = false },
                    title = {
                        Text("Snapshot Branching", color = MatrixGreen, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    },
                    text = {
                        Column {
                            Text(
                                "Quick-save the current Central Canvas state to create a non-destructive branch point.",
                                color = MatrixGreenDim,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = customSnapshotTag,
                                onValueChange = { customSnapshotTag = it },
                                placeholder = { Text("e.g., Branch: Decoupled Cache Paradigm", color = Color.Gray, fontSize = 14.sp) },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MatrixGreen),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MatrixGreen,
                                    unfocusedBorderColor = OculusBorder,
                                    focusedTextColor = MatrixGreen,
                                    unfocusedTextColor = MatrixGreen
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.quickSnapshotBranch(customSnapshotTag.ifBlank { null })
                                customSnapshotTag = ""
                                showSnapshotTagPrompt = false
                                Toast.makeText(context, "Canvas Snapshot Branch Saved", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen, contentColor = OculusBackground)
                        ) {
                            Text("Save Snapshot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSnapshotTagPrompt = false }) {
                            Text("Cancel", color = Color.Gray, fontSize = 14.sp)
                        }
                    },
                    containerColor = OculusSurface,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

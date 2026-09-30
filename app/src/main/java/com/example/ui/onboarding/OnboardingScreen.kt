package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.example.domain.model.EyeExpression
import com.example.domain.model.Persona
import com.example.ui.components.GuidedPersonaCreatorDialog
import com.example.ui.components.PixelArtEyes
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingMessage(
    val id: String,
    val sender: String,
    val text: String,
    val isBot: Boolean,
    val options: List<String> = emptyList(),
    val isRosterStep: Boolean = false,
    val isFinalStep: Boolean = false
)

@Composable
fun OnboardingScreen(
    onComplete: (domain: String, format: String, rigor: String, temperature: Float, selectedRosterIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var userDomain by remember { mutableStateOf("Distributed Systems & AI Architecture") }
    var userFormat by remember { mutableStateOf("Modular PRD & Synthesis Matrix") }
    var userRigor by remember { mutableStateOf("Adversarial Stress-Testing") }
    var userTemp by remember { mutableFloatStateOf(0.70f) }

    var availablePersonas by remember { mutableStateOf(Persona.DEFAULTS) }
    var selectedRosterIds by remember { mutableStateOf(Persona.DEFAULTS.map { it.id }.toSet()) }
    var showCustomCreator by remember { mutableStateOf(false) }

    var step by remember { mutableIntStateOf(0) }
    var customInputText by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            OnboardingMessage(
                id = "m0",
                sender = "Oculus Core Prime",
                text = "Welcome to The Oculus Roundtable. I am Core Prime, your session conductor.\n\nBefore launching the deliberation, we must calibrate your session parameters.\n\nWhat is your primary domain or objective for this deliberation?",
                isBot = true,
                options = listOf(
                    "Distributed Systems & AI Architecture",
                    "Autonomous Multi-Agent Robotics",
                    "Strategic Venture Ideation",
                    "Philosophy & Cognitive Ethics"
                )
            )
        )
    }

    fun proceedToNextStep(selectedOption: String) {
        when (step) {
            0 -> {
                userDomain = selectedOption
                messages.add(OnboardingMessage(id = "u0", sender = "Moderator", text = selectedOption, isBot = false))
                step = 1
                messages.add(
                    OnboardingMessage(
                        id = "m1",
                        sender = "Oculus Core Prime",
                        text = "Calibrating domain vectors for [$selectedOption].\n\nNext, what is your preferred output format for Central Canvas synthesis?",
                        isBot = true,
                        options = listOf(
                            "Modular PRD & Synthesis Matrix",
                            "Executive Decision Tree & Spec",
                            "Architecture Topology & Code Vectors",
                            "Comprehensive Philosophical Brief"
                        )
                    )
                )
            }
            1 -> {
                userFormat = selectedOption
                messages.add(OnboardingMessage(id = "u1", sender = "Moderator", text = selectedOption, isBot = false))
                step = 2
                messages.add(
                    OnboardingMessage(
                        id = "m2",
                        sender = "Oculus Core Prime",
                        text = "Recorded output target: [$selectedOption].\n\nWhat dialectic workflow protocol and rigor level shall we enforce across the nodes?",
                        isBot = true,
                        options = listOf(
                            "Adversarial Stress-Testing (Devil's Advocate Weighted)",
                            "Consensus Acceleration (Synthesizer Weighted)",
                            "Oblique Lateral Exploration (High Entropy Purple)",
                            "Balanced Multi-Vector Equilibrium"
                        )
                    )
                )
            }
            2 -> {
                userRigor = selectedOption
                userTemp = when {
                    selectedOption.contains("Adversarial") -> 0.60f
                    selectedOption.contains("Consensus") -> 0.70f
                    selectedOption.contains("Oblique") -> 0.90f
                    else -> 0.75f
                }
                messages.add(OnboardingMessage(id = "u2", sender = "Moderator", text = selectedOption, isBot = false))
                step = 3
                messages.add(
                    OnboardingMessage(
                        id = "m3",
                        sender = "Oculus Core Prime",
                        text = "Now, configure your Starting Roster.\n\nYou may select any 1 to 7 AI nodes below, plus yourself as Moderator. Not every node must be filled. You can swap, add, or create custom nodes at any time (changes activate in the next round):",
                        isBot = true,
                        isRosterStep = true
                    )
                )
            }
            3 -> {
                step = 4
                messages.add(
                    OnboardingMessage(
                        id = "m4",
                        sender = "Oculus Core Prime",
                        text = """
Diagnostic calibration complete!
• Domain: $userDomain
• Output Spec: $userFormat
• Protocol: $userRigor
• Baseline Temperature: ${String.format("%.2f", userTemp)}
• Starting Roster: ${selectedRosterIds.size} AI Nodes (+ Moderator)

All roles are locked for Round #1. Ready to initialize the Roundtable?
""".trimIndent(),
                        isBot = true,
                        isFinalStep = true
                    )
                )
            }
        }
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OculusBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with Core Prime Eye
            Surface(
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, OculusBorder),
                color = OculusSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PixelArtEyes(
                            color = MatrixGreen,
                            expression = EyeExpression.SYNTHESIS,
                            isGenerating = true,
                            isTargeted = false,
                            content = "Calibrating starting roster and dialectic vectors",
                            sizeDp = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "THE OCULUS ROUNDTABLE",
                                color = MatrixGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Core Prime Diagnostic Conductor",
                                color = MatrixGreenDim,
                                fontSize = 13.sp
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            onComplete(userDomain, userFormat, userRigor, userTemp, selectedRosterIds.toList())
                        },
                        modifier = Modifier.testTag("skip_onboarding_btn")
                    ) {
                        Text("Skip Setup", color = MatrixGreenDim, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chat Message Stream with Large Fonts
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isBot) Alignment.Start else Alignment.End
                    ) {
                        // Sender label
                        Text(
                            text = msg.sender.uppercase(),
                            color = if (msg.isBot) MatrixGreen else MatrixGreenDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )

                        // Bubble
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isBot) 4.dp else 16.dp,
                                bottomEnd = if (msg.isBot) 16.dp else 4.dp
                            ),
                            color = if (msg.isBot) OculusSurface else OculusSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                if (msg.isBot) MatrixGreen.copy(alpha = 0.5f) else MatrixGreen
                            ),
                            modifier = Modifier.widthIn(max = 350.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = MatrixGreen,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        // Option Pills for Bot Messages
                        if (msg.isBot && msg.options.isNotEmpty() && step < 3) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                msg.options.forEach { opt ->
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = OculusSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, MatrixGreen.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .clickable { proceedToNextStep(opt) }
                                            .testTag("onboard_option_${opt.take(10).replace(" ", "_")}")
                                    ) {
                                        Text(
                                            text = "› $opt",
                                            color = MatrixGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Starting Roster Selection Step (1 to 7 nodes, plus user)
                        if (msg.isRosterStep && step == 3) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OculusSurface,
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, MatrixGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "STARTING ROSTER: ${selectedRosterIds.size}/7 NODES",
                                            color = MatrixGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        TextButton(onClick = { showCustomCreator = true }) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ Create Custom", color = MatrixGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Roster options
                                    availablePersonas.forEach { p ->
                                        val isSelected = selectedRosterIds.contains(p.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) OculusSurfaceVariant else Color.Transparent)
                                                .clickable {
                                                    selectedRosterIds = if (isSelected) {
                                                        if (selectedRosterIds.size > 1) selectedRosterIds - p.id else selectedRosterIds
                                                    } else {
                                                        if (selectedRosterIds.size < 7) selectedRosterIds + p.id else selectedRosterIds
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isSelected) p.color else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(p.color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${p.name} (${p.role})",
                                                color = p.color,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = p.defaultLens.take(20),
                                                color = MatrixGreenDim,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { proceedToNextStep("Roster Configured (${selectedRosterIds.size} nodes)") },
                                        enabled = selectedRosterIds.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MatrixGreen,
                                            contentColor = OculusBackground
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Lock Starting Roster (${selectedRosterIds.size}/7 Nodes)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Final Step Action Button
                        if (msg.isFinalStep) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    onComplete(userDomain, userFormat, userRigor, userTemp, selectedRosterIds.toList())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MatrixGreen,
                                    contentColor = OculusBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("initialize_roundtable_btn")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "INITIALIZE THE OCULUS ROUNDTABLE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Custom Text Input if user wants custom answers
            if (step < 3) {
                Surface(
                    color = OculusSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OculusBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customInputText,
                            onValueChange = { customInputText = it },
                            placeholder = { Text("Or specify custom preference...", color = Color.Gray, fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MatrixGreen,
                                unfocusedBorderColor = OculusBorder,
                                focusedTextColor = MatrixGreen,
                                unfocusedTextColor = MatrixGreen
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboarding_custom_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (customInputText.isNotBlank()) {
                                    val text = customInputText.trim()
                                    customInputText = ""
                                    proceedToNextStep(text)
                                }
                            },
                            enabled = customInputText.isNotBlank(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (customInputText.isNotBlank()) MatrixGreen else Color(0xFF1E293B))
                                .testTag("onboarding_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (customInputText.isNotBlank()) OculusBackground else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Guided Custom Persona Creator inside Onboarding
        if (showCustomCreator) {
            GuidedPersonaCreatorDialog(
                onDismiss = { showCustomCreator = false },
                onSavePersona = { custom ->
                    availablePersonas = availablePersonas + custom
                    if (selectedRosterIds.size < 7) {
                        selectedRosterIds = selectedRosterIds + custom.id
                    }
                    showCustomCreator = false
                }
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.EyeExpression
import com.example.domain.model.Persona
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun GuidedPersonaCreatorDialog(
    onDismiss: () -> Unit,
    onSavePersona: (Persona) -> Unit
) {
    var selectedPreset by remember { mutableStateOf<Persona?>(null) }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#00FF66") }
    var systemPrompt by remember { mutableStateOf("") }
    var defaultLens by remember { mutableStateOf("") }
    var selectedExpression by remember { mutableStateOf(EyeExpression.SYNTHESIS) }

    val colorOptions = listOf(
        "#00FF66", // Matrix Green
        "#00E5FF", // Cyan
        "#D500F9", // Neon Purple
        "#FF1744", // Crimson
        "#FFAB00", // Amber
        "#2979FF", // Cobalt
        "#FF4081", // Rose
        "#76FF03", // Electric Lime
        "#FFD700", // Gold
        "#CFD8DC"  // Silver
    )

    fun applyPreset(p: Persona) {
        selectedPreset = p
        name = p.name
        role = p.role
        selectedColorHex = p.colorHex
        systemPrompt = p.systemPrompt
        defaultLens = p.defaultLens
        selectedExpression = p.defaultExpression
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, MatrixGreen, RoundedCornerShape(20.dp))
                .testTag("persona_creator_dialog"),
            color = OculusBackground.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GUIDED NODE CREATION",
                                color = MatrixGreen,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Construct a custom cognitive persona with specialized skillset",
                                color = MatrixGreenDim,
                                fontSize = 13.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Quick-Load Tray
                Text(
                    text = "ARCHETYPE PRESETS (OPTIONAL TEMPLATES)",
                    color = MatrixGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Persona.PRESET_TEMPLATES) { preset ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedPreset?.id == preset.id) OculusSurfaceVariant else OculusSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedPreset?.id == preset.id) preset.color else OculusBorder
                            ),
                            modifier = Modifier
                                .clickable { applyPreset(preset) }
                                .testTag("preset_${preset.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(preset.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = preset.name,
                                        color = preset.color,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = preset.role,
                                        color = MatrixGreenDim,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Card
                val activePreviewColor = Persona.parseHexColor(selectedColorHex, MatrixGreen)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OculusSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, activePreviewColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (name.isBlank()) "UNNAMED NODE" else name.uppercase(),
                                color = activePreviewColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (role.isBlank()) "Specialized Cognitive Role" else role,
                                color = MatrixGreen,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (defaultLens.isBlank()) "Lens: Unspecified Skillset" else "Lens: $defaultLens",
                                color = MatrixGreenDim,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Animated Eye Preview
                        PixelArtEyes(
                            color = activePreviewColor,
                            expression = selectedExpression,
                            isGenerating = false,
                            isTargeted = true,
                            sizeDp = 58.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Node Name & Role Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Node Name", fontSize = 14.sp) },
                        placeholder = { Text("e.g., Turing", fontSize = 14.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatrixGreen,
                            unfocusedBorderColor = OculusBorder,
                            focusedTextColor = MatrixGreen,
                            unfocusedTextColor = MatrixGreen,
                            focusedLabelColor = MatrixGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("persona_name_input")
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role / Title", fontSize = 14.sp) },
                        placeholder = { Text("e.g., Cryptanalyst", fontSize = 14.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatrixGreen,
                            unfocusedBorderColor = OculusBorder,
                            focusedTextColor = MatrixGreen,
                            unfocusedTextColor = MatrixGreen,
                            focusedLabelColor = MatrixGreen
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("persona_role_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Signature Hex Color Picker
                Text(
                    text = "SIGNATURE CHROMATIC IDENTIFIER",
                    color = MatrixGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorOptions.forEach { hex ->
                        val parsed = Persona.parseHexColor(hex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (selectedColorHex == hex) 3.dp else 1.dp,
                                    color = if (selectedColorHex == hex) Color.White else OculusBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                                .testTag("color_picker_$hex")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Default Skillset Lens
                OutlinedTextField(
                    value = defaultLens,
                    onValueChange = { defaultLens = it },
                    label = { Text("Skillset / Evaluation Lens", fontSize = 14.sp) },
                    placeholder = { Text("e.g., Zero-Knowledge Cryptography & Trust Invariants", fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatrixGreen,
                        unfocusedBorderColor = OculusBorder,
                        focusedTextColor = MatrixGreen,
                        unfocusedTextColor = MatrixGreen,
                        focusedLabelColor = MatrixGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("persona_lens_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // System Prompt / Cognitive Mandate
                Text(
                    text = "COGNITIVE SYSTEM PROMPT & INVARIANTS",
                    color = MatrixGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    placeholder = {
                        Text(
                            "Define this node's thought processes, biases, and dialectic rules.\ne.g., 'You are a relentless security auditor. You evaluate everything through adversarial attack trees and failure state cascading.'",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("persona_prompt_input"),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MatrixGreen,
                        lineHeight = 18.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatrixGreen,
                        unfocusedBorderColor = OculusBorder,
                        focusedContainerColor = OculusSurface,
                        unfocusedContainerColor = OculusSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Eye Expression Style
                Text(
                    text = "OCULAR EXPRESSION ENGINE",
                    color = MatrixGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EyeExpression.entries) { expr ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedExpression == expr) OculusSurfaceVariant else OculusSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedExpression == expr) MatrixGreen else OculusBorder
                            ),
                            modifier = Modifier
                                .clickable { selectedExpression = expr }
                                .testTag("expression_${expr.name}")
                        ) {
                            Text(
                                text = expr.name,
                                color = if (selectedExpression == expr) MatrixGreen else Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            val finalName = name.ifBlank { "Custom Node" }
                            val finalRole = role.ifBlank { "Specialized Analyst" }
                            val newPersona = Persona(
                                id = "custom_${UUID.randomUUID().toString().take(8)}",
                                name = finalName,
                                role = finalRole,
                                colorHex = selectedColorHex,
                                color = Persona.parseHexColor(selectedColorHex),
                                systemPrompt = systemPrompt.ifBlank { "You are $finalName, the $finalRole. Analyze all topics with maximum rigor." },
                                defaultLens = defaultLens.ifBlank { "Specialized Analytical Lens" },
                                defaultExpression = selectedExpression,
                                isCustom = true
                            )
                            onSavePersona(newPersona)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_custom_persona_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixGreen,
                            contentColor = OculusBackground
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Add to Roster",
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

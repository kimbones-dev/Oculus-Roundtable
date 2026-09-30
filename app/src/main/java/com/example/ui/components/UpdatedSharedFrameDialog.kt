package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
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
import com.example.ui.theme.*

@Composable
fun UpdatedSharedFrameDialog(
    roundNumber: Int,
    proposedFrame: String,
    onCommit: (editedSummary: String, moderatorNotes: String) -> Unit,
    onCancel: () -> Unit
) {
    var editedContent by remember(proposedFrame) { mutableStateOf(proposedFrame) }
    var moderatorNotes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(2.dp, MatrixGreen, RoundedCornerShape(22.dp))
                .testTag("checkpoint_dialog"),
            color = OculusBackground.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CHECKPOINT: ROUND #$roundNumber",
                                color = MatrixGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "State Reduction: Updated Shared Frame",
                                color = MatrixGreenDim,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MatrixGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen)
                    ) {
                        Text(
                            text = "Synthesized",
                            color = MatrixGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Review, edit, or append to the synthesized frame below. Upon clicking 'Commit Frame', this will be saved to Room and become the immutable system context for all active Personas for Round #${roundNumber + 1}.",
                    color = MatrixGreen,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Editable Frame Content
                Text(
                    text = "EDITABLE SHARED FRAME SUMMARY (MARKDOWN)",
                    color = MatrixGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .testTag("frame_summary_input"),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.5.sp,
                        color = MatrixGreen,
                        lineHeight = 21.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatrixGreen,
                        unfocusedBorderColor = OculusBorder,
                        focusedContainerColor = OculusSurface,
                        unfocusedContainerColor = OculusSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Moderator Notes
                Text(
                    text = "MODERATOR DIRECTIVES (FOR ROUND #${roundNumber + 1})",
                    color = MatrixGreenDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = moderatorNotes,
                    onValueChange = { moderatorNotes = it },
                    placeholder = {
                        Text("Add specific instructions or focus constraints for the next round...", color = Color.Gray, fontSize = 13.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("moderator_notes_input"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MatrixGreen),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatrixGreen,
                        unfocusedBorderColor = OculusBorder,
                        focusedContainerColor = OculusSurface,
                        unfocusedContainerColor = OculusSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("frame_cancel_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OculusBorder)
                    ) {
                        Text("Return to Round", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { onCommit(editedContent, moderatorNotes) },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp)
                            .testTag("frame_commit_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixGreen,
                            contentColor = OculusBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Commit & Advance to Round #${roundNumber + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

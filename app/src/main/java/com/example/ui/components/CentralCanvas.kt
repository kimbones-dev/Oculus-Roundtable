package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CentralCanvas(
    content: String,
    onContentChange: (String) -> Unit,
    onQuickSnapshot: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPreviewMode by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.2.dp, MatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .testTag("central_canvas_container"),
        color = OculusSurface.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Canvas Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MatrixGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CENTRAL CANVAS",
                        color = MatrixGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Editor / Markdown Preview Toggle
                    FilledTonalButton(
                        onClick = { isPreviewMode = !isPreviewMode },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("canvas_mode_toggle"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isPreviewMode) MatrixGreen.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            contentColor = if (isPreviewMode) MatrixGreen else MatrixGreenDim
                        )
                    ) {
                        Icon(
                            imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.RemoveRedEye,
                            contentDescription = if (isPreviewMode) "Switch to Editor" else "Switch to Preview",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPreviewMode) "Edit" else "Preview",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Snapshot Branch Quick Action
                    IconButton(
                        onClick = onQuickSnapshot,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("canvas_snapshot_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ForkRight,
                            contentDescription = "Snapshot Branch",
                            tint = MatrixGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Clear Canvas Action
                    IconButton(
                        onClick = { onContentChange("") },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("canvas_clear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Canvas",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MatrixGreen.copy(alpha = 0.4f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Editor Area or Live Markdown Preview Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isPreviewMode) {
                    // Live Markdown Renderer with Large Fonts & Matrix Green Styling
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(end = 6.dp)
                            .testTag("canvas_markdown_preview")
                    ) {
                        if (content.isBlank()) {
                            Text(
                                text = "Canvas is empty. Type in Editor mode or tap thought bubbles to append ideas.",
                                color = MatrixGreenDim,
                                fontSize = 15.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            RenderSimpleMarkdown(content)
                        }
                    }
                } else {
                    // Interactive Translucent Text Editor with Large Monospace Matrix Green
                    BasicTextField(
                        value = content,
                        onValueChange = onContentChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("canvas_text_input"),
                        textStyle = TextStyle(
                            color = MatrixGreen,
                            fontSize = 15.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 23.sp
                        ),
                        cursorBrush = SolidColor(MatrixGreen),
                        decorationBox = { innerTextField ->
                            if (content.isEmpty()) {
                                Text(
                                    text = "Central Canvas: Live working spec & synthesis buffer.\n• Tap a thought bubble to append its insight directly.\n• Type manually here anytime without interrupting AI generation.",
                                    color = MatrixGreenDim.copy(alpha = 0.6f),
                                    fontSize = 14.5.sp,
                                    lineHeight = 21.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RenderSimpleMarkdown(markdown: String) {
    markdown.lines().forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> {
                Text(
                    text = trimmed.removePrefix("# "),
                    color = MatrixGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            trimmed.startsWith("## ") -> {
                Text(
                    text = trimmed.removePrefix("## "),
                    color = Color(0xFF67E8F9),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            trimmed.startsWith("### ") -> {
                Text(
                    text = trimmed.removePrefix("### "),
                    color = Color(0xFFA5F3FC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                Row(
                    modifier = Modifier.padding(start = 8.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "› ", color = MatrixGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = trimmed.substring(2),
                        color = MatrixGreen,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }
            trimmed.startsWith("```") -> {
                Surface(
                    color = Color(0xFF070B14),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "--- Code Vector Block ---",
                        color = MatrixGreenDim,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            trimmed.isBlank() -> {
                Spacer(modifier = Modifier.height(8.dp))
            }
            else -> {
                Text(
                    text = trimmed,
                    color = MatrixGreen,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

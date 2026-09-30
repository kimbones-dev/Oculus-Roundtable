package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.local.entity.CanvasSnapshotEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.local.entity.SharedFrameEntity
import com.example.data.local.entity.TranscriptEntity
import com.example.domain.model.SentimentTelemetry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun exportSessionAsMarkdown(
        context: Context,
        session: SessionEntity,
        transcripts: List<TranscriptEntity>,
        sharedFrames: List<SharedFrameEntity>,
        snapshots: List<CanvasSnapshotEntity>,
        sentiment: SentimentTelemetry,
        canvasContent: String
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val md = buildString {
            append("# The Oculus Roundtable — Session Report\n\n")
            append("**Session ID:** `${session.id}`  \n")
            append("**Topic:** ${session.topic}  \n")
            append("**Domain Focus:** ${session.domainFocus}  \n")
            append("**Workflow Rigor:** ${session.workflowRigor}  \n")
            append("**Output Format:** ${session.outputFormat}  \n")
            append("**Baseline Temperature:** ${session.baselineTemperature}  \n")
            append("**Date:** ${dateFormat.format(Date(session.createdAt))}  \n")
            append("**Rounds Progress:** Round ${session.currentRound} of ${session.totalRounds}  \n\n")

            append("## Engagement & Sentiment Telemetry\n\n")
            append("* **Consensus Convergence:** ${(sentiment.consensusScore * 100).toInt()}%  \n")
            append("* **Dialectic Tension Index:** ${(sentiment.tensionScore * 100).toInt()}%  \n")
            append("* **Dialectic Velocity:** ${sentiment.dialecticVelocity} wpm  \n")
            append("* **Dominant Polarity:** ${sentiment.dominantPolarity}  \n\n")

            append("## Central Canvas Final Output\n\n")
            append("```markdown\n")
            append(canvasContent.ifBlank { "(Canvas is empty)" })
            append("\n```\n\n")

            if (snapshots.isNotEmpty()) {
                append("## Canvas Snapshot Branching History\n\n")
                snapshots.forEach { snap ->
                    append("### ${snap.branchTag} (Round #${snap.roundNumber})\n")
                    append("*Captured:* ${dateFormat.format(Date(snap.timestamp))}\n\n")
                    append("```\n${snap.content}\n```\n\n")
                }
            }

            append("## Sequential Updated Shared Frames (Synthesis Checkpoints)\n\n")
            if (sharedFrames.isEmpty()) {
                append("_No frames committed yet._\n\n")
            } else {
                sharedFrames.forEach { frame ->
                    append("### Shared Frame — Round #${frame.roundNumber}\n")
                    append("*Committed At:* ${dateFormat.format(Date(frame.committedAt))}\n\n")
                    append(frame.frameContent)
                    if (frame.moderatorNotes.isNotBlank()) {
                        append("\n\n> **Moderator Annotations:** ${frame.moderatorNotes}\n")
                    }
                    append("\n\n---\n\n")
                }
            }

            append("## Full Dialectic Round Transcripts\n\n")
            val roundsGrouped = transcripts.groupBy { it.roundNumber }
            roundsGrouped.forEach { (roundNum, roundTranscripts) ->
                append("### --- Round #$roundNum ---\n\n")
                roundTranscripts.forEach { t ->
                    val directMark = if (t.isDirectAddress) " 🎯 [DIRECTLY ADDRESSED]" else ""
                    val toneBadge = " [${t.sentimentLabel}]"
                    append("#### ${t.personaName}$directMark$toneBadge\n")
                    append("*${dateFormat.format(Date(t.timestamp))}*\n\n")
                    append(t.content)
                    append("\n\n")
                }
            }
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "oculus_session_${session.currentRound}_$timestamp.md")
        file.writeText(md)

        shareFile(context, file, "text/markdown", "Export Oculus Roundtable Session (Markdown)")
    }

    fun exportSessionAsJson(
        context: Context,
        session: SessionEntity,
        transcripts: List<TranscriptEntity>,
        sharedFrames: List<SharedFrameEntity>,
        snapshots: List<CanvasSnapshotEntity>,
        sentiment: SentimentTelemetry,
        canvasContent: String
    ) {
        val root = JSONObject().apply {
            put("sessionId", session.id)
            put("title", session.title)
            put("topic", session.topic)
            put("domainFocus", session.domainFocus)
            put("workflowRigor", session.workflowRigor)
            put("currentRound", session.currentRound)
            put("totalRounds", session.totalRounds)
            put("createdAt", session.createdAt)
            put("canvasContent", canvasContent)

            put("telemetry", JSONObject().apply {
                put("consensusScore", sentiment.consensusScore)
                put("tensionScore", sentiment.tensionScore)
                put("velocity", sentiment.dialecticVelocity)
                put("dominantPolarity", sentiment.dominantPolarity)
            })

            put("sharedFrames", JSONArray().apply {
                sharedFrames.forEach { f ->
                    put(JSONObject().apply {
                        put("roundNumber", f.roundNumber)
                        put("frameContent", f.frameContent)
                        put("moderatorNotes", f.moderatorNotes)
                        put("committedAt", f.committedAt)
                    })
                }
            })

            put("canvasSnapshots", JSONArray().apply {
                snapshots.forEach { s ->
                    put(JSONObject().apply {
                        put("roundNumber", s.roundNumber)
                        put("branchTag", s.branchTag)
                        put("content", s.content)
                        put("timestamp", s.timestamp)
                    })
                }
            })

            put("transcripts", JSONArray().apply {
                transcripts.forEach { t ->
                    put(JSONObject().apply {
                        put("roundNumber", t.roundNumber)
                        put("personaId", t.personaId)
                        put("personaName", t.personaName)
                        put("content", t.content)
                        put("sentimentLabel", t.sentimentLabel)
                        put("sentimentScore", t.sentimentScore)
                        put("isDirectAddress", t.isDirectAddress)
                        put("timestamp", t.timestamp)
                    })
                }
            })
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "oculus_session_$timestamp.json")
        file.writeText(root.toString(2))

        shareFile(context, file, "application/json", "Export Oculus Roundtable Session (JSON)")
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportHelper", "Failed to share file: ${e.message}", e)
        }
    }
}

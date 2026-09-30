package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val topic: String,
    val createdAt: Long = System.currentTimeMillis(),
    val currentRound: Int = 1,
    val totalRounds: Int = 10,
    val isCompleted: Boolean = false,
    val baselineTemperature: Float = 0.7f,
    val domainFocus: String = "Multi-Disciplinary Synthesis",
    val outputFormat: String = "Structured Dialectic Spec",
    val workflowRigor: String = "Adversarial Consensus",
    val activeRosterIds: String = "teal_synthesizer,crimson_devils_advocate,purple_lateral_thinker,amber_pragmatist,cobalt_architect,rose_empathetic_observer,silver_archivist",
    val stagedRosterIds: String = "teal_synthesizer,crimson_devils_advocate,purple_lateral_thinker,amber_pragmatist,cobalt_architect,rose_empathetic_observer,silver_archivist"
)

@Entity(
    tableName = "rounds",
    indices = [Index(value = ["sessionId", "roundNumber"], unique = true)]
)
data class RoundEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val roundNumber: Int,
    val status: String, // ACTIVE, RESOLVING, COMMITTED
    val startedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val rosterSnapshotIds: String = "" // roster locked specifically for this round
)

@Entity(
    tableName = "transcripts",
    indices = [Index(value = ["sessionId", "roundNumber"])]
)
data class TranscriptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val roundNumber: Int,
    val personaId: String,
    val personaName: String,
    val colorHex: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sentimentScore: Float = 0.5f,
    val sentimentLabel: String = "Neutral",
    val isDirectAddress: Boolean = false,
    val appendedToCanvas: Boolean = false
)

@Entity(
    tableName = "shared_frames",
    indices = [Index(value = ["sessionId", "roundNumber"])]
)
data class SharedFrameEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val roundNumber: Int,
    val frameContent: String,
    val moderatorNotes: String = "",
    val committedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "canvas_snapshots",
    indices = [Index(value = ["sessionId"])]
)
data class CanvasSnapshotEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val roundNumber: Int,
    val content: String,
    val branchTag: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_personas")
data class CustomPersonaEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val colorHex: String,
    val systemPrompt: String,
    val defaultLens: String,
    val expressionName: String = "SYNTHESIS",
    val createdAt: Long = System.currentTimeMillis()
)

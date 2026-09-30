package com.example.domain.model

sealed interface RoundState {
    data class Active(
        val activePersonas: List<Persona> = Persona.DEFAULTS,
        val flags: Map<String, Boolean> = activePersonas.associate { it.id to false },
        val currentDirectTargetId: String? = null
    ) : RoundState {
        val totalActiveNodes: Int
            get() = activePersonas.size

        val isMinimumThresholdMet: Boolean
            get() = activePersonas.isNotEmpty() && activePersonas.all { flags[it.id] == true }

        val completedCount: Int
            get() = activePersonas.count { flags[it.id] == true }

        val progressFraction: Float
            get() = if (activePersonas.isEmpty()) 0f else completedCount.toFloat() / activePersonas.size.toFloat()
    }

    data class Resolving(
        val roundNumber: Int,
        val rawTranscript: String,
        val statusMessage: String = "Distilling dialectic vectors & isolating consensus..."
    ) : RoundState

    data class Checkpoint(
        val roundNumber: Int,
        val proposedFrame: String,
        val rawTranscript: String
    ) : RoundState
}

data class SentimentTelemetry(
    val consensusScore: Float = 0.5f, // 0.0 to 1.0
    val tensionScore: Float = 0.35f,   // 0.0 to 1.0
    val dialecticVelocity: Int = 180,  // words per minute or round output velocity
    val dominantPolarity: String = "Equilibrium",
    val recentLogEntries: List<SentimentLogEntry> = emptyList()
)

data class SentimentLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val personaId: String,
    val personaName: String,
    val toneLabel: String,
    val polarityScore: Float,
    val summarySnippet: String
)

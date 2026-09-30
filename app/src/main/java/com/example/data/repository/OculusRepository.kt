package com.example.data.repository

import com.example.data.local.dao.OculusDao
import com.example.data.local.entity.*
import com.example.domain.model.EyeExpression
import com.example.domain.model.Persona
import com.example.domain.model.SentimentLogEntry
import com.example.domain.model.SentimentTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OculusRepository(private val dao: OculusDao) {

    fun observeLatestSession(): Flow<SessionEntity?> = dao.observeLatestSession()
    fun getAllSessions(): Flow<List<SessionEntity>> = dao.getAllSessions()

    suspend fun getOrCreateSession(
        topic: String,
        domainFocus: String,
        outputFormat: String,
        workflowRigor: String,
        temperature: Float,
        initialRosterIds: List<String> = Persona.DEFAULTS.map { it.id }
    ): SessionEntity {
        val rosterString = initialRosterIds.take(7).joinToString(",")
        val newSession = SessionEntity(
            id = UUID.randomUUID().toString(),
            title = if (topic.isNotBlank()) topic.take(50) else "Oculus Dialectic Protocol",
            topic = topic.ifBlank { "Autonomous Multi-Agent Consensus Architecture" },
            createdAt = System.currentTimeMillis(),
            currentRound = 1,
            totalRounds = 10,
            isCompleted = false,
            baselineTemperature = temperature,
            domainFocus = domainFocus,
            outputFormat = outputFormat,
            workflowRigor = workflowRigor,
            activeRosterIds = rosterString,
            stagedRosterIds = rosterString
        )
        dao.insertSession(newSession)
        // Initialize Round 1 with locked roster snapshot
        val initialRound = RoundEntity(
            id = UUID.randomUUID().toString(),
            sessionId = newSession.id,
            roundNumber = 1,
            status = "ACTIVE",
            startedAt = System.currentTimeMillis(),
            rosterSnapshotIds = rosterString
        )
        dao.insertRound(initialRound)
        return newSession
    }

    suspend fun updateStagedRoster(sessionId: String, stagedIds: List<String>) {
        val session = dao.getSession(sessionId) ?: return
        val clamped = stagedIds.take(7).joinToString(",")
        dao.updateSession(session.copy(stagedRosterIds = clamped))
    }

    suspend fun advanceRoundAndApplyStagedRoster(sessionId: String, newRoundNumber: Int, isCompleted: Boolean = false): List<String> {
        val session = dao.getSession(sessionId) ?: return emptyList()
        // The staged roster becomes the active roster for Round N+1!
        val nextActiveRoster = session.stagedRosterIds.ifBlank { session.activeRosterIds }
        dao.updateSession(
            session.copy(
                currentRound = newRoundNumber,
                isCompleted = isCompleted,
                activeRosterIds = nextActiveRoster
            )
        )
        val nextRound = RoundEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            roundNumber = newRoundNumber,
            status = "ACTIVE",
            startedAt = System.currentTimeMillis(),
            rosterSnapshotIds = nextActiveRoster
        )
        dao.insertRound(nextRound)
        return nextActiveRoster.split(",").filter { it.isNotBlank() }
    }

    suspend fun getRound(sessionId: String, roundNumber: Int): RoundEntity? {
        return dao.getRound(sessionId, roundNumber)
    }

    suspend fun updateRoundStatus(sessionId: String, roundNumber: Int, status: String) {
        val round = dao.getRound(sessionId, roundNumber)
        if (round != null) {
            dao.updateRound(
                round.copy(
                    status = status,
                    resolvedAt = if (status != "ACTIVE") System.currentTimeMillis() else null
                )
            )
        }
    }

    suspend fun addTranscript(
        sessionId: String,
        roundNumber: Int,
        personaId: String,
        personaName: String,
        colorHex: String,
        content: String,
        sentimentScore: Float,
        sentimentLabel: String,
        isDirectAddress: Boolean
    ): TranscriptEntity {
        val transcript = TranscriptEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            roundNumber = roundNumber,
            personaId = personaId,
            personaName = personaName,
            colorHex = colorHex,
            content = content,
            timestamp = System.currentTimeMillis(),
            sentimentScore = sentimentScore,
            sentimentLabel = sentimentLabel,
            isDirectAddress = isDirectAddress,
            appendedToCanvas = false
        )
        dao.insertTranscript(transcript)
        return transcript
    }

    fun observeTranscriptsForRound(sessionId: String, roundNumber: Int): Flow<List<TranscriptEntity>> {
        return dao.observeTranscriptsForRound(sessionId, roundNumber)
    }

    suspend fun getTranscriptsForRound(sessionId: String, roundNumber: Int): List<TranscriptEntity> {
        return dao.getTranscriptsForRound(sessionId, roundNumber)
    }

    suspend fun getAllTranscriptsForSession(sessionId: String): List<TranscriptEntity> {
        return dao.getAllTranscriptsForSession(sessionId)
    }

    suspend fun saveSharedFrame(
        sessionId: String,
        roundNumber: Int,
        frameContent: String,
        moderatorNotes: String
    ) {
        val frame = SharedFrameEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            roundNumber = roundNumber,
            frameContent = frameContent,
            moderatorNotes = moderatorNotes,
            committedAt = System.currentTimeMillis()
        )
        dao.insertSharedFrame(frame)
    }

    fun observeLatestSharedFrame(sessionId: String): Flow<SharedFrameEntity?> {
        return dao.observeLatestSharedFrame(sessionId)
    }

    suspend fun getAllSharedFramesForSession(sessionId: String): List<SharedFrameEntity> {
        return dao.getAllSharedFramesForSession(sessionId)
    }

    suspend fun saveCanvasSnapshot(
        sessionId: String,
        roundNumber: Int,
        content: String,
        branchTag: String
    ): CanvasSnapshotEntity {
        val snapshot = CanvasSnapshotEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            roundNumber = roundNumber,
            content = content,
            branchTag = branchTag.ifBlank { "Branch @ Round $roundNumber" },
            timestamp = System.currentTimeMillis()
        )
        dao.insertCanvasSnapshot(snapshot)
        return snapshot
    }

    fun observeSnapshots(sessionId: String): Flow<List<CanvasSnapshotEntity>> {
        return dao.observeSnapshotsForSession(sessionId)
    }

    // Custom Personas Persistence
    fun observeCustomPersonas(): Flow<List<Persona>> {
        return dao.observeAllCustomPersonas().map { list ->
            list.map { entityToPersona(it) }
        }
    }

    suspend fun getCustomPersonas(): List<Persona> {
        return dao.getCustomPersonas().map { entityToPersona(it) }
    }

    suspend fun saveCustomPersona(persona: Persona) {
        dao.insertCustomPersona(
            CustomPersonaEntity(
                id = persona.id,
                name = persona.name,
                role = persona.role,
                colorHex = persona.colorHex,
                systemPrompt = persona.systemPrompt,
                defaultLens = persona.defaultLens,
                expressionName = persona.defaultExpression.name
            )
        )
    }

    suspend fun deleteCustomPersona(id: String) {
        dao.deleteCustomPersona(id)
    }

    private fun entityToPersona(e: CustomPersonaEntity): Persona {
        val expression = try {
            EyeExpression.valueOf(e.expressionName)
        } catch (_: Exception) {
            EyeExpression.SYNTHESIS
        }
        return Persona(
            id = e.id,
            name = e.name,
            role = e.role,
            colorHex = e.colorHex,
            color = Persona.parseHexColor(e.colorHex),
            systemPrompt = e.systemPrompt,
            defaultLens = e.defaultLens,
            defaultExpression = expression,
            isCustom = true
        )
    }

    fun computeSentimentTelemetry(transcripts: List<TranscriptEntity>): SentimentTelemetry {
        if (transcripts.isEmpty()) {
            return SentimentTelemetry()
        }
        val avgScore = transcripts.map { it.sentimentScore }.average().toFloat()
        val crimsonContributions = transcripts.filter { it.personaId == "crimson_devils_advocate" }
        val tension = if (crimsonContributions.isNotEmpty()) 0.72f else (1.0f - avgScore).coerceIn(0.2f, 0.85f)
        val consensus = (avgScore * 0.7f + (1f - tension * 0.3f)).coerceIn(0.1f, 0.95f)

        val recentEntries = transcripts.takeLast(10).reversed().map {
            SentimentLogEntry(
                timestamp = it.timestamp,
                personaId = it.personaId,
                personaName = it.personaName,
                toneLabel = it.sentimentLabel,
                polarityScore = it.sentimentScore,
                summarySnippet = it.content.take(65)
            )
        }

        val dominantPolarity = when {
            consensus > 0.7f -> "Strong Convergence"
            tension > 0.6f -> "Adversarial Friction"
            else -> "Dialectic Exploration"
        }

        return SentimentTelemetry(
            consensusScore = consensus,
            tensionScore = tension,
            dialecticVelocity = (transcripts.sumOf { it.content.split("\\s+".toRegex()).size } / transcripts.size.coerceAtLeast(1) * 3).coerceIn(90, 450),
            dominantPolarity = dominantPolarity,
            recentLogEntries = recentEntries
        )
    }
}

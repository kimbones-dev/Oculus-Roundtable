package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OculusDatabase
import com.example.data.local.entity.CanvasSnapshotEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.local.entity.SharedFrameEntity
import com.example.data.local.entity.TranscriptEntity
import com.example.data.network.GeminiOrchestrator
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.OculusRepository
import com.example.domain.model.Persona
import com.example.domain.model.RoundState
import com.example.domain.model.SentimentTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoundtableViewModel(application: Application) : AndroidViewModel(application) {

    private val db = OculusDatabase.getInstance(application)
    private val repository = OculusRepository(db.oculusDao())
    private val preferencesRepository = UserPreferencesRepository(application)
    private val orchestrator = GeminiOrchestrator()

    val userPreferences = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _currentSession = MutableStateFlow<SessionEntity?>(null)
    val currentSession: StateFlow<SessionEntity?> = _currentSession.asStateFlow()

    private val _customPersonas = MutableStateFlow<List<Persona>>(emptyList())
    val customPersonas: StateFlow<List<Persona>> = _customPersonas.asStateFlow()

    val allAvailablePersonas: StateFlow<List<Persona>> = combine(
        _customPersonas
    ) { custom ->
        // Combine default 7 personas + any created custom personas
        val defaults = Persona.DEFAULTS
        val customs = custom[0]
        defaults + customs
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Persona.DEFAULTS
    )

    private val _activeRoster = MutableStateFlow<List<Persona>>(Persona.DEFAULTS)
    val activeRoster: StateFlow<List<Persona>> = _activeRoster.asStateFlow()

    private val _stagedRoster = MutableStateFlow<List<Persona>>(Persona.DEFAULTS)
    val stagedRoster: StateFlow<List<Persona>> = _stagedRoster.asStateFlow()

    private val _roundState = MutableStateFlow<RoundState>(RoundState.Active(activePersonas = Persona.DEFAULTS))
    val roundState: StateFlow<RoundState> = _roundState.asStateFlow()

    private val _generatingPersonas = MutableStateFlow<Set<String>>(emptySet())
    val generatingPersonas: StateFlow<Set<String>> = _generatingPersonas.asStateFlow()

    private val _latestThoughtByPersona = MutableStateFlow<Map<String, TranscriptEntity>>(emptyMap())
    val latestThoughtByPersona: StateFlow<Map<String, TranscriptEntity>> = _latestThoughtByPersona.asStateFlow()

    private val _transcriptsForCurrentRound = MutableStateFlow<List<TranscriptEntity>>(emptyList())
    val transcriptsForCurrentRound: StateFlow<List<TranscriptEntity>> = _transcriptsForCurrentRound.asStateFlow()

    private val _sentimentTelemetry = MutableStateFlow(SentimentTelemetry())
    val sentimentTelemetry: StateFlow<SentimentTelemetry> = _sentimentTelemetry.asStateFlow()

    private val _centralCanvasContent = MutableStateFlow("")
    val centralCanvasContent: StateFlow<String> = _centralCanvasContent.asStateFlow()

    private val _snapshots = MutableStateFlow<List<CanvasSnapshotEntity>>(emptyList())
    val snapshots: StateFlow<List<CanvasSnapshotEntity>> = _snapshots.asStateFlow()

    private val _sharedFrames = MutableStateFlow<List<SharedFrameEntity>>(emptyList())
    val sharedFrames: StateFlow<List<SharedFrameEntity>> = _sharedFrames.asStateFlow()

    private val _latestSharedFrame = MutableStateFlow<SharedFrameEntity?>(null)
    val latestSharedFrame: StateFlow<SharedFrameEntity?> = _latestSharedFrame.asStateFlow()

    private val _directTargetPersona = MutableStateFlow<Persona?>(null)
    val directTargetPersona: StateFlow<Persona?> = _directTargetPersona.asStateFlow()

    private val _focusedTranscript = MutableStateFlow<TranscriptEntity?>(null)
    val focusedTranscript: StateFlow<TranscriptEntity?> = _focusedTranscript.asStateFlow()

    private val _isControlNubExpanded = MutableStateFlow(false)
    val isControlNubExpanded: StateFlow<Boolean> = _isControlNubExpanded.asStateFlow()

    private val _baselineTemperature = MutableStateFlow(0.70f)
    val baselineTemperature: StateFlow<Float> = _baselineTemperature.asStateFlow()

    init {
        // Observe custom personas from DB
        viewModelScope.launch {
            repository.observeCustomPersonas().collect { list ->
                _customPersonas.value = list
            }
        }

        viewModelScope.launch {
            repository.observeLatestSession().collect { session ->
                if (session != null && _currentSession.value == null) {
                    _currentSession.value = session
                    _baselineTemperature.value = session.baselineTemperature
                    resolveRostersFromSession(session)
                    loadRoundData(session)
                }
            }
        }
    }

    private fun resolveRostersFromSession(session: SessionEntity) {
        val pool = allAvailablePersonas.value.associateBy { it.id }
        val activeIds = session.activeRosterIds.split(",").filter { it.isNotBlank() }
        val stagedIds = session.stagedRosterIds.split(",").filter { it.isNotBlank() }

        val activeList = activeIds.mapNotNull { pool[it] ?: Persona.DEFAULTS.find { d -> d.id == it } }.take(7)
        val stagedList = stagedIds.mapNotNull { pool[it] ?: Persona.DEFAULTS.find { d -> d.id == it } }.take(7)

        val resolvedActive = activeList.ifEmpty { Persona.DEFAULTS }
        val resolvedStaged = stagedList.ifEmpty { Persona.DEFAULTS }

        _activeRoster.value = resolvedActive
        _stagedRoster.value = resolvedStaged
        _roundState.value = RoundState.Active(activePersonas = resolvedActive)
    }

    fun completeOnboarding(domain: String, format: String, rigor: String, temperature: Float, selectedRosterIds: List<String>? = null) {
        viewModelScope.launch {
            preferencesRepository.completeOnboarding(domain, format, rigor, temperature)
            _baselineTemperature.value = temperature
            val roster = selectedRosterIds ?: Persona.DEFAULTS.map { it.id }
            initNewSession(domain, format, rigor, temperature, initialRosterIds = roster)
        }
    }

    fun initNewSession(
        domain: String = "Multi-Disciplinary Synthesis",
        format: String = "Structured Dialectic Spec",
        rigor: String = "Adversarial Consensus",
        temperature: Float = 0.70f,
        topic: String = "Autonomous Multi-Agent Consensus Architecture",
        initialRosterIds: List<String> = Persona.DEFAULTS.map { it.id }
    ) {
        viewModelScope.launch {
            val session = repository.getOrCreateSession(
                topic = topic,
                domainFocus = domain,
                outputFormat = format,
                workflowRigor = rigor,
                temperature = temperature,
                initialRosterIds = initialRosterIds
            )
            _currentSession.value = session
            _baselineTemperature.value = temperature
            resolveRostersFromSession(session)
            loadRoundData(session)
        }
    }

    private fun loadRoundData(session: SessionEntity) {
        viewModelScope.launch {
            repository.observeTranscriptsForRound(session.id, session.currentRound)
                .collect { list ->
                    _transcriptsForCurrentRound.value = list
                    _latestThoughtByPersona.value = list.associateBy { it.personaId }
                    _sentimentTelemetry.value = repository.computeSentimentTelemetry(list)

                    val currentState = _roundState.value
                    if (currentState is RoundState.Active) {
                        val contributedIds = list.map { it.personaId }.toSet()
                        val updatedFlags = currentState.flags.toMutableMap()
                        contributedIds.forEach { updatedFlags[it] = true }
                        _roundState.value = currentState.copy(flags = updatedFlags)
                    }
                }
        }

        viewModelScope.launch {
            repository.observeSnapshots(session.id).collect { list ->
                _snapshots.value = list
            }
        }

        viewModelScope.launch {
            val frames = repository.getAllSharedFramesForSession(session.id)
            _sharedFrames.value = frames
            _latestSharedFrame.value = frames.lastOrNull()
        }
    }

    // Custom Personas Operations
    fun saveCustomPersona(persona: Persona) {
        viewModelScope.launch {
            repository.saveCustomPersona(persona)
            // Add to staged roster if room
            val currentStaged = _stagedRoster.value
            if (currentStaged.size < 7 && currentStaged.none { it.id == persona.id }) {
                updateStagedRoster(currentStaged + persona)
            }
        }
    }

    fun deleteCustomPersona(id: String) {
        viewModelScope.launch {
            repository.deleteCustomPersona(id)
            val updatedStaged = _stagedRoster.value.filter { it.id != id }
            updateStagedRoster(updatedStaged)
        }
    }

    fun updateStagedRoster(newRoster: List<Persona>) {
        val session = _currentSession.value ?: return
        val clamped = newRoster.take(7)
        _stagedRoster.value = clamped
        viewModelScope.launch {
            repository.updateStagedRoster(session.id, clamped.map { it.id })
        }
    }

    fun setDirectTarget(persona: Persona) {
        if (_directTargetPersona.value?.id == persona.id) {
            _directTargetPersona.value = null
        } else {
            _directTargetPersona.value = persona
        }
    }

    fun skipPersona(personaId: String) {
        val currentState = _roundState.value
        if (currentState is RoundState.Active) {
            val updated = currentState.flags.toMutableMap()
            updated[personaId] = true
            _roundState.value = currentState.copy(flags = updated)
        }
    }

    fun sendModeratorPrompt(prompt: String) {
        val session = _currentSession.value ?: return
        val currentRound = session.currentRound
        val targeted = _directTargetPersona.value
        val activeNodes = _activeRoster.value

        viewModelScope.launch {
            val recentTranscripts = _transcriptsForCurrentRound.value.map { it.personaName to it.content }
            val sharedFrameContent = _latestSharedFrame.value?.frameContent

            if (targeted != null) {
                _directTargetPersona.value = null
                executePersonaTurn(
                    persona = targeted,
                    session = session,
                    roundNumber = currentRound,
                    prompt = prompt,
                    sharedFrame = sharedFrameContent,
                    recentTranscripts = recentTranscripts,
                    isDirect = true
                )
            } else {
                val currentState = _roundState.value
                val activeFlags = if (currentState is RoundState.Active) currentState.flags else emptyMap()
                val pendingPersonas = activeNodes.filter { activeFlags[it.id] != true }
                val targetList = if (pendingPersonas.isNotEmpty()) pendingPersonas else activeNodes

                targetList.forEach { persona ->
                    executePersonaTurn(
                        persona = persona,
                        session = session,
                        roundNumber = currentRound,
                        prompt = prompt,
                        sharedFrame = sharedFrameContent,
                        recentTranscripts = recentTranscripts,
                        isDirect = false
                    )
                }
            }
        }
    }

    private fun executePersonaTurn(
        persona: Persona,
        session: SessionEntity,
        roundNumber: Int,
        prompt: String,
        sharedFrame: String?,
        recentTranscripts: List<Pair<String, String>>,
        isDirect: Boolean
    ) {
        viewModelScope.launch {
            _generatingPersonas.update { it + persona.id }
            try {
                val response = orchestrator.queryPersona(
                    persona = persona,
                    currentTopic = session.topic,
                    userPrompt = prompt,
                    updatedSharedFrame = sharedFrame,
                    recentRoundTranscripts = recentTranscripts,
                    temperature = _baselineTemperature.value,
                    isDirectAddress = isDirect
                )

                repository.addTranscript(
                    sessionId = session.id,
                    roundNumber = roundNumber,
                    personaId = persona.id,
                    personaName = persona.name,
                    colorHex = persona.colorHex,
                    content = response.text,
                    sentimentScore = response.sentimentScore,
                    sentimentLabel = response.sentimentLabel,
                    isDirectAddress = isDirect
                )

                val currentState = _roundState.value
                if (currentState is RoundState.Active) {
                    val updated = currentState.flags.toMutableMap()
                    updated[persona.id] = true
                    _roundState.value = currentState.copy(flags = updated)
                }
            } finally {
                _generatingPersonas.update { it - persona.id }
            }
        }
    }

    fun callTheTurn() {
        val session = _currentSession.value ?: return
        val roundNumber = session.currentRound
        val transcripts = _transcriptsForCurrentRound.value

        val rawTranscript = buildString {
            transcripts.forEach { t ->
                append("[${t.personaName}]:\n${t.content}\n\n")
            }
        }

        _roundState.value = RoundState.Resolving(
            roundNumber = roundNumber,
            rawTranscript = rawTranscript
        )

        viewModelScope.launch {
            val synthesis = orchestrator.resolveTurnSynthesis(
                roundNumber = roundNumber,
                topic = session.topic,
                rawRoundTranscript = rawTranscript,
                previousFrame = _latestSharedFrame.value?.frameContent
            )

            _roundState.value = RoundState.Checkpoint(
                roundNumber = roundNumber,
                proposedFrame = synthesis.updatedSharedFrame,
                rawTranscript = rawTranscript
            )
        }
    }

    fun cancelTurnResolution() {
        val contributedIds = _transcriptsForCurrentRound.value.map { it.personaId }.toSet()
        val flags = _activeRoster.value.associate { it.id to contributedIds.contains(it.id) }
        _roundState.value = RoundState.Active(activePersonas = _activeRoster.value, flags = flags)
    }

    fun commitSharedFrame(editedFrame: String, moderatorNotes: String) {
        val session = _currentSession.value ?: return
        val currentRound = session.currentRound
        val nextRound = currentRound + 1

        viewModelScope.launch {
            repository.saveSharedFrame(
                sessionId = session.id,
                roundNumber = currentRound,
                frameContent = editedFrame,
                moderatorNotes = moderatorNotes
            )
            repository.updateRoundStatus(session.id, currentRound, "COMMITTED")

            // Advance round and apply staged roster to active for Round N+1!
            val isCompleted = nextRound > session.totalRounds
            val nextActiveIds = repository.advanceRoundAndApplyStagedRoster(session.id, nextRound, isCompleted)

            val pool = allAvailablePersonas.value.associateBy { it.id }
            val nextActivePersonas = nextActiveIds.mapNotNull { pool[it] ?: Persona.DEFAULTS.find { d -> d.id == it } }.take(7)
            val resolvedActive = nextActivePersonas.ifEmpty { _stagedRoster.value }

            _activeRoster.value = resolvedActive
            _roundState.value = RoundState.Active(
                activePersonas = resolvedActive,
                flags = resolvedActive.associate { it.id to false }
            )
            _latestThoughtByPersona.value = emptyMap()
            _transcriptsForCurrentRound.value = emptyList()

            val frames = repository.getAllSharedFramesForSession(session.id)
            _sharedFrames.value = frames
            _latestSharedFrame.value = frames.lastOrNull()
        }
    }

    fun updateCanvas(content: String) {
        _centralCanvasContent.value = content
    }

    fun appendToCanvas(textToAppend: String) {
        val current = _centralCanvasContent.value
        val addition = if (current.isBlank()) textToAppend else "\n\n$textToAppend"
        _centralCanvasContent.value = current + addition
    }

    fun quickSnapshotBranch(customTag: String? = null) {
        val session = _currentSession.value ?: return
        val content = _centralCanvasContent.value
        val roundNum = session.currentRound
        val tag = customTag ?: "Branch Snapshot @ Round #$roundNum"

        viewModelScope.launch {
            repository.saveCanvasSnapshot(
                sessionId = session.id,
                roundNumber = roundNum,
                content = content,
                branchTag = tag
            )
        }
    }

    fun restoreSnapshot(snapshot: CanvasSnapshotEntity) {
        _centralCanvasContent.value = snapshot.content
    }

    fun setFocusedTranscript(transcript: TranscriptEntity?) {
        _focusedTranscript.value = transcript
    }

    fun toggleControlNub() {
        _isControlNubExpanded.update { !it }
    }

    fun setTemperature(temp: Float) {
        _baselineTemperature.value = temp
    }
}

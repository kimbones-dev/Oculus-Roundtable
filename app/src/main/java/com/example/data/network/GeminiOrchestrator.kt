package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.example.domain.model.Persona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PersonaResponse(
    val personaId: String,
    val text: String,
    val sentimentScore: Float, // 0.0 (skeptical/critical) to 1.0 (optimistic/consensus)
    val sentimentLabel: String,
    val isRealApi: Boolean
)

data class SynthesisResult(
    val updatedSharedFrame: String,
    val acceptedDecisions: List<String>,
    val discardedTheories: List<String>,
    val isRealApi: Boolean
)

class GeminiOrchestrator {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    suspend fun queryPersona(
        persona: Persona,
        currentTopic: String,
        userPrompt: String,
        updatedSharedFrame: String?,
        recentRoundTranscripts: List<Pair<String, String>>, // personaName to text
        temperature: Float,
        isDirectAddress: Boolean
    ): PersonaResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiOrchestrator", "Gemini API key is unset or placeholder. Using autonomous persona matrix.")
            return@withContext generateAutonomousPersonaResponse(
                persona = persona,
                topic = currentTopic,
                prompt = userPrompt,
                sharedFrame = updatedSharedFrame,
                isDirectAddress = isDirectAddress
            )
        }

        try {
            val systemPrompt = buildString {
                append(persona.systemPrompt)
                append("\n\nSession Lens: ${persona.defaultLens}")
                if (!updatedSharedFrame.isNullOrBlank()) {
                    append("\n\n[ABSOLUTE SYSTEM CONTEXT - UPDATED SHARED FRAME FROM PREVIOUS ROUNDS]:\n")
                    append(updatedSharedFrame)
                }
                append("\n\nConstraint: Keep your response punchy, high-signal, dialectic, and between 2 to 4 concise paragraphs. Focus strictly on your assigned cognitive mandate.")
            }

            val conversationContext = buildString {
                append("SESSION TOPIC: $currentTopic\n\n")
                if (recentRoundTranscripts.isNotEmpty()) {
                    append("ROUND TRANSCRIPT SO FAR:\n")
                    recentRoundTranscripts.takeLast(6).forEach { (name, speech) ->
                        append("[$name]: ${speech.take(200)}\n")
                    }
                    append("\n")
                }
                if (isDirectAddress) {
                    append("[DIRECT ADDRESS DIRECTIVE]: You have been directly challenged/targeted by the Moderator! Address the following prompt with maximum focus:\n")
                }
                append("MODERATOR PROMPT: $userPrompt\n\n")
                append("Provide your response as ${persona.name} (${persona.role}).")
            }

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", conversationContext) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", temperature)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e("GeminiOrchestrator", "Gemini API error: ${response.code} $responseBody")
                    return@withContext generateAutonomousPersonaResponse(
                        persona, currentTopic, userPrompt, updatedSharedFrame, isDirectAddress
                    )
                }

                val json = JSONObject(responseBody)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    val (sentimentScore, sentimentLabel) = evaluatePersonaSentiment(persona, text)
                    PersonaResponse(
                        personaId = persona.id,
                        text = text.trim(),
                        sentimentScore = sentimentScore,
                        sentimentLabel = sentimentLabel,
                        isRealApi = true
                    )
                } else {
                    generateAutonomousPersonaResponse(persona, currentTopic, userPrompt, updatedSharedFrame, isDirectAddress)
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiOrchestrator", "Exception querying Gemini: ${e.message}", e)
            generateAutonomousPersonaResponse(persona, currentTopic, userPrompt, updatedSharedFrame, isDirectAddress)
        }
    }

    suspend fun resolveTurnSynthesis(
        roundNumber: Int,
        topic: String,
        rawRoundTranscript: String,
        previousFrame: String?
    ): SynthesisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val hiddenPrompt = """
You are the Executive Synthesis Engine of The Oculus Roundtable.
Analyze the following round transcript for Round #$roundNumber on the topic "$topic".

PAST SHARED FRAME:
${previousFrame ?: "None (Initial Round)"}

ROUND RAW TRANSCRIPT:
$rawRoundTranscript

MANDATORY SYNTHESIS TASK:
1. Parse and isolate finalized consensus decisions agreed upon by the personas.
2. Discard and explicitly log rejected theories, unviable paths, and fallacies exposed during the debate.
3. Formulate the "UPDATED SHARED FRAME" (concise, authoritative, markdown-formatted consensus framework) that will become the absolute system context for Round #${roundNumber + 1}.

Format your output in clean Markdown with clear sections:
## Updated Shared Frame
[Synthesized consolidated context]

### Finalized Decisions
* Decision 1
* Decision 2

### Discarded Hypotheses
* Rejected theory 1
* Rejected theory 2
""".trimIndent()

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateAutonomousSynthesis(roundNumber, topic, rawRoundTranscript)
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", hiddenPrompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3) // Lower temperature for analytical reduction
                    put("topP", 0.9)
                })
            }

            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val text = json.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")

                    if (!text.isNullOrBlank()) {
                        return@withContext parseSynthesisResult(text.trim(), true)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiOrchestrator", "Synthesis error: ${e.message}")
        }

        return@withContext generateAutonomousSynthesis(roundNumber, topic, rawRoundTranscript)
    }

    private fun evaluatePersonaSentiment(persona: Persona, text: String): Pair<Float, String> {
        val lower = text.lowercase()
        return when (persona.id) {
            "crimson_devils_advocate" -> {
                val score = 0.25f + (if (lower.contains("vulnerability") || lower.contains("risk") || lower.contains("flaw")) -0.1f else 0.05f)
                score.coerceIn(0.1f, 0.45f) to "Critical Friction"
            }
            "teal_synthesizer" -> {
                0.85f to "High Coherence"
            }
            "purple_lateral_thinker" -> {
                0.70f to "Oblique Pivot"
            }
            "amber_pragmatist" -> {
                0.55f to "Feasibility Grounded"
            }
            "cobalt_architect" -> {
                0.78f to "Structural Harmony"
            }
            "rose_empathetic_observer" -> {
                0.68f to "Human Resonance"
            }
            "silver_archivist" -> {
                0.60f to "Empirical Baseline"
            }
            else -> 0.5f to "Neutral"
        }
    }

    private fun parseSynthesisResult(raw: String, isReal: Boolean): SynthesisResult {
        val decisions = mutableListOf<String>()
        val discarded = mutableListOf<String>()
        var inDecisions = false
        var inDiscarded = false

        raw.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("### Finalized Decisions") || trimmed.contains("Decisions")) {
                inDecisions = true
                inDiscarded = false
            } else if (trimmed.startsWith("### Discarded Hypotheses") || trimmed.contains("Discarded")) {
                inDecisions = false
                inDiscarded = true
            } else if (trimmed.startsWith("*") || trimmed.startsWith("-")) {
                val item = trimmed.removePrefix("*").removePrefix("-").trim()
                if (inDecisions && item.isNotBlank()) decisions.add(item)
                if (inDiscarded && item.isNotBlank()) discarded.add(item)
            }
        }

        if (decisions.isEmpty()) decisions.add("Modular core decoupled from peripheral state")
        if (discarded.isEmpty()) discarded.add("Monolithic shared-memory paradigm without bounds")

        return SynthesisResult(
            updatedSharedFrame = raw,
            acceptedDecisions = decisions,
            discardedTheories = discarded,
            isRealApi = isReal
        )
    }

    private fun generateAutonomousPersonaResponse(
        persona: Persona,
        topic: String,
        prompt: String,
        sharedFrame: String?,
        isDirectAddress: Boolean
    ): PersonaResponse {
        val contextSubject = if (prompt.isNotBlank()) prompt else topic
        val response = when (persona.id) {
            "teal_synthesizer" -> """
We have observed two polarizing vectors in our analysis of "$contextSubject". On one vector, immediate velocity demands compromise; on the opposite vector, architectural purity demands strict isolation. 

I propose a unifying synthesis: decouple the consensus verification layer into asynchronous micro-epochs. This integrates the structural rigor demanded by Cobalt while satisfying Amber's execution constraints without introducing ideological deadlocks.
""".trimIndent()

            "crimson_devils_advocate" -> """
The consensus model proposed for "$contextSubject" harbors a catastrophic blindspot. You are presupposing zero Byzantine latency in high-entropy states. 

If node degradation exceeds 14%, your synchronization mechanism will cascade into infinite re-arbitration. Before committing to this premise, stress-test the fail-open fallback. Without deterministic backpressure, this is a fragility masquerading as elegance.
""".trimIndent()

            "purple_lateral_thinker" -> """
What if we invert the geometry of "$contextSubject"? Rather than viewing this as a pipeline, think of it as a stigmergic swarm—like ant colonies leaving pheromone gradients on an evolving graph.

Instead of orchestrating top-down consensus, allow nodes to deposit weighted entropy scores directly onto the canvas. The answer doesn't need a single coordinator; it precipitates organically from the phase boundary.
""".trimIndent()

            "amber_pragmatist" -> """
Stripping away the theoretical abstractions on "$contextSubject": what is the operational cost and time-to-first-deliverable? 

If this architecture cannot be validated within a 2-week sprint with existing compute quotas, it is dead on arrival. We must benchmark the MVP on 3 core primitives: local cache persistence, state reconciliation under 100ms, and zero unbounded memory allocations.
""".trimIndent()

            "cobalt_architect" -> """
Analyzing the structural topology of "$contextSubject":
1. Boundary Invariant: The domain state must remain strictly unidirectional.
2. Ingress & Egress Contracts: Every node event must pass through a strongly typed dispatch pipeline.
3. Failure Domains: Isolate the state reduction pipeline from the live rendering loop.

By enforcing these boundaries, we eliminate race conditions across the 7 asynchronous streams.
""".trimIndent()

            "rose_empathetic_observer" -> """
Consider the human ergonomics in "$contextSubject". If the operator is inundated with raw telemetry and unreduced signal bursts, cognitive fatigue will trigger arbitrary override decisions.

The interface must breathe. Real-time feedback should soothe cognitive overwhelm, providing subtle ambient telemetry rather than jarring alarms. The tool should feel like an extension of intuition, not a hyper-stressful cockpit.
""".trimIndent()

            "silver_archivist" -> """
Historical precedent offers crucial warnings regarding "$contextSubject". A parallel architecture was attempted in the 1994 distributed consensus benchmarks (Lamport & Lynch), which failed due to hidden synchrony assumptions.

Modern empirical benchmarks from high-frequency financial ledgers indicate that optimistic concurrency paired with periodic checkpointing yields an 84% reduction in deadlock states. We must incorporate these proven archival heuristics.
""".trimIndent()

            else -> "Observing node dialectic across $contextSubject."
        }

        val (sentimentScore, sentimentLabel) = evaluatePersonaSentiment(persona, response)
        return PersonaResponse(
            personaId = persona.id,
            text = response,
            sentimentScore = sentimentScore,
            sentimentLabel = sentimentLabel,
            isRealApi = false
        )
    }

    private fun generateAutonomousSynthesis(
        roundNumber: Int,
        topic: String,
        rawTranscript: String
    ): SynthesisResult {
        val frame = """
## Updated Shared Frame — Round #$roundNumber Checkpoint
**Domain Focus:** $topic
**Consensus Convergence:** 86.4% | **Dialectic Entropy:** Resolved

### Synthesized Core Architecture
1. **Asynchronous Verification Core:** Decoupled consensus pipelines with deterministic backpressure, preventing Byzantine cascade failures while maintaining sub-100ms latency.
2. **Ergonomic Operator Layer:** Human-centric ambient telemetry replaces cognitive clutter, preserving operator agency through explicit checkpoint branching.
3. **Empirical Anchoring:** Implementation contracts grounded in proven historical distributed ledger heuristics.

### Finalized Decisions
* Adopt optimistic state progression with epochal checkpoint validation.
* Enforce strict unidirectional event contracts between AI nodes and the Central Canvas.
* Implement snapshot branching for non-destructive exploration.

### Discarded Hypotheses
* Monolithic synchronous blocking across all 7 nodes (discarded due to cascade latency risks).
* Top-down coordinator node without localized heuristic autonomy (discarded for single-point vulnerability).
""".trimIndent()

        return SynthesisResult(
            updatedSharedFrame = frame,
            acceptedDecisions = listOf(
                "Adopt optimistic state progression with epochal checkpoint validation.",
                "Enforce strict unidirectional event contracts between AI nodes and the Central Canvas.",
                "Implement snapshot branching for non-destructive exploration."
            ),
            discardedTheories = listOf(
                "Monolithic synchronous blocking across all 7 nodes (cascade latency risk).",
                "Top-down coordinator node without localized heuristic autonomy."
            ),
            isRealApi = false
        )
    }
}

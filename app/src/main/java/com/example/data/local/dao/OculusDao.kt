package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OculusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSession(id: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity)

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Query("SELECT * FROM rounds WHERE sessionId = :sessionId AND roundNumber = :roundNumber LIMIT 1")
    suspend fun getRound(sessionId: String, roundNumber: Int): RoundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId AND roundNumber = :roundNumber ORDER BY timestamp ASC")
    fun observeTranscriptsForRound(sessionId: String, roundNumber: Int): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId AND roundNumber = :roundNumber ORDER BY timestamp ASC")
    suspend fun getTranscriptsForRound(sessionId: String, roundNumber: Int): List<TranscriptEntity>

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId ORDER BY roundNumber ASC, timestamp ASC")
    suspend fun getAllTranscriptsForSession(sessionId: String): List<TranscriptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedFrame(frame: SharedFrameEntity)

    @Query("SELECT * FROM shared_frames WHERE sessionId = :sessionId ORDER BY roundNumber DESC LIMIT 1")
    fun observeLatestSharedFrame(sessionId: String): Flow<SharedFrameEntity?>

    @Query("SELECT * FROM shared_frames WHERE sessionId = :sessionId ORDER BY roundNumber ASC")
    suspend fun getAllSharedFramesForSession(sessionId: String): List<SharedFrameEntity>

    @Query("SELECT * FROM shared_frames WHERE sessionId = :sessionId AND roundNumber = :roundNumber LIMIT 1")
    suspend fun getSharedFrameForRound(sessionId: String, roundNumber: Int): SharedFrameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanvasSnapshot(snapshot: CanvasSnapshotEntity)

    @Query("SELECT * FROM canvas_snapshots WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun observeSnapshotsForSession(sessionId: String): Flow<List<CanvasSnapshotEntity>>

    // Custom Personas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPersona(persona: CustomPersonaEntity)

    @Query("SELECT * FROM custom_personas ORDER BY createdAt DESC")
    fun observeAllCustomPersonas(): Flow<List<CustomPersonaEntity>>

    @Query("SELECT * FROM custom_personas ORDER BY createdAt DESC")
    suspend fun getCustomPersonas(): List<CustomPersonaEntity>

    @Query("DELETE FROM custom_personas WHERE id = :id")
    suspend fun deleteCustomPersona(id: String)
}

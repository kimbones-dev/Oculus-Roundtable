package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.OculusDao
import com.example.data.local.entity.*

@Database(
    entities = [
        SessionEntity::class,
        RoundEntity::class,
        TranscriptEntity::class,
        SharedFrameEntity::class,
        CanvasSnapshotEntity::class,
        CustomPersonaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OculusDatabase : RoomDatabase() {
    abstract fun oculusDao(): OculusDao

    companion object {
        @Volatile
        private var INSTANCE: OculusDatabase? = null

        fun getInstance(context: Context): OculusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OculusDatabase::class.java,
                    "oculus_roundtable.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

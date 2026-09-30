package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.Persona
import com.example.domain.model.RoundState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("The Oculus Roundtable", appName)
    }

    @Test
    fun `verify default personas and presets exist with valid configuration`() {
        assertEquals(7, Persona.DEFAULTS.size)
        val ids = Persona.DEFAULTS.map { it.id }.toSet()
        assertEquals(7, ids.size)

        // Verify key personas
        assertTrue(ids.contains("teal_synthesizer"))
        assertTrue(ids.contains("crimson_devils_advocate"))
        assertTrue(ids.contains("purple_lateral_thinker"))
        assertTrue(ids.contains("amber_pragmatist"))
        assertTrue(ids.contains("cobalt_architect"))
        assertTrue(ids.contains("rose_empathetic_observer"))
        assertTrue(ids.contains("silver_archivist"))

        // Verify archetype presets exist
        assertTrue(Persona.PRESET_TEMPLATES.isNotEmpty())
    }

    @Test
    fun `verify round state minimum threshold mechanics with flexible roster`() {
        val threeNodes = Persona.DEFAULTS.take(3)
        val state = RoundState.Active(
            activePersonas = threeNodes,
            flags = mapOf(
                threeNodes[0].id to true,
                threeNodes[1].id to true,
                threeNodes[2].id to false
            )
        )
        assertFalse("2 of 3 flags should not meet threshold", state.isMinimumThresholdMet)
        assertEquals(2, state.completedCount)

        val completedState = state.copy(flags = threeNodes.associate { it.id to true })
        assertTrue("All active nodes in user-defined roster satisfy threshold", completedState.isMinimumThresholdMet)
        assertEquals(3, completedState.completedCount)
    }
}

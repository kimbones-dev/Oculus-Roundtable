import { useAppStore } from '../store';
import axios from 'axios';

// The Chat Flow Manager handles the dynamic turn orchestration
class ChatFlowManager {
  constructor() {
    this.isProcessing = false;
  }

  async processModInput(promptText) {
    if (this.isProcessing) return;
    this.isProcessing = true;

    const store = useAppStore.getState();
    const personas = store.personas;
    const chatHistory = store.chatHistory;

    // Format history for context
    const chatContext = chatHistory.slice(-10).map(msg => {
      if (msg.isMod) return `Mod: ${msg.text}`;
      const p = personas.find(x => x.id === msg.senderId);
      return `${p ? p.archetype : 'Unknown'}: ${msg.text}`;
    }).join('\n');

    try {
      // 1. Gather intents and engagement scores concurrently
      store.addActionLog("Analyzing group engagement...");

      const aiPromises = personas.map(async (persona) => {
        // Set visual indicator
        store.setActiveThought(persona.id, "...");

        try {
          const response = await axios.post('/api/generate', {
            prompt: promptText,
            persona: persona,
            chatContext: chatContext
          });

          return { personaId: persona.id, data: response.data };
        } catch (error) {
          console.error(`Error generating for ${persona.id}`, error);
          return null;
        }
      });

      const results = (await Promise.all(aiPromises)).filter(Boolean);

      // 2. Clear thinking states
      personas.forEach(p => store.setActiveThought(p.id, null));

      // 3. Process Skip Requests (Non-blocking: they don't halt the flow, we just log them)
      results.forEach(result => {
        if (result.data.response === '<SKIP>') {
          store.addSkipRequest(result.personaId);
          // Set a 3-second auto-pass timer as requested
          setTimeout(() => {
            const currentSkips = useAppStore.getState().skipRequests;
            if (currentSkips[result.personaId]) {
              useAppStore.getState().resolveSkipRequest(result.personaId, true); // Auto-approve
              store.addActionLog(`${personas.find(p => p.id === result.personaId)?.archetype} passed their turn.`);
            }
          }, 3000);
        }
      });

      // 4. Sort remaining responses by engagement score
      const activeResponses = results
        .filter(r => r.data.response !== '<SKIP>')
        .sort((a, b) => b.data.engagementScore - a.data.engagementScore);

      // 5. Sequence the delivery to the chat window
      for (const res of activeResponses) {
        const persona = personas.find(p => p.id === res.personaId);

        // Show thought bubble just before speaking
        store.setActiveThought(persona.id, res.data.response);

        // Wait a short duration to simulate speaking
        await new Promise(r => setTimeout(r, 2000));

        // Remove thought and add to shared frame
        store.setActiveThought(persona.id, null);
        store.addChatMessage({
          id: Date.now().toString() + res.personaId,
          senderId: res.personaId,
          text: res.data.response,
          timestamp: Date.now()
        });

        // Handle Cross-Talk injection from others
        results.forEach(otherRes => {
          if (otherRes.personaId !== res.personaId && otherRes.data.crossTalk && otherRes.data.crossTalk.trim() !== "") {
            store.addChatMessage({
              id: Date.now().toString() + otherRes.personaId + "_ct",
              senderId: otherRes.personaId,
              text: `[Asides] ${otherRes.data.crossTalk}`,
              timestamp: Date.now(),
              isCrossTalk: true
            });
            // Clear cross talk so it's not reused
            otherRes.data.crossTalk = "";
          }
        });
      }

      store.addActionLog("Round complete. Awaiting Moderator.");

    } catch (error) {
      console.error("Chat orchestration error", error);
      store.addActionLog("Error orchestrating AI responses.");
    } finally {
      this.isProcessing = false;
    }
  }
}

export const chatFlowManager = new ChatFlowManager();

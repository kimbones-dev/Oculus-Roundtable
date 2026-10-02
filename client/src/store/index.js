import { create } from 'zustand';

export const useAppStore = create((set, get) => ({
  isOffline: false,
  setOffline: (status) => set({ isOffline: status }),

  personasSetup: false,
  setPersonasSetup: (status) => set({ personasSetup: status }),

  // Array of persona objects
  personas: [],
  addPersona: (persona) => set((state) => {
    const newPersonas = [...state.personas, { ...persona, id: Date.now().toString() }];
    if (newPersonas.length > 0 && !state.personasSetup) {
      return { personas: newPersonas, personasSetup: true };
    }
    return { personas: newPersonas };
  }),
  removePersona: (id) => set((state) => {
    const newPersonas = state.personas.filter(p => p.id !== id);
    return { personas: newPersonas, personasSetup: newPersonas.length > 0 };
  }),
  updatePersona: (id, updates) => set((state) => ({
    personas: state.personas.map(p => p.id === id ? { ...p, ...updates } : p)
  })),

  // Menu State Management
  menuStack: [],
  pushMenu: (menuId) => set((state) => ({ menuStack: [...state.menuStack, menuId] })),
  popMenu: () => set((state) => ({ menuStack: state.menuStack.slice(0, -1) })),
  clearMenu: () => set({ menuStack: [] }),

  // Chat State
  chatHistory: [],
  addChatMessage: (msg) => set((state) => ({ chatHistory: [...state.chatHistory, msg] })),

  // Local UI state for individual blobs
  activeThoughts: {}, // { [personaId]: "Thinking..." }
  setActiveThought: (personaId, text) => set((state) => ({
    activeThoughts: { ...state.activeThoughts, [personaId]: text }
  })),

  skipRequests: {}, // { [personaId]: timestamp }
  addSkipRequest: (personaId) => set((state) => ({
    skipRequests: { ...state.skipRequests, [personaId]: Date.now() }
  })),
  resolveSkipRequest: (personaId, approved) => set((state) => {
    const newSkips = { ...state.skipRequests };
    delete newSkips[personaId];
    return { skipRequests: newSkips };
  }),

  // Action log for Mod actions, skip notifications
  actionLog: [],
  addActionLog: (text) => set((state) => ({
    actionLog: [...state.actionLog, { id: Date.now().toString(), text, timestamp: Date.now() }]
  })),

  roundSettings: {
    pacing: 'medium',
    roundGoals: ''
  },
  updateRoundSettings: (updates) => set((state) => ({ roundSettings: { ...state.roundSettings, ...updates } })),
}));

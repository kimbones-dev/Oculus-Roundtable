import React, { useState, useRef, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useAppStore } from '../../store';

export function GlobalChatWindow() {
  const { chatHistory, personasSetup, addChatMessage, personas } = useAppStore();
  const [inputText, setInputText] = useState('');
  const messagesEndRef = useRef(null);

  // Draggable window state
  const [position, setPosition] = useState({ x: 20, y: 20 });
  const dragRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatHistory]);

  if (!personasSetup) return null;

  const handleSend = async (e) => {
    e.preventDefault();
    if (!inputText.trim()) return;

    const textToProcess = inputText;

    // Add Mod's message to history
    addChatMessage({
      id: Date.now().toString(),
      senderId: 'mod',
      text: textToProcess,
      isMod: true,
      timestamp: Date.now()
    });

    setInputText('');

    // Import dynamically to avoid circular dependencies if any
    const { chatFlowManager } = await import('../../utils/chatFlow');
    chatFlowManager.processModInput(textToProcess);
  };

  return (
    <motion.div
      drag
      dragControls={dragRef}
      dragMomentum={false}
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      className="absolute z-40 flex flex-col bg-black/40 backdrop-blur-md border border-white/10 rounded-xl shadow-2xl resize overflow-hidden min-w-[300px] min-h-[400px] max-w-[600px] max-h-[80vh]"
      style={{ x: position.x, y: position.y, width: 400, height: 600 }}
    >
      {/* Drag Handle & Header */}
      <div className="w-full px-4 py-2 bg-white/5 border-b border-white/10 flex items-center justify-between cursor-move" onPointerDown={(e) => dragRef.current?.start(e)}>
        <span className="text-sm font-light uppercase tracking-widest text-white/70">Shared Frame</span>
        <div className="flex gap-2">
          <div className="w-2 h-2 rounded-full bg-white/20"></div>
          <div className="w-2 h-2 rounded-full bg-white/20"></div>
          <div className="w-2 h-2 rounded-full bg-white/20"></div>
        </div>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3 custom-scrollbar">
        {chatHistory.length === 0 && (
          <div className="text-white/30 text-center text-sm my-auto">The frame is empty. Awaiting Mod input.</div>
        )}
        {chatHistory.map((msg) => {
          const isMod = msg.senderId === 'mod';
          const persona = personas.find(p => p.id === msg.senderId);

          return (
            <div key={msg.id} className={`flex flex-col ${isMod ? 'items-end' : 'items-start'}`}>
              {!isMod && persona && (
                <span className="text-xs mb-1 opacity-60" style={{ color: persona.color }}>
                  {persona.archetype} ({persona.professionCategory})
                </span>
              )}
              {isMod && <span className="text-xs mb-1 opacity-60 text-white">Moderator</span>}

              <div
                className={`px-4 py-2 rounded-2xl max-w-[85%] text-sm ${
                  isMod
                    ? 'bg-white/20 text-white rounded-br-none'
                    : 'bg-black/60 border text-white rounded-bl-none'
                }`}
                style={!isMod && persona ? { borderColor: `${persona.color}40`, boxShadow: `0 0 10px ${persona.color}10` } : {}}
              >
                {msg.text}
              </div>
            </div>
          );
        })}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <form onSubmit={handleSend} className="p-3 bg-white/5 border-t border-white/10 flex gap-2">
        <input
          type="text"
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          placeholder="Command the frame..."
          className="flex-1 bg-black/40 border border-white/20 rounded-full px-4 py-2 text-sm text-white placeholder-white/30 outline-none focus:border-white/50 transition-colors"
        />
        <button
          type="submit"
          className="w-10 h-10 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors"
        >
          <span className="transform rotate-90 inline-block">➔</span>
        </button>
      </form>
    </motion.div>
  );
}

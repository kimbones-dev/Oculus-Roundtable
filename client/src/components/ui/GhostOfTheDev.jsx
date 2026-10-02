import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useAppStore } from '../../store';

export function GhostOfTheDev() {
  const isOffline = useAppStore(state => state.isOffline);
  const personasSetup = useAppStore(state => state.personasSetup);

  const [message, setMessage] = useState('');

  useEffect(() => {
    if (isOffline) {
      setMessage('The connection is lost. Awaiting restoration...');
    } else if (!personasSetup) {
      setMessage("Let's assemble the team");
    } else {
      setMessage('');
    }
  }, [isOffline, personasSetup]);

  // If no message, render nothing
  if (!message) return null;

  return (
    <div className="absolute inset-0 pointer-events-none flex flex-col items-center justify-center z-10 font-sans">
      <AnimatePresence mode="wait">
        <motion.div
          key={message}
          initial={{ opacity: 0, filter: 'blur(20px)', scale: 1.1 }}
          animate={{ opacity: 0.8, filter: 'blur(2px)', scale: 1 }}
          exit={{ opacity: 0, filter: 'blur(30px)', scale: 0.9 }}
          transition={{ duration: 4, ease: "easeInOut" }}
          className="text-white text-5xl tracking-[0.5em] font-light uppercase mix-blend-screen text-center"
          style={{ textShadow: '0 0 20px rgba(255,255,255,0.5)' }}
        >
          {message}
        </motion.div>
      </AnimatePresence>

      {/* Smoky Arrow to lozenge if not setup and not offline */}
      {!personasSetup && !isOffline && (
        <motion.div
          initial={{ opacity: 0, y: -20, filter: 'blur(10px)' }}
          animate={{ opacity: 0.6, y: 0, filter: 'blur(2px)' }}
          transition={{ duration: 3, delay: 3, repeat: Infinity, repeatType: 'reverse' }}
          className="mt-16 text-white text-4xl transform rotate-90"
        >
          ➔
        </motion.div>
      )}
    </div>
  );
}

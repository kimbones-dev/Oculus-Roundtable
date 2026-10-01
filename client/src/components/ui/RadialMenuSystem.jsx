import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useAppStore } from '../../store';
import { TAXONOMY } from '../../utils/taxonomy';
import { PersonaBuilderForm } from './PersonaBuilderForm';

// Helper to calculate depth blur
const getMenuStyles = (index, total) => {
  const isTop = index === total - 1;
  const depth = total - 1 - index;
  return {
    opacity: isTop ? 1 : Math.max(0.2, 1 - depth * 0.4),
    filter: isTop ? 'blur(0px)' : `blur(${depth * 4}px)`,
    scale: isTop ? 1 : 1 - depth * 0.1,
    zIndex: 100 + index,
  };
};

function MenuButton({ onClick, children }) {
  return (
    <button
      onClick={onClick}
      className="block w-full text-left px-6 py-3 text-lg hover:bg-white/10 hover:text-white transition-colors border-b border-white/5 last:border-0"
    >
      {children}
    </button>
  );
}

function MenuPanel({ title, onBack, children, style }) {
  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: style.opacity, scale: style.scale, filter: style.filter }}
      exit={{ opacity: 0, scale: 0.95 }}
      transition={{ duration: 0.3 }}
      className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 bg-black/60 backdrop-blur-md border border-white/20 rounded-2xl overflow-hidden shadow-2xl flex flex-col"
      style={{ ...style, maxHeight: '80vh' }}
    >
      <div className="flex items-center justify-between px-6 py-4 bg-white/5 border-b border-white/10">
        <h2 className="text-xl font-light uppercase tracking-widest">{title}</h2>
        {onBack && (
          <button onClick={onBack} className="text-sm opacity-60 hover:opacity-100 uppercase tracking-wider">
            Back
          </button>
        )}
      </div>
      <div className="overflow-y-auto custom-scrollbar p-2 flex-1">
        {children}
      </div>
    </motion.div>
  );
}

export function RadialMenuSystem() {
  const { menuStack, pushMenu, popMenu, clearMenu, personas, addPersona } = useAppStore();

  if (menuStack.length === 0) return null;

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) clearMenu();
  };

  const renderMenuContent = (menuId) => {
    switch (menuId) {
      case 'main':
        return (
          <>
            <MenuButton onClick={() => pushMenu('personas')}>Persona Editor</MenuButton>
            <MenuButton onClick={() => pushMenu('round_controls')}>Round Controls</MenuButton>
            <MenuButton onClick={() => pushMenu('visuals')}>Visual Customization</MenuButton>
            <MenuButton onClick={() => pushMenu('audio')}>Audio Tracks</MenuButton>
            <MenuButton onClick={clearMenu}>Close Menu</MenuButton>
          </>
        );
      case 'personas':
        return (
          <>
            {personas.map(p => (
              <div key={p.id} className="px-6 py-3 border-b border-white/5 flex justify-between items-center">
                <span>{p.archetype} ({p.professionCategory})</span>
              </div>
            ))}
            {personas.length < 7 && (
              <MenuButton onClick={() => pushMenu('add_persona')}>+ Add New Persona</MenuButton>
            )}
          </>
        );
      case 'add_persona':
        return <PersonaBuilderForm onComplete={popMenu} />;
      case 'round_controls':
        return <p className="p-6 opacity-50 text-center">Round settings...</p>;
      case 'visuals':
        return <p className="p-6 opacity-50 text-center">Visual settings...</p>;
      case 'audio':
        return <p className="p-6 opacity-50 text-center">Audio settings...</p>;
      default:
        return null;
    }
  };

  const renderMenuTitle = (menuId) => {
    return menuId.replace('_', ' ');
  };

  return (
    <div
      className="absolute inset-0 z-50 bg-black/20 backdrop-blur-sm transition-all duration-500"
      onClick={handleBackdropClick}
    >
      <AnimatePresence>
        {menuStack.map((menuId, index) => {
          const style = getMenuStyles(index, menuStack.length);
          return (
            <MenuPanel
              key={`${menuId}-${index}`}
              title={renderMenuTitle(menuId)}
              onBack={index > 0 ? popMenu : null}
              style={style}
            >
              {renderMenuContent(menuId)}
            </MenuPanel>
          );
        })}
      </AnimatePresence>
    </div>
  );
}

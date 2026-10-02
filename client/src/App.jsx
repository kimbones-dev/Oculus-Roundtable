import React, { Suspense, useEffect } from 'react';
import { Canvas } from '@react-three/fiber';
import { Environment, Stars } from '@react-three/drei';
import { CentralLozenge } from './components/scene/CentralLozenge';
import { BlobArcLayout } from './components/scene/BlobArcLayout';
import { GhostOfTheDev } from './components/ui/GhostOfTheDev';
import { RadialMenuSystem } from './components/ui/RadialMenuSystem';
import { GlobalChatWindow } from './components/ui/GlobalChatWindow';
import { useAppStore } from './store';

function App() {
  const pushMenu = useAppStore(state => state.pushMenu);
  const setOffline = useAppStore(state => state.setOffline);

  useEffect(() => {
    const handleOnline = () => setOffline(false);
    const handleOffline = () => setOffline(true);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Initial check
    setOffline(!navigator.onLine);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [setOffline]);

  const handleLozengeClick = () => {
    pushMenu('main');
  };

  // Expose store to window for testing
  useEffect(() => {
    window.__pushMenu = pushMenu;
  }, [pushMenu]);

  return (
    <div className="w-full h-full bg-black relative overflow-hidden">
      <GhostOfTheDev />

      <RadialMenuSystem />

      <GlobalChatWindow />

      <Canvas
        camera={{ position: [0, 0, 8], fov: 45 }}
        gl={{ antialias: true, alpha: false }}
        className="absolute inset-0 z-0"
      >
        {/* Chromatic black vacuum feeling */}
        <color attach="background" args={['#030305']} />
        <fog attach="fog" args={['#030305', 5, 20]} />

        {/* Subtle starfield/dust for depth without clutter */}
        <Stars radius={100} depth={50} count={2000} factor={2} saturation={0} fade speed={1} />

        <Suspense fallback={null}>
          <CentralLozenge onClick={handleLozengeClick} />
          <BlobArcLayout />
        </Suspense>
      </Canvas>
    </div>
  );
}

export default App;

import React, { useRef, useMemo, useState } from 'react';
import { useFrame } from '@react-three/fiber';
import { MeshDistortMaterial, Html } from '@react-three/drei';
import * as THREE from 'three';
import { useAppStore } from '../../store';
import { AnimatePresence, motion } from 'framer-motion';

export function AIBlob({ persona, position }) {
  const blobRef = useRef();
  const materialRef = useRef();
  const lightRef = useRef();
  const eyesGroupRef = useRef();

  // Randomize initial idle movement offset
  const randomOffset = useMemo(() => Math.random() * 100, []);

  const color = new THREE.Color(persona.color || '#ff00ff');

  const activeThought = useAppStore(state => state.activeThoughts[persona.id]);
  const skipRequest = useAppStore(state => state.skipRequests[persona.id]);
  const resolveSkipRequest = useAppStore(state => state.resolveSkipRequest);

  useFrame((state) => {
    const t = state.clock.elapsedTime + randomOffset;

    if (blobRef.current) {
      // Loose, floating motion
      blobRef.current.position.y = position[1] + Math.sin(t * 0.5) * 0.2;
      blobRef.current.position.x = position[0] + Math.cos(t * 0.3) * 0.1;
      blobRef.current.position.z = position[2] + Math.sin(t * 0.4) * 0.1;

      // Slight rotation
      blobRef.current.rotation.y = Math.sin(t * 0.2) * 0.2;
    }

    // Animate eyes slightly (blinking logic can be added later)
    if (eyesGroupRef.current) {
       // Look around loosely
       eyesGroupRef.current.position.x = Math.sin(t) * 0.05;
       eyesGroupRef.current.position.y = Math.cos(t * 1.5) * 0.05;
    }
  });

  return (
    <group ref={blobRef} position={position}>
      {/* The Amorphous Blob Body */}
      <mesh castShadow receiveShadow>
        <sphereGeometry args={[1, 64, 64]} />
        <MeshDistortMaterial
          ref={materialRef}
          color={color}
          envMapIntensity={0.5}
          clearcoat={0.8}
          clearcoatRoughness={0.2}
          metalness={0.1}
          roughness={0.4}
          distort={0.4} // Distort factor
          speed={2} // Animation speed
          transmission={0.5}
          opacity={0.9}
          transparent
        />
      </mesh>

      {/* Soft Internal Glow */}
      <pointLight ref={lightRef} color={color} intensity={2} distance={3} />

      {/* Pixel Art Eyes Container */}
      <group ref={eyesGroupRef} position={[0, 0, 0.9]}>
        {/* Left Eye */}
        <mesh position={[-0.3, 0.2, 0]}>
          <planeGeometry args={[0.2, 0.2]} />
          <meshBasicMaterial color="#ffffff" transparent opacity={0.9} side={THREE.DoubleSide} />
          {/* Pupil */}
          <mesh position={[0, 0, 0.01]}>
             <planeGeometry args={[0.08, 0.08]} />
             <meshBasicMaterial color="#000000" />
          </mesh>
        </mesh>
        {/* Right Eye */}
        <mesh position={[0.3, 0.2, 0]}>
          <planeGeometry args={[0.2, 0.2]} />
          <meshBasicMaterial color="#ffffff" transparent opacity={0.9} side={THREE.DoubleSide} />
           {/* Pupil */}
          <mesh position={[0, 0, 0.01]}>
             <planeGeometry args={[0.08, 0.08]} />
             <meshBasicMaterial color="#000000" />
          </mesh>
        </mesh>
      </group>

      <Html position={[0, 1.5, 0]} center zIndexRange={[100, 0]}>
        <AnimatePresence>
          {activeThought && (
            <motion.div
              initial={{ opacity: 0, y: 10, scale: 0.8 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -10, scale: 0.8 }}
              className="bg-black/60 backdrop-blur-md border border-white/20 text-white px-4 py-2 rounded-2xl max-w-[250px] shadow-2xl"
              style={{
                boxShadow: `0 0 20px ${persona.color}40`,
                textShadow: '0 2px 4px rgba(0,0,0,0.5)'
              }}
            >
              <div className="max-h-32 overflow-y-auto custom-scrollbar text-sm pointer-events-auto">
                {activeThought}
              </div>
            </motion.div>
          )}

          {skipRequest && !activeThought && (
            <motion.div
              initial={{ opacity: 0, scale: 0 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0 }}
              className="mt-2 flex gap-2 pointer-events-auto"
            >
              <button
                onClick={(e) => { e.stopPropagation(); resolveSkipRequest(persona.id, true); }}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-green-500/50 border border-white/20 flex items-center justify-center transition-colors backdrop-blur-sm"
              >
                👍
              </button>
              <button
                onClick={(e) => { e.stopPropagation(); resolveSkipRequest(persona.id, false); }}
                className="w-8 h-8 rounded-full bg-white/10 hover:bg-red-500/50 border border-white/20 flex items-center justify-center transition-colors backdrop-blur-sm"
              >
                👎
              </button>
            </motion.div>
          )}
        </AnimatePresence>
      </Html>
    </group>
  );
}

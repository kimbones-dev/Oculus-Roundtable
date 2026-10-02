import React, { useRef, useMemo } from 'react';
import { useFrame } from '@react-three/fiber';
import { useAppStore } from '../../store';
import * as THREE from 'three';
import { Html } from '@react-three/drei';
import { motion } from 'framer-motion';

export function CentralLozenge({ onClick }) {
  const meshRef = useRef();
  const materialRef = useRef();

  // A vertical oval with a depressed center
  const geometry = useMemo(() => {
    // Create an extruded shape or a modified cylinder
    const shape = new THREE.Shape();
    shape.moveTo(0, 1.5);
    shape.bezierCurveTo(0.8, 1.5, 0.8, -1.5, 0, -1.5);
    shape.bezierCurveTo(-0.8, -1.5, -0.8, 1.5, 0, 1.5);

    const extrudeSettings = {
      depth: 0.2,
      bevelEnabled: true,
      bevelSegments: 2,
      steps: 2,
      bevelSize: 0.1,
      bevelThickness: 0.1,
    };
    const geom = new THREE.ExtrudeGeometry(shape, extrudeSettings);
    geom.center();

    // Create depression in the center (cheap approach: scale vertices near center)
    const posAttribute = geom.attributes.position;
    for (let i = 0; i < posAttribute.count; i++) {
        const x = posAttribute.getX(i);
        const y = posAttribute.getY(i);
        const z = posAttribute.getZ(i);

        // If near center, push Z inwards
        const distToCenter = Math.sqrt(x*x + y*y);
        if (distToCenter < 0.6) {
            // Modify Z based on side (front or back)
            const pushAmt = (0.6 - distToCenter) * 0.3;
            if (z > 0) posAttribute.setZ(i, z - pushAmt);
            else posAttribute.setZ(i, z + pushAmt);
        }
    }
    geom.computeVertexNormals();
    return geom;
  }, []);

  useFrame((state, delta) => {
    if (meshRef.current) {
        // Slow float
        meshRef.current.position.y = Math.sin(state.clock.elapsedTime) * 0.1 - 2;
    }
    if (materialRef.current) {
        // Pulse opacity slightly
        materialRef.current.opacity = 0.6 + Math.sin(state.clock.elapsedTime * 2) * 0.1;
    }
  });

  return (
    <mesh
        ref={meshRef}
        geometry={geometry}
        position={[0, -2, 0]}
        onClick={(e) => {
            e.stopPropagation();
            onClick();
        }}
    >
      <meshPhysicalMaterial
        ref={materialRef}
        color="#ffffff"
        transmission={0.9}
        opacity={0.8}
        transparent
        roughness={0.1}
        thickness={0.5}
        emissive="#ffffff"
        emissiveIntensity={0.2}
      />
      {/* Light coming from the lozenge */}
      <pointLight color="#ffffff" intensity={2} distance={10} position={[0, 0, 0]} />
    </mesh>
  );
}

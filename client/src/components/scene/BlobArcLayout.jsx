import React, { useMemo } from 'react';
import { useAppStore } from '../../store';
import { AIBlob } from './AIBlob';

export function BlobArcLayout() {
  const personas = useAppStore(state => state.personas);

  // Calculate positions in an arc above and around the lozenge
  // The lozenge is at [0, -2, 0]
  const blobPositions = useMemo(() => {
    const positions = [];
    const count = personas.length;
    if (count === 0) return positions;

    const radiusX = 5;
    const radiusY = 2;
    const radiusZ = 3;

    // Spread them across an arc
    // Angle goes from PI/4 to 3*PI/4 (or spread based on count)
    const arcSpread = Math.PI * 0.6;
    const startAngle = Math.PI / 2 - (arcSpread / 2);
    const step = count > 1 ? arcSpread / (count - 1) : 0;

    for (let i = 0; i < count; i++) {
      const angle = startAngle + i * step;

      // X position along the arc
      const x = Math.cos(angle) * radiusX;
      // Z position to give depth (curve back slightly)
      const z = -Math.sin(angle) * radiusZ + 1;
      // Y position (height)
      const y = Math.sin(angle) * radiusY;

      positions.push([x, y, z]);
    }

    return positions;
  }, [personas.length]);

  return (
    <>
      {personas.map((persona, i) => (
        <AIBlob key={persona.id} persona={persona} position={blobPositions[i]} />
      ))}
    </>
  );
}

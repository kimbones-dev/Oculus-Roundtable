import React, { useState } from 'react';
import { TAXONOMY } from '../../utils/taxonomy';
import { useAppStore } from '../../store';

export function PersonaBuilderForm({ onComplete }) {
  const addPersona = useAppStore(state => state.addPersona);

  const [archetype, setArchetype] = useState(TAXONOMY.archetypes[0]);
  const [professionCategory, setProfessionCategory] = useState(Object.keys(TAXONOMY.professions)[0]);
  const [profession, setProfession] = useState(TAXONOMY.professions[Object.keys(TAXONOMY.professions)[0]][0]);
  const [lens, setLens] = useState(TAXONOMY.personalLenses[0]);
  const [skillLevel, setSkillLevel] = useState(TAXONOMY.skillLevels[2]);
  const [tone, setTone] = useState(TAXONOMY.tones[0]);
  const [color, setColor] = useState('#ff00ff');

  const handleCategoryChange = (e) => {
    const cat = e.target.value;
    setProfessionCategory(cat);
    setProfession(TAXONOMY.professions[cat][0]);
  };

  const handleSave = () => {
    addPersona({
      archetype,
      professionCategory,
      profession,
      lens,
      skillLevel,
      tone,
      color
    });
    onComplete();
  };

  return (
    <div className="p-4 flex flex-col gap-3 text-sm">
      <div>
        <label className="block text-white/50 mb-1">Archetype</label>
        <select value={archetype} onChange={e => setArchetype(e.target.value)} className="w-full bg-black/40 border border-white/20 rounded p-2 text-white">
          {TAXONOMY.archetypes.map(a => <option key={a} value={a}>{a}</option>)}
        </select>
      </div>
      <div>
        <label className="block text-white/50 mb-1">Profession Domain</label>
        <select value={professionCategory} onChange={handleCategoryChange} className="w-full bg-black/40 border border-white/20 rounded p-2 text-white">
          {Object.keys(TAXONOMY.professions).map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>
      <div>
        <label className="block text-white/50 mb-1">Specialization</label>
        <select value={profession} onChange={e => setProfession(e.target.value)} className="w-full bg-black/40 border border-white/20 rounded p-2 text-white">
          {TAXONOMY.professions[professionCategory].map(p => <option key={p} value={p}>{p}</option>)}
        </select>
      </div>
      <div>
        <label className="block text-white/50 mb-1">Personal Lens</label>
        <select value={lens} onChange={e => setLens(e.target.value)} className="w-full bg-black/40 border border-white/20 rounded p-2 text-white">
          {TAXONOMY.personalLenses.map(l => <option key={l} value={l}>{l}</option>)}
        </select>
      </div>
      <div className="flex gap-2">
        <div className="flex-1">
            <label className="block text-white/50 mb-1">Skill</label>
            <select value={skillLevel} onChange={e => setSkillLevel(e.target.value)} className="w-full bg-black/40 border border-white/20 rounded p-2 text-white">
            {TAXONOMY.skillLevels.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
        </div>
        <div className="flex-1">
            <label className="block text-white/50 mb-1">Tone</label>
            <select value={tone} onChange={e => setTone(e.target.value)} className="w-full bg-black/40 border border-white/20 rounded p-2 text-white">
            {TAXONOMY.tones.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
        </div>
      </div>
      <div>
        <label className="block text-white/50 mb-1">Aura Color</label>
        <input type="color" value={color} onChange={e => setColor(e.target.value)} className="w-full h-8 bg-transparent border-none rounded cursor-pointer" />
      </div>

      <button onClick={handleSave} className="mt-2 w-full py-2 bg-white/20 hover:bg-white/30 rounded transition-colors uppercase tracking-wider text-xs">
        Manifest Persona
      </button>
    </div>
  );
}

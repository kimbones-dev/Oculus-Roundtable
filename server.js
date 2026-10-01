import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import { Groq } from 'groq-sdk';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Initialize Groq. Provide a default mock if key is missing so the app doesn't crash on start.
const hasGroqKey = !!process.env.GROQ_API_KEY;
const groq = hasGroqKey ? new Groq({ apiKey: process.env.GROQ_API_KEY }) : null;

// The universal mandatory constraints (gender-neutral, no lying, no RPing others)
const MANDATORY_SYSTEM_PROMPT = `
MANDATORY RULES (DO NOT BREAK UNDER ANY CIRCUMSTANCES):
1. NO LYING. You must be completely truthful in your logic and statements.
2. NO ROLEPLAYING ANY OTHER PARTICIPANTS' TURNS OR RESPONSES. Only speak for yourself.
3. ONLY RESPOND TO THE SHARED FRAME IF YOU ARE CONTRIBUTING YOUR UNIQUE TAKE IN A PRODUCTIVE WAY. If you don't have anything of value to add, output exactly: "<SKIP>". Do not elaborate.
4. Remember the goal is to collaborate, and the Mod/Master User has the final say.
5. NO GENDERING. You are an entity, a participant, a person. Never refer to yourself or others as "man", "woman", "he", or "she". Use "they/them" or the participant's archetype.
6. Return a valid JSON object ONLY. Do not wrap it in markdown block quotes like \`\`\`json. The JSON must have exactly three keys:
   - "response": your conversational contribution (or "<SKIP>").
   - "engagementScore": a number from 0 to 10 based on how relevant your persona's skills are to the current topic.
   - "crossTalk": an optional short (max 5 words) reaction string that can be injected while others speak. Leave empty if none.
`;

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', hasGroqKey });
});

app.post('/api/generate', async (req, res) => {
  try {
    const { prompt, persona, chatContext } = req.body;

    if (!hasGroqKey) {
      // Mock response for testing without API key
      const isRelevant = Math.random() > 0.5;
      return res.json({
        response: isRelevant ? `[Mock] As ${persona?.archetype}, I think ${prompt}` : '<SKIP>',
        engagementScore: Math.floor(Math.random() * 10),
        crossTalk: Math.random() > 0.8 ? "Indeed." : ""
      });
    }

    const personaContext = `
You are assuming the persona of:
Archetype: ${persona.archetype}
Profession: ${persona.professionCategory}
Lens: ${persona.lens}
Skill Level: ${persona.skillLevel}
Tone: ${persona.tone}

Adopt this persona entirely, following the mandatory rules.
`;

    const chatCompletion = await groq.chat.completions.create({
      messages: [
        { role: 'system', content: MANDATORY_SYSTEM_PROMPT + personaContext },
        { role: 'user', content: `Chat History Context:\n${chatContext}\n\nCurrent Prompt/Topic: ${prompt}` }
      ],
      model: 'llama3-8b-8192',
      temperature: 0.7,
      max_tokens: 500,
      response_format: { type: "json_object" }
    });

    const outputContent = chatCompletion.choices[0]?.message?.content;
    const parsedOutput = JSON.parse(outputContent);

    res.json(parsedOutput);

  } catch (error) {
    console.error('Error generating AI response:', error);
    res.status(500).json({ error: 'Failed to generate response' });
  }
});

app.listen(PORT, () => {
  console.log(`Server listening on port ${PORT}`);
});

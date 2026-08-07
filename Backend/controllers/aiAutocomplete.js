const express = require('express');
const { GoogleGenerativeAI } = require('@google/generative-ai');

const router = express.Router();

// LLM-powered autocomplete: returns a single suggested completion for given text.
router.post('/autocomplete', async (req, res) => {
  const text = (req.body?.text || '').trim();
  if (!text) return res.json({ suggestion: '' });

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    // Fallback suggestion without calling an external model
    return res.json({ suggestion: `${text} for a follow-up consultation` });
  }

  try {
    const genAI = new GoogleGenerativeAI(apiKey, { apiVersion: 'v1' });
    const model = genAI.getGenerativeModel({ model: 'gemini-1.5-flash-latest' });
    const prompt = `Complete the following user input naturally. Return ONLY the completed sentence. Do not explain.\nUser input: "${text}"`;

    const result = await model.generateContent(prompt);
    const suggestion = result?.response?.text?.()?.trim() || '';
    res.json({ suggestion });
  } catch (err) {
    console.error('Autocomplete error', err.message);
    res.status(500).json({ suggestion: '', error: 'autocomplete_failed' });
  }
});

module.exports = router;

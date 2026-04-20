const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const nodemailer = require('nodemailer');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// Connect to MongoDB
mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log('MongoDB connected'))
  .catch((err) => console.error('MongoDB connection error:', err));

// Place Schema
const placeSchema = new mongoose.Schema({
  name:             { type: String, required: true },
  category:         { type: String, required: true },
  lat:              { type: Number, required: true },
  lng:              { type: Number, required: true },
  weatherCondition: { type: String, required: true },
  rating:           { type: Number, default: 0 },
});

const Place = mongoose.model('Place', placeSchema);

// In-memory reset code store: email -> { code, expiresAt }
const resetCodes = new Map();

// GET /places?weather=Rain
app.get('/places', async (req, res) => {
  try {
    const { weather } = req.query;
    if (!weather) {
      return res.status(400).json({ error: 'weather query param is required' });
    }
    const places = await Place.find({ weatherCondition: weather });
    res.json(places);
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

// POST /favorites
app.post('/favorites', async (req, res) => {
  try {
    const { name, category, lat, lng, weatherCondition, rating } = req.body;
    if (!name || !category || lat == null || lng == null || !weatherCondition) {
      return res.status(400).json({ error: 'Missing required fields' });
    }
    const place = new Place({ name, category, lat, lng, weatherCondition, rating });
    await place.save();
    res.status(201).json(place);
  } catch (err) {
    res.status(500).json({ error: 'Server error' });
  }
});

// POST /send-reset-code — generates a 6-digit code and emails it
app.post('/send-reset-code', async (req, res) => {
  const { email } = req.body;
  if (!email) return res.status(400).json({ error: 'Email is required' });

  const code = Math.floor(100000 + Math.random() * 900000).toString();
  const expiresAt = Date.now() + 10 * 60 * 1000; // 10 minutes
  resetCodes.set(email.toLowerCase(), { code, expiresAt });

  const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
      user: process.env.EMAIL_USER,
      pass: process.env.EMAIL_PASS,
    },
  });

  try {
    await transporter.sendMail({
      from: `"WeatherMood" <${process.env.EMAIL_USER}>`,
      to: email,
      subject: 'WeatherMood — Password Reset Code',
      text: `Your verification code is: ${code}\n\nThis code expires in 10 minutes.\nIf you did not request this, ignore this email.`,
    });
    res.json({ success: true });
  } catch (err) {
    console.error('Email send error:', err);
    res.status(500).json({ error: 'Failed to send email' });
  }
});

// POST /verify-reset-code — checks code validity
app.post('/verify-reset-code', (req, res) => {
  const { email, code } = req.body;
  if (!email || !code) return res.status(400).json({ error: 'Email and code are required' });

  const entry = resetCodes.get(email.toLowerCase());
  if (!entry) return res.status(400).json({ error: 'No code found for this email' });
  if (Date.now() > entry.expiresAt) {
    resetCodes.delete(email.toLowerCase());
    return res.status(400).json({ error: 'Code has expired' });
  }
  if (entry.code !== code) return res.status(400).json({ error: 'Invalid code' });

  resetCodes.delete(email.toLowerCase());
  res.json({ success: true });
});

// Health check
app.get('/', (req, res) => {
  res.json({ status: 'WeatherMood API is running' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));

const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
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
  weatherCondition: { type: String, required: true }, // e.g. "Rain", "Clear", "Clouds"
  rating:           { type: Number, default: 0 },
});

const Place = mongoose.model('Place', placeSchema);

// GET /places?weather=Rain
// Returns all places matching the given weather condition
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
// Saves a new place to the database
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

// Health check
app.get('/', (req, res) => {
  res.json({ status: 'WeatherMood API is running' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));

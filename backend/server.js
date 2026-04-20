const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// PostgreSQL connection
const pool = new Pool({
  user: 'postgres',
  host: 'db',
  database: 'event_management',
  password: '1234',  // put your password
  port: 5432,
});

// Test route
app.get('/', (req, res) => {
  res.send('Server is running');
});

// Fetch users
app.get('/users', async (req, res) => {
  const result = await pool.query('SELECT * FROM users');
  res.json(result.rows);
});
// Fetch bookings
app.get('/api/bookings', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM bookings');
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Error fetching bookings');
  }
});

app.listen(5000, () => {
  console.log('Server running on port 5000');
});
// Fetch events
app.get('/api/events', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM events');
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Error fetching events');
  }
});

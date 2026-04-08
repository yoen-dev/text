/**
 * NourishWell — Express Server
 * 
 * Endpoints:
 *   POST /api/auth/register  — create account
 *   POST /api/auth/login     — sign in, returns JWT
 *   GET  /api/auth/me        — verify token, returns user info
 * 
 * Test backdoor accounts (no registration needed):
 *   Subscriber  →  test@user.com       / test1234
 *   Pro         →  test@pro.com        / test1234
 */

const express = require('express');
const bcrypt  = require('bcryptjs');
const jwt     = require('jsonwebtoken');
const cors    = require('cors');
const path    = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'nourishwell-dev-secret-change-in-production';

app.use(cors());
app.use(express.json());

// ── Serve static frontend files ───────────────────────────────────────────────
app.use(express.static(path.join(__dirname, '../public')));

// ── In-memory user store (replace with DB later) ─────────────────────────────
// Passwords are bcrypt hashed. Hash for 'test1234':
const HASH_TEST = bcrypt.hashSync('test1234', 10);

const users = [
  // ── BACKDOOR TEST ACCOUNTS ────────────────────────────────────────────────
  {
    id: 'usr_test_subscriber',
    email: 'test@user.com',
    passwordHash: HASH_TEST,
    firstName: 'Rose',
    lastName: 'Campbell',
    role: 'subscriber',        // → redirects to /dashboard.html
    backdoor: true,
  },
  {
    id: 'usr_test_pro',
    email: 'test@pro.com',
    passwordHash: HASH_TEST,
    firstName: 'Dr.',
    lastName: 'Rivera',
    role: 'professional',      // → redirects to /pro_dashboard.html
    backdoor: true,
  },
];

// ── Helpers ───────────────────────────────────────────────────────────────────
function signToken(user) {
  return jwt.sign(
    { id: user.id, email: user.email, role: user.role, firstName: user.firstName },
    JWT_SECRET,
    { expiresIn: '7d' }
  );
}

function authMiddleware(req, res, next) {
  const header = req.headers.authorization;
  if (!header || !header.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'No token provided' });
  }
  try {
    req.user = jwt.verify(header.slice(7), JWT_SECRET);
    next();
  } catch {
    res.status(401).json({ error: 'Invalid or expired token' });
  }
}

// ── POST /api/auth/register ───────────────────────────────────────────────────
app.post('/api/auth/register', async (req, res) => {
  const { firstName, lastName, email, password, role, licenceNumber } = req.body;

  // Basic validation
  if (!firstName || !lastName || !email || !password) {
    return res.status(400).json({ error: 'All fields are required' });
  }
  if (password.length < 8) {
    return res.status(400).json({ error: 'Password must be at least 8 characters' });
  }
  if (role === 'professional' && !licenceNumber) {
    return res.status(400).json({ error: 'Licence number required for professionals' });
  }

  // Check duplicate email
  if (users.find(u => u.email.toLowerCase() === email.toLowerCase())) {
    return res.status(409).json({ error: 'An account with this email already exists' });
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const newUser = {
    id: 'usr_' + Date.now(),
    email: email.toLowerCase(),
    passwordHash,
    firstName,
    lastName,
    role: role || 'subscriber',
    licenceNumber: licenceNumber || null,
    createdAt: new Date().toISOString(),
  };
  users.push(newUser);

  const token = signToken(newUser);
  res.status(201).json({
    token,
    user: {
      id: newUser.id,
      email: newUser.email,
      firstName: newUser.firstName,
      lastName: newUser.lastName,
      role: newUser.role,
    },
    redirectTo: newUser.role === 'professional' ? '/pro_dashboard.html' : '/dashboard.html',
  });
});

// ── POST /api/auth/login ──────────────────────────────────────────────────────
app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }

  const user = users.find(u => u.email.toLowerCase() === email.toLowerCase());
  if (!user) {
    return res.status(401).json({ error: 'Incorrect email or password' });
  }

  const valid = await bcrypt.compare(password, user.passwordHash);
  if (!valid) {
    return res.status(401).json({ error: 'Incorrect email or password' });
  }

  const token = signToken(user);
  res.json({
    token,
    user: {
      id: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      role: user.role,
    },
    redirectTo: user.role === 'professional' ? '/pro_dashboard.html' : '/dashboard.html',
  });
});

// ── GET /api/auth/me ──────────────────────────────────────────────────────────
app.get('/api/auth/me', authMiddleware, (req, res) => {
  const user = users.find(u => u.id === req.user.id);
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json({
    id: user.id,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    role: user.role,
  });
});

// ── GET /api/auth/backdoor-info ───────────────────────────────────────────────
// Shows test accounts in development only — remove before real deployment
app.get('/api/auth/backdoor-info', (req, res) => {
  res.json({
    note: 'Development backdoor accounts — remove before production',
    accounts: [
      { email: 'test@user.com', password: 'test1234', role: 'subscriber', redirectTo: '/dashboard.html' },
      { email: 'test@pro.com',  password: 'test1234', role: 'professional', redirectTo: '/pro_dashboard.html' },
    ],
  });
});

// ── Fallback: serve index.html for any unmatched route ────────────────────────
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.listen(PORT, () => {
  console.log(`\n🌿 NourishWell server running on http://localhost:${PORT}`);
  console.log(`\n── Backdoor test accounts ──────────────────────────`);
  console.log(`   Subscriber : test@user.com  / test1234  → /dashboard.html`);
  console.log(`   Pro        : test@pro.com   / test1234  → /pro_dashboard.html`);
  console.log(`────────────────────────────────────────────────────\n`);
});

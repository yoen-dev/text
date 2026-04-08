# NourishWell

Diet & nutrition web app — COMP2850 Group Project.

## Quick Start

```bash
npm install
npm start
# → http://localhost:3000
```

## Test Accounts (backdoor)

| Role         | Email           | Password  | Redirects to        |
|--------------|-----------------|-----------|---------------------|
| Subscriber   | test@user.com   | test1234  | /dashboard.html     |
| Professional | test@pro.com    | test1234  | /pro_dashboard.html |

Backdoor info also available at: `GET /api/auth/backdoor-info`

## API Endpoints

| Method | Endpoint               | Description          |
|--------|------------------------|----------------------|
| POST   | /api/auth/register     | Create account       |
| POST   | /api/auth/login        | Sign in, get JWT     |
| GET    | /api/auth/me           | Verify token         |
| GET    | /api/auth/backdoor-info| Dev test accounts    |

## Auth Flow

1. `POST /api/auth/login` → returns `{ token, user, redirectTo }`
2. Store `token` in `localStorage` as `nw_token`
3. Dashboard pages check token on load — redirect to `/index.html` if missing/invalid
4. All future API calls send `Authorization: Bearer <token>`

## Project Structure

```
nourishwell/
├── server/
│   └── index.js          # Express server + auth routes
├── public/
│   ├── index.html        # Landing page + login/register
│   ├── dashboard.html    # Subscriber dashboard
│   └── pro_dashboard.html# Professional dashboard
├── package.json
├── .gitignore
└── README.md
```

## Notes

- User data is stored **in-memory** — restarting the server resets all registered users (backdoor accounts persist).
- Replace with a real database (SQLite / PostgreSQL) when building the full backend.
- Change `JWT_SECRET` via environment variable in production: `JWT_SECRET=your-secret npm start`

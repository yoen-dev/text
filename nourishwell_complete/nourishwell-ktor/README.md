# NourishWell — Ktor Backend

## Quick Start

```bash
# Run the server (port 8080)
./gradlew run

# Run tests
./gradlew test
```

Then put your frontend files in a `public/` folder next to where you run the server:
```
public/
  index.html
  dashboard.html
  pro_dashboard.html
```

## Backdoor Test Accounts

| Role         | Email           | Password  | Redirects to        |
|--------------|-----------------|-----------|---------------------|
| Subscriber   | test@user.com   | test1234  | /dashboard.html     |
| Professional | test@pro.com    | test1234  | /pro_dashboard.html |

## API Endpoints

| Method | Endpoint                    | Auth required | Description               |
|--------|-----------------------------|---------------|---------------------------|
| POST   | /api/auth/register          | No            | Create account            |
| POST   | /api/auth/login             | No            | Sign in, returns JWT      |
| GET    | /api/auth/me                | Yes (Bearer)  | Get current user info     |
| GET    | /api/auth/backdoor-info     | No            | Dev test account list     |

### POST /api/auth/login

Request:
```json
{ "email": "test@user.com", "password": "test1234" }
```

Response:
```json
{
  "token": "eyJ...",
  "user": { "id": "...", "email": "...", "firstName": "Rose", "lastName": "Campbell", "role": "subscriber" },
  "redirectTo": "/dashboard.html"
}
```

### POST /api/auth/register

Request:
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "password": "password123",
  "role": "subscriber"
}
```
For `role: "professional"` also include `"licenceNumber": "ABC123"`.

## Project Structure

```
src/main/kotlin/com/nourishwell/
├── Application.kt              # Entry point
├── models/
│   └── Models.kt               # Data classes (User, requests, responses)
├── utils/
│   ├── UserStore.kt            # In-memory user store → swap with DB here
│   └── JwtUtil.kt              # JWT sign/verify helpers
├── plugins/
│   ├── Security.kt             # JWT auth configuration
│   ├── Routing.kt              # Route registration + static files
│   └── Plugins.kt              # Serialization, CORS, error handling
└── routes/
    └── AuthRoutes.kt           # /api/auth/* endpoints
```

## Adding a Real Database (Later)

When you're ready to add persistence, the only file you need to change is `UserStore.kt`:

1. Add to `build.gradle.kts`:
```kotlin
implementation("org.jetbrains.exposed:exposed-core:0.44.1")
implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
implementation("com.h2database:h2:2.2.224")  // or PostgreSQL driver
```

2. Replace the `users` list in `UserStore.kt` with Exposed table queries.

## Environment Variables

| Variable   | Default                                        | Description        |
|------------|------------------------------------------------|--------------------|
| PORT       | 8080                                           | Server port        |
| JWT_SECRET | nourishwell-dev-secret-change-in-production    | JWT signing secret |

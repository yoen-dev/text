package com.nourishwell.routes

import com.nourishwell.models.*
import com.nourishwell.utils.JwtUtil
import com.nourishwell.utils.UserStore
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {

    route("/api/auth") {

        // ── POST /api/auth/register ───────────────────────────────────────────
        post("/register") {
            val req = call.receive<RegisterRequest>()

            // Validation
            if (req.firstName.isBlank() || req.lastName.isBlank() ||
                req.email.isBlank() || req.password.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("All fields are required")
                )
            }
            if (req.password.length < 8) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Password must be at least 8 characters")
                )
            }
            val role = try {
                UserRole.valueOf(req.role)
            } catch (e: IllegalArgumentException) {
                UserRole.subscriber
            }
            if (role == UserRole.professional && req.licenceNumber.isNullOrBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Licence number required for professionals")
                )
            }
            if (UserStore.exists(req.email)) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("An account with this email already exists")
                )
            }

            val newUser = User(
                id            = "usr_${System.currentTimeMillis()}",
                email         = req.email.lowercase().trim(),
                passwordHash  = BCrypt.hashpw(req.password, BCrypt.gensalt()),
                firstName     = req.firstName.trim(),
                lastName      = req.lastName.trim(),
                role          = role,
                licenceNumber = req.licenceNumber
            )
            UserStore.save(newUser)

            val token     = JwtUtil.generateToken(newUser)
            val redirectTo = if (role == UserRole.professional) "/pro_dashboard.html" else "/dashboard.html"

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    token      = token,
                    user       = newUser.toResponse(),
                    redirectTo = redirectTo
                )
            )
        }

        // ── POST /api/auth/login ──────────────────────────────────────────────
        post("/login") {
            val req = call.receive<LoginRequest>()

            if (req.email.isBlank() || req.password.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Email and password are required")
                )
            }

            val user = UserStore.findByEmail(req.email)
            if (user == null || !BCrypt.checkpw(req.password, user.passwordHash)) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Incorrect email or password")
                )
            }

            val token      = JwtUtil.generateToken(user)
            val redirectTo = if (user.role == UserRole.professional) "/pro_dashboard.html" else "/dashboard.html"

            call.respond(
                AuthResponse(
                    token      = token,
                    user       = user.toResponse(),
                    redirectTo = redirectTo
                )
            )
        }

        // ── GET /api/auth/me (requires valid JWT) ─────────────────────────────
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId    = principal?.subject ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, ErrorResponse("Invalid token")
                )
                val user = UserStore.findById(userId) ?: return@get call.respond(
                    HttpStatusCode.NotFound, ErrorResponse("User not found")
                )
                call.respond(user.toResponse())
            }
        }

        // ── GET /api/auth/backdoor-info (dev only — remove before real deploy) ─
        get("/backdoor-info") {
            call.respond(
                BackdoorInfoResponse(
                    note = "Development backdoor accounts — remove before production",
                    accounts = listOf(
                        BackdoorAccount("test@user.com", "test1234", "subscriber", "/dashboard.html"),
                        BackdoorAccount("test@pro.com",  "test1234", "professional", "/pro_dashboard.html")
                    )
                )
            )
        }
    }
}

// ── Extension: User → UserResponse ───────────────────────────────────────────
fun User.toResponse() = UserResponse(
    id        = id,
    email     = email,
    firstName = firstName,
    lastName  = lastName,
    role      = role.name
)

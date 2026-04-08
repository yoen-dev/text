package com.nourishwell.plugins

import com.nourishwell.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {

        // ── Auth API routes ────────────────────────────────────────────────────
        authRoutes()

        // ── Serve frontend static files from /public ───────────────────────────
        // In production, put your built frontend files in the `public` folder
        // next to the jar, or configure a proper static file directory.
        staticFiles("/", File("public")) {
            default("index.html")
        }
    }
}

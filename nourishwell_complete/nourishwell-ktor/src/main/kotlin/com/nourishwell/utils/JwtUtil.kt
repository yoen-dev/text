package com.nourishwell.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nourishwell.models.User
import io.ktor.server.application.*
import java.util.Date

object JwtUtil {

    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private var expiryDays: Int = 7

    fun init(app: Application) {
        secret    = app.environment.config.property("jwt.secret").getString()
        issuer    = app.environment.config.property("jwt.issuer").getString()
        audience  = app.environment.config.property("jwt.audience").getString()
        expiryDays = app.environment.config.propertyOrNull("jwt.expiryDays")
            ?.getString()?.toIntOrNull() ?: 7
    }

    fun generateToken(user: User): String {
        val expiresAt = Date(System.currentTimeMillis() + expiryDays * 24 * 60 * 60 * 1000L)
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withClaim("firstName", user.firstName)
            .withExpiresAt(expiresAt)
            .sign(Algorithm.HMAC256(secret))
    }

    fun getSecret()   = secret
    fun getIssuer()   = issuer
    fun getAudience() = audience
}

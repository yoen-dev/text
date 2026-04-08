package com.nourishwell

import com.nourishwell.plugins.*
import com.nourishwell.utils.JwtUtil
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Initialise JWT util with config values
    JwtUtil.init(this)

    // Install plugins
    configureSerialization()
    configureCors()
    configureStatusPages()
    configureSecurity()
    configureRouting()

    log.info("🌿 NourishWell server started")
    log.info("── Backdoor test accounts ──────────────────────")
    log.info("   Subscriber : test@user.com  / test1234")
    log.info("   Pro        : test@pro.com   / test1234")
    log.info("────────────────────────────────────────────────")
}

package com.nourishwell

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.*

class AuthTest {

    @Test
    fun testLoginBackdoorSubscriber() = testApplication {
        application { module() }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@user.com","password":"test1234"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("subscriber", body["user"]?.jsonObject?.get("role")?.jsonPrimitive?.content)
        assertEquals("/dashboard.html", body["redirectTo"]?.jsonPrimitive?.content)
        assertNotNull(body["token"])
    }

    @Test
    fun testLoginBackdoorPro() = testApplication {
        application { module() }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@pro.com","password":"test1234"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("professional", body["user"]?.jsonObject?.get("role")?.jsonPrimitive?.content)
        assertEquals("/pro_dashboard.html", body["redirectTo"]?.jsonPrimitive?.content)
    }

    @Test
    fun testLoginWrongPassword() = testApplication {
        application { module() }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@user.com","password":"wrongpassword"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testRegisterNewUser() = testApplication {
        application { module() }
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firstName":"Jane","lastName":"Doe","email":"jane@test.com","password":"securepass123","role":"subscriber"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("subscriber", body["user"]?.jsonObject?.get("role")?.jsonPrimitive?.content)
    }

    @Test
    fun testRegisterDuplicateEmail() = testApplication {
        application { module() }
        // First registration
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firstName":"A","lastName":"B","email":"dup@test.com","password":"password123","role":"subscriber"}""")
        }
        // Duplicate
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firstName":"A","lastName":"B","email":"dup@test.com","password":"password123","role":"subscriber"}""")
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }
}

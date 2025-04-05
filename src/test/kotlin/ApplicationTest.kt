package com.jawa

import com.jawa.auth.LoginRequest
import com.jawa.dto.User
import com.jawa.enums.Role
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {
    private val ROOT = "/api"

    private fun testAppWithConfig(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        environment {
            config = ApplicationConfig("application-test.yaml")
        }

        application {
            module()
        }

        block(
            createClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                install(HttpCookies)
            }
        )
    }

    @Test
    fun testRoot() = testAppWithConfig {
        client.get(ROOT).apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello dari Server Ktor", body())
        }
    }

    @Test
    fun testInitDatabase() = testAppWithConfig { client ->
        client.get("$ROOT/users").apply {
            assertEquals(HttpStatusCode.OK, status)

            val expectedAdmin = User(
                id = 1L,
                username = "andreasmlbngaol",
                name = "Andreas M Lbn Gaol",
                role = Role.ADMIN,
                mustChangePassword = false
            )

            assertTrue(
                body<List<User>>()
                    .any { it == expectedAdmin }
            )
        }
    }

    @Test
    fun testGetUserById() = testAppWithConfig { client ->
        client.get("$ROOT/users/1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(Role.ADMIN, body<User>().role)
        }
    }

    @Test
    fun testSuccessLogin() = testAppWithConfig { client ->
        val loginPayload = LoginRequest("andreasmlbngaol", "password")

        val response = client.post("$ROOT/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginPayload))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Login Success", response.bodyAsText())

        val cookies = client.cookies("http://localhost/")
        val sessionCookie = cookies["USER_SESSION"]
        assertNotNull(sessionCookie, "Session cookie not found")
    }

    @Test
    fun testWrongPassword() = testAppWithConfig { client ->
        val loginPayload = LoginRequest("andreasmlbngaol", "wrongpassword")

        val response = client.post("$ROOT/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginPayload))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("Passwords do not match", response.bodyAsText())
    }
}


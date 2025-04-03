package com.jawa

import com.jawa.dao.UserDao
import com.jawa.dto.UserRequest
import com.jawa.service.validName
import com.jawa.service.validPassword
import com.jawa.service.validUsername
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/users") {
            call.respond(UserDao.getAllUsers())
        }

        post("/users") {
            println("Start")
            val request = try {
                println("Received")
                call.receive<UserRequest>()
            } catch (e: Exception) {
                println(e.message)
                call.respond(HttpStatusCode.BadRequest, "Invalid Request Format")
                return@post
            }

            println("request: $request")
            val username = request.username.lowercase()
            println("Lowercased")

            if(!username.validUsername()) {
                println("Error1")
                call.respond(HttpStatusCode.BadRequest, "Invalid Username Format")
                return@post
            }

            if(request.name.validName()) {
                println("Error1")

                call.respond(HttpStatusCode.BadRequest, "Invalid Name Format")
                return@post
            }

            if(request.originalPassword.validPassword()) {
                println("Error1")

                call.respond(HttpStatusCode.BadRequest, "Invalid Password Format")
                return@post
            }

            println("Test")

            UserDao.insertUser(request.copy(username = username.lowercase()))
            call.respond(HttpStatusCode.Created)
        }
    }
}
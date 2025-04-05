package com.jawa

import com.jawa.dao.UserDao
import com.jawa.dto.CreateUserRequest
import com.jawa.dto.UpdateUserRequest
import com.jawa.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureUserRoute() {
    routing {
        route("/api") {
            get("/") {
                call.respond(HttpStatusCode.OK, "Hello dari Server Ktor")
            }
            route("/users") {
                get { call.respond(HttpStatusCode.OK, UserDao.getAllUsers()) }

                post {
                    val request = try {
                        call.receive<CreateUserRequest>()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid Request")
                        return@post
                    }

                    if (!request.username.isValidUsername()) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid Username")
                        return@post
                    }

                    if (!request.username.isUsernameAvailable()) {
                        call.respond(HttpStatusCode.Conflict, "Username is already taken")
                        return@post
                    }

                    if (!request.name.isValidName()) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid Name")
                        return@post
                    }

                    val otp = PasswordManager.generateOtp()

                    UserDao.insertUser(
                        request = request.copy(username = request.username.lowercase()),
                        otp = otp
                    )
                    call.respond(HttpStatusCode.Created, mapOf("otp" to otp))
                }

                route("/{id}") {
                    get {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if(id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid Id")
                            return@get
                        }

                        val user = UserDao.getUserById(id)
                        if(user == null) {
                            call.respond(HttpStatusCode.NotFound, "User with id $id does not exist")
                            return@get
                        }

                        call.respond(HttpStatusCode.OK, user)
                    }

                    patch("/name") {
                        call.extractPatchRequest { id, request ->
                            request.name?.let { name ->
                                if (!name.isValidName()) {
                                    call.respond(HttpStatusCode.BadRequest, "Invalid Name Format")
                                    return@extractPatchRequest
                                }

                                UserDao.updateName(id, name)
                                call.respond(HttpStatusCode.NoContent, "User Name Updated")
                            }
                        }
                    }

                    route("/password") {
                        patch("/reset") {
                            call.extractPatchRequest { id, request ->
                                request.resetPassword?.let { resetPassword ->
                                    if (resetPassword) {
                                        val otp = PasswordManager.generateOtp()
                                        UserDao.resetPassword(id, otp)
                                        call.respond(HttpStatusCode.OK, mapOf("otp" to otp))
                                    }
                                }
                            }
                        }

                        patch("/change") {
                            call.extractPatchRequest { id, request ->
                                request.newPassword?.let { newPassword ->
                                    if (!newPassword.isValidPassword()) {
                                        call.respond(HttpStatusCode.BadRequest, "Invalid Password Format")
                                        return@extractPatchRequest
                                    }

                                    UserDao.updatePassword(id, newPassword)
                                    call.respond(HttpStatusCode.NoContent, "Password Updated")
                                }
                            }
                        }
                    }

                    patch("/username") {
                        call.extractPatchRequest { id, request ->
                            request.username?.let { username ->
                                if (!username.isUsernameAvailable()) {
                                    call.respond(HttpStatusCode.Conflict, "Username is already taken")
                                    return@extractPatchRequest
                                }

                                if (!username.isValidUsername()) {
                                    call.respond(HttpStatusCode.BadRequest, "Invalid Name Format")
                                    return@extractPatchRequest
                                }

                                UserDao.updateUsername(id, username)
                                call.respond(HttpStatusCode.NoContent, "User Name Updated")
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun RoutingCall.extractPatchRequest(
    onExtract: suspend (Long, UpdateUserRequest) -> Unit
) {
    val id = this.parameters["id"]?.toLongOrNull()
    if(id == null) {
        this.respond(HttpStatusCode.BadRequest, "Invalid Id Format. ID is null")
        return
    }

    if(UserDao.getUserById(id) == null) {
        this.respond(HttpStatusCode.NotFound, "User with ID $id does not exist")
        return
    }

    val request = try {
        this.receive<UpdateUserRequest>()
    } catch (e: Exception) {
        this.respond(HttpStatusCode.BadRequest, "Invalid Request Format")
        return
    }

    onExtract(id, request)
}
package com.jawa

import com.jawa.dao.UserDao
import com.jawa.dao.UserDao.toUser
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
                    val request = runCatching { call.receive<CreateUserRequest>() }
                        .getOrElse {
                            call.respond(HttpStatusCode.BadRequest, "Invalid Request")
                            return@post
                        }

                    when {
                        !request.username.isValidUsername() -> return@post call.respond(HttpStatusCode.BadRequest, "Invalid Username")
                        !request.username.isUsernameAvailable() -> return@post call.respond(HttpStatusCode.Conflict, "Username is already taken")
                        !request.name.isValidName() -> return@post call.respond(HttpStatusCode.BadRequest, "Invalid Name")
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
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid Id Format")
                        val user = UserDao.getUserById(id)?.toUser() ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")
                        call.respond(HttpStatusCode.OK, user)
                    }

                    patch("/name") {
                        call.extractPatchRequest { id, updateRequest ->
                            updateRequest.name?.let { name ->
                                if (!name.isValidName()) return@extractPatchRequest call.respond(HttpStatusCode.BadRequest, "Invalid Name Format")

                                UserDao.updateName(id, name)
                                call.respond(HttpStatusCode.NoContent, "Name updated")
                            } ?: call.respond(HttpStatusCode.BadRequest, "No Name Provided")
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
                                } ?: call.respond(HttpStatusCode.BadRequest, "Invalid Reset Password Request Format")
                            }
                        }

                        patch("/change") {
                            call.extractPatchRequest { id, request ->
                                val newPassword = request.newPassword
                                val oldPassword = request.oldPassword

                                when {
                                    oldPassword == null || newPassword == null -> return@extractPatchRequest call.respond(HttpStatusCode.BadRequest, "Old and new password required")
                                    !newPassword.isValidPassword() -> return@extractPatchRequest call.respond(HttpStatusCode.BadRequest, "Invalid password format")
                                    !UserDao.verifyPassword(id, oldPassword) -> return@extractPatchRequest call.respond(HttpStatusCode.Unauthorized, "Incorrect old password")
                                }

                                newPassword?.let {
                                    UserDao.updatePassword(id, newPassword)
                                    call.respond(HttpStatusCode.NoContent, "Password updated")
                                }
                            }
                        }
                    }

                    patch("/username") {
                        call.extractPatchRequest { id, request ->
                            request.username?.let { username ->
                                if (!username.isUsernameAvailable()) return@extractPatchRequest call.respond(HttpStatusCode.Conflict, "Username is already taken")
                                if (!username.isValidUsername()) return@extractPatchRequest call.respond(HttpStatusCode.BadRequest, "Invalid Name Format")

                                UserDao.updateUsername(id, username)
                                call.respond(HttpStatusCode.NoContent, "Username updated")
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun RoutingCall.extractPatchRequest(
    block: suspend (Long, UpdateUserRequest) -> Unit
) {
    val id = this.parameters["id"]?.toLongOrNull()
        ?: return this.respond(HttpStatusCode.BadRequest, "Invalid Id Format. ID is null")

    if(UserDao.getUserById(id) == null)
        return this.respond(HttpStatusCode.NotFound, "User not found")

    val request = runCatching { this.receive<UpdateUserRequest>() }
        .getOrElse { return this.respond(HttpStatusCode.BadRequest, "Invalid Id") }

    block(id, request)
}
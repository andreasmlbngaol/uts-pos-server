package com.jawa.routes.user

import com.jawa.dao.UserDao
import com.jawa.dao.UserDao.toUser
import com.jawa.dto.UpdateUserRequest
import com.jawa.service.isUsernameAvailable
import com.jawa.service.isValidName
import com.jawa.service.isValidUsername
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userDetailRoutes() {
    route("/{id}") {
        get {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Invalid Id Format"
            )
            val user =
                UserDao.getUserById(id)?.toUser() ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")
            call.respond(HttpStatusCode.OK, user)
        }

        patch("/name") {
            call.extractPatchRequest { id, updateRequest ->
                updateRequest.name?.let { name ->
                    if (!name.isValidName()) return@extractPatchRequest call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid Name Format"
                    )

                    UserDao.updateName(id, name)
                    call.respond(HttpStatusCode.NoContent, "Name updated")
                } ?: call.respond(HttpStatusCode.BadRequest, "No Name Provided")
            }
        }

        patch("/username") {
            call.extractPatchRequest { id, request ->
                request.username?.let { username ->
                    if (!username.isUsernameAvailable()) return@extractPatchRequest call.respond(
                        HttpStatusCode.Conflict,
                        "Username is already taken"
                    )
                    if (!username.isValidUsername()) return@extractPatchRequest call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid Name Format"
                    )

                    UserDao.updateUsername(id, username)
                    call.respond(HttpStatusCode.NoContent, "Username updated")
                }
            }
        }

        userPasswordRoutes()
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
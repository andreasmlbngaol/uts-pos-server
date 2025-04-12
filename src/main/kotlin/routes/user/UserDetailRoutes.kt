package com.jawa.routes.user

import com.jawa.dao.UserDao
import com.jawa.dao.UserDao.toUser
import com.jawa.dto.UpdateUserRequest
import com.jawa.response.stdRespond
import com.jawa.service.isUsernameAvailable
import com.jawa.service.isValidName
import com.jawa.service.isValidUsername
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.userDetailRoutes() {
    route("/{id}") {
        get {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.stdRespond(
                HttpStatusCode.BadRequest,
                "Invalid Id Format"
            )
            val user = UserDao.getUserById(id)?.toUser() ?: return@get call.stdRespond(HttpStatusCode.NotFound, "User not found")
            call.stdRespond(HttpStatusCode.OK, "Successfully retrieve user", user)
        }

        patch("/name") {
            call.extractPatchRequest { id, updateRequest ->
                updateRequest.name?.let { name ->
                    if (!name.isValidName()) return@extractPatchRequest call.stdRespond(
                        HttpStatusCode.BadRequest,
                        "Invalid new name format"
                    )

                    UserDao.updateName(id, name)
                    call.stdRespond(HttpStatusCode.NoContent, "Name updated")
                } ?: call.stdRespond(HttpStatusCode.BadRequest, "Provide new name")
            }
        }

        patch("/username") {
            call.extractPatchRequest { id, request ->
                request.username?.let { username ->
                    if (!username.isUsernameAvailable()) return@extractPatchRequest call.stdRespond(
                        HttpStatusCode.Conflict,
                        "Username is already taken"
                    )
                    if (!username.isValidUsername()) return@extractPatchRequest call.stdRespond(
                        HttpStatusCode.BadRequest,
                        "Invalid name format"
                    )

                    UserDao.updateUsername(id, username)
                    call.stdRespond(HttpStatusCode.NoContent, "Username updated successfully")
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
        ?: return this.stdRespond(HttpStatusCode.BadRequest, "Invalid id format. Id is null")

    if(UserDao.getUserById(id) == null)
        return this.stdRespond(HttpStatusCode.NotFound, "User with id $id does not exist")

    val request = runCatching { this.receive<UpdateUserRequest>() }
        .getOrElse { return this.stdRespond(HttpStatusCode.BadRequest, "Invalid Id") }

    block(id, request)
}
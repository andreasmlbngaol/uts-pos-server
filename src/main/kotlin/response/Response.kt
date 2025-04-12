package com.jawa.response

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val code: Int,
    val success: Boolean,
    val message: String,
    val data: T?
)

suspend inline fun <reified T> ApplicationCall.stdRespond(
    code: HttpStatusCode,
    message: String,
    data: T? = null
) {
    val isSuccess = code.value in 200..299
    respond(code, Response(code.value, isSuccess, message, data))
}

suspend fun ApplicationCall.stdRespond(
    code: HttpStatusCode,
    message: String
) = stdRespond<Unit>(code, message, null)

package com.jawa.response

import kotlinx.serialization.Serializable

@Serializable
data class OtpResponse(
    val otp: String
)

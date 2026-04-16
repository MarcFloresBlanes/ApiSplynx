package com.mflores.apisplynx.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("auth_type") val authType: String = "admin",
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
)

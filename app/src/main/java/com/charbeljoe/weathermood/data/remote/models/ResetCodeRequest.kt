package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResetCodeRequest(
    @Json(name = "email") val email: String
)

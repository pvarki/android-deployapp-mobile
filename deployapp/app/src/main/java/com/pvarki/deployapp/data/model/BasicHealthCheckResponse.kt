package com.pvarki.deployapp.data.model

data class BasicHealthCheckResponse(
    val deployment: String,
    val dns: String,
    val healthcheck: String,
    val rm_version: String,
    val version: String
)
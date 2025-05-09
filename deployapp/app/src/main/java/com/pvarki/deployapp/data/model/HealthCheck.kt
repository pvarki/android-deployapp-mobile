package com.pvarki.deployapp.data.model

data class HealthCheck(
    val deployment: String,
    val dns: String,
    val healthcheck: String,
    val rm_version: String,
    val version: String
)
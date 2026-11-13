package com.pvarki.deployapp.data.model

data class EnrollmentInviteCodeEnrollIn(
    val invite_code: String,
    val callsign: String,
    val csr: String? = null
)

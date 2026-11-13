package com.pvarki.deployapp.data.model

data class EnrollmentShowVerificationCodeOut(
    val state: String,
    val callsign: String,
    val accepted: String,
    val locked: String
)

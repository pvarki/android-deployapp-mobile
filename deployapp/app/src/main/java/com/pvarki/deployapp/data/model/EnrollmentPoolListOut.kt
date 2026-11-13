package com.pvarki.deployapp.data.model

data class EnrollmentPoolListItem(
    val invitecode: String,
    val active: Boolean,
    val owner_cs: String,
    val created: String
)

data class EnrollmentPoolListOut(
    val pools: List<EnrollmentPoolListItem>
)

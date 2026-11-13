package com.pvarki.deployapp.data.model

data class OperationResultResponse(
    val success: Boolean,
    val extra: String? = null,
    val error: String? = null
)

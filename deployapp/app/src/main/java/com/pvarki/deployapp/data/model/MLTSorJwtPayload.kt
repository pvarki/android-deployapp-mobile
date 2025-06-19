package com.pvarki.deployapp.data.model

data class MLTSorJwtPayload(
    val type: String,
    val userid: String,
    val payload: Map<String, String>
)
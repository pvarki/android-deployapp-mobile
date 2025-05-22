package com.pvarki.deployapp.data.model

data class AllProductsHealthCheckResponse(
    val all_ok: Boolean,
    val products: Map<String, Boolean>
)

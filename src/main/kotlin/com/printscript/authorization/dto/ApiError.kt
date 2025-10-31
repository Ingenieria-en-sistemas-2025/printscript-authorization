package com.printscript.authorization.dto

import java.time.Instant

data class ApiError(
    val message: String,
    val code: String? = null,
    val path: String? = null,
    val timestamp: Instant = Instant.now(),
)

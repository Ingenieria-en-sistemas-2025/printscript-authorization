package com.printscript.authorization.dto

import com.printscript.authorization.db.table.AuthorizationScopeType
import jakarta.validation.constraints.NotBlank

data class AuthorizationCreateRequest(
    @field:NotBlank(message = "Snippet ID cannot be blank")
    val snippetId: String,

    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String,

    val scope: AuthorizationScopeType,
)

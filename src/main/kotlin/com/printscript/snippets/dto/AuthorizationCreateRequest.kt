package com.printscript.snippets.dto

import jakarta.validation.constraints.NotBlank

data class AuthorizationCreateRequest(
    @field:NotBlank(message = "Snippet ID cannot be blank")
    val snippetId: String,

    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String,

    @field:NotBlank(message = "Scope cannot be blank")
    val scope: String
)

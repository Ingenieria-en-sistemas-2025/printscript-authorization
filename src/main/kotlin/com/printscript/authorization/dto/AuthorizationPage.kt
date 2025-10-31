package com.printscript.authorization.dto

data class AuthorizationView(
    val id: String,
    val snippetId: String,
    val ownerId: String,
    val scope: String,
)

data class AuthorizationPage(
    val authorizations: List<AuthorizationView>,
    val total: Int,
)

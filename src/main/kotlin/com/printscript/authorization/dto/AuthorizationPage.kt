package com.printscript.authorization.dto

import com.printscript.authorization.db.table.AuthorizationScopeType

data class AuthorizationView(
    val id: String,
    val snippetId: String,
    val scope: AuthorizationScopeType,
)

data class AuthorizationPage(
    val authorizations: List<AuthorizationView>,
    val total: Int,
)

package com.printscript.authorization.service

import com.printscript.authorization.dto.AuthorizationCreateRequest
import com.printscript.authorization.dto.AuthorizationPage

interface AuthorizationService {
    fun createAuthorization(input: AuthorizationCreateRequest)
    fun listByUser(userId: String, page: Int, size: Int): AuthorizationPage
    fun revokeAllBySnippet(snippetId: String)
    fun findOwner(snippetId: String): String
}

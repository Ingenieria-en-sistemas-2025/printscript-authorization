package com.printscript.authorization.service

import com.printscript.authorization.db.repository.AuthorizationRepository
import com.printscript.authorization.db.repository.AuthorizationScopeRepository
import com.printscript.authorization.db.table.Authorization
import com.printscript.authorization.dto.AuthorizationCreateRequest
import com.printscript.authorization.dto.AuthorizationPage
import com.printscript.authorization.dto.AuthorizationView
import com.printscript.authorization.exceptions.OwnerNotFound
import com.printscript.authorization.exceptions.ScopeNotFound
import com.printscript.authorization.exceptions.UserAlreadyAuthorized
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val authorizationRepo: AuthorizationRepository,
    private val scopeRepo: AuthorizationScopeRepository,
) {
    fun createAuthorization(input: AuthorizationCreateRequest) {
        val userId = requireNotNull(input.userId) {
            "userId must be provided by controller"
        }

        if (authorizationRepo.findByUserIdAndSnippetId(userId, input.snippetId).isPresent) {
            throw UserAlreadyAuthorized()
        }

        val scope = scopeRepo.findByName(input.scope).orElseThrow { ScopeNotFound() }

        authorizationRepo.save(
            Authorization(snippetId = input.snippetId, userId = userId, scope = scope),
        )
    }

    fun listByUser(userId: String, page: Int, size: Int): AuthorizationPage {
        val pagination = PageRequest.of(page, size)
        val records = authorizationRepo.findAllByUserId(userId, pagination)
        val total = authorizationRepo.countAllByUserId(userId)

        val ownerScope = scopeRepo.findByName("OWNER").orElseThrow { ScopeNotFound() }

        val views = records.map {
            val ownerAuth = authorizationRepo.findByScopeNameAndSnippetId(ownerScope.name, it.snippetId)
                .orElse(null)

            AuthorizationView(
                it.id!!,
                it.snippetId,
                ownerAuth?.userId ?: it.userId,
                it.scope.name,
            )
        }

        return AuthorizationPage(views, total)
    }

    @Transactional
    fun revokeAllBySnippet(snippetId: String) {
        authorizationRepo.deleteAllBySnippetId(snippetId)
    }

    fun findOwner(snippetId: String): String {
        val ownerScope = scopeRepo.findByName("OWNER").orElseThrow { ScopeNotFound() }
        val ownerAuth = authorizationRepo.findByScopeNameAndSnippetId(ownerScope.name, snippetId)
            .orElseThrow { OwnerNotFound() }

        return ownerAuth.userId
    }
}

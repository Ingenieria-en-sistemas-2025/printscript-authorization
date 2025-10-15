package com.printscript.snippets.service

import com.printscript.snippets.db.repository.AuthorizationRepository
import com.printscript.snippets.db.repository.AuthorizationScopeRepository
import com.printscript.snippets.db.table.Authorization
import com.printscript.snippets.dto.AuthorizationCreateRequest
import com.printscript.snippets.dto.AuthorizationPage
import com.printscript.snippets.dto.AuthorizationView
import com.printscript.snippets.exceptions.UserAlreadyAuthorized
import com.printscript.snippets.exceptions.ScopeNotFound
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val authorizationRepo: AuthorizationRepository,
    private val scopeRepo: AuthorizationScopeRepository
) {
    private val log = LoggerFactory.getLogger(AuthorizationService::class.java)

    fun createAuthorization(input: AuthorizationCreateRequest) {
        if (authorizationRepo.findByUserIdAndSnippetId(input.userId, input.snippetId).isPresent) {
            throw UserAlreadyAuthorized()
        }

        val scope = scopeRepo.findByName(input.scope).orElseThrow { ScopeNotFound() }

        authorizationRepo.save(
            Authorization(snippetId = input.snippetId, userId = input.userId, scope = scope)
        )
        log.info("Authorization created for user ${input.userId} on snippet ${input.snippetId}")
    }

    fun listByUser(userId: String, page: Int, size: Int): AuthorizationPage {
        val pagination = PageRequest.of(page, size)
        val records = authorizationRepo.findAllByUserId(userId, pagination)
        val total = authorizationRepo.countAllByUserId(userId)

        val ownerScope = scopeRepo.findByName("OWNER").get()
        val views = records.map {
            val ownerAuth = authorizationRepo.getByScopeNameAndSnippetId(ownerScope.name, it.snippetId)
            AuthorizationView(it.id!!, it.snippetId, ownerAuth.userId, it.scope.name)
        }

        return AuthorizationPage(views, total)
    }

    @Transactional
    fun revokeAllBySnippet(snippetId: String) {
        authorizationRepo.deleteAllBySnippetId(snippetId)
    }

    fun findOwner(snippetId: String): String {
        val ownerScope = scopeRepo.findByName("OWNER").orElseThrow { ScopeNotFound() }
        val ownerAuth = authorizationRepo.getByScopeNameAndSnippetId(ownerScope.name, snippetId)
        return ownerAuth.userId
    }
}
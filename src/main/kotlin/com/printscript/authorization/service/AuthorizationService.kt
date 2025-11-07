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
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val authorizationRepo: AuthorizationRepository,
    private val scopeRepo: AuthorizationScopeRepository,
) {
    private val logger = LoggerFactory.getLogger(AuthorizationService::class.java)

    fun createAuthorization(input: AuthorizationCreateRequest) {
        logger.info("Request received for creating authorization for user: ${input.userId} and snippet: ${input.snippetId}")

        val userId = requireNotNull(input.userId) {
            logger.error("userId was not provided in request body")
            "userId must be provided in request body"
        }

        if (authorizationRepo.findByUserIdAndSnippetId(userId, input.snippetId).isPresent) {
            logger.error("User with id: $userId already has authorization for snippet: ${input.snippetId}")
            throw UserAlreadyAuthorized()
        }

        val scope = scopeRepo.findByName(input.scope).orElseThrow {
            logger.error("Scope with name: ${input.scope} not found in database.")
            ScopeNotFound()
        }

        authorizationRepo.save(
            Authorization(snippetId = input.snippetId, userId = userId, scope = scope),
        )
        logger.info("Authorization created successfully for user: $userId and snippet: ${input.snippetId}")
    }

    fun listByUser(userId: String, page: Int, size: Int): AuthorizationPage {
        logger.info("Request received for listing authorizations for user: $userId (Page $page, Size $size)")

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

        logger.info("Successfully returning ${views.size} authorizations for user: $userId (Total: $total)")
        return AuthorizationPage(views, total)
    }

    @Transactional
    fun revokeAllBySnippet(snippetId: String) {
        logger.info("Request received to revoke all authorizations for snippet: $snippetId")

        authorizationRepo.deleteAllBySnippetId(snippetId)

        logger.info("All authorizations revoked successfully for snippet: $snippetId")
    }

    fun findOwner(snippetId: String): String {
        logger.info("Request received to find owner for snippet: $snippetId")

        val ownerScope = scopeRepo.findByName("OWNER").orElseThrow { ScopeNotFound() }
        val ownerAuth = authorizationRepo.findByScopeNameAndSnippetId(ownerScope.name, snippetId)
            .orElseThrow {
                logger.error("Owner authorization not found for snippet: $snippetId")
                OwnerNotFound()
            }

        logger.info("Owner found for snippet: $snippetId (User ID: ${ownerAuth.userId})")
        return ownerAuth.userId
    }
}

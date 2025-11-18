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
class AuthorizationServiceImpl(
    private val authorizationRepo: AuthorizationRepository,
    private val scopeRepo: AuthorizationScopeRepository,
) : AuthorizationService {
    private val logger = LoggerFactory.getLogger(AuthorizationServiceImpl::class.java)

    override fun createAuthorization(input: AuthorizationCreateRequest) {
        logger.info("Request received for creating authorization for user: ${input.userId} and snippet: ${input.snippetId}")

        val decodedUserId = java.net.URLDecoder.decode(
            requireNotNull(input.userId) {
                logger.error("userId was not provided in request body")
                "userId must be provided in request body"
            },
            "UTF-8",
        )

        if (authorizationRepo.findByUserIdAndSnippetId(decodedUserId, input.snippetId).isPresent) {
            logger.error("User with id: $decodedUserId already has authorization for snippet: ${input.snippetId}")
            throw UserAlreadyAuthorized()
        }

        val scope = scopeRepo.findByName(input.scope).orElseThrow {
            logger.error("Scope with name: ${input.scope} not found in database.")
            ScopeNotFound()
        }

        authorizationRepo.save(
            Authorization(snippetId = input.snippetId, userId = decodedUserId, scope = scope),
        )
        logger.info("Authorization created successfully for user: $decodedUserId and snippet: ${input.snippetId}")
    }

    override fun listByUser(userId: String, page: Int, size: Int): AuthorizationPage {
        // DECODIFICAR el userId
        val decodedUserId = java.net.URLDecoder.decode(userId, "UTF-8")

        logger.info("Request received for listing authorizations for user: $userId (decoded: $decodedUserId) (Page $page, Size $size)")

        val pagination = PageRequest.of(page, size)
        val records = authorizationRepo.findAllByUserId(decodedUserId, pagination)
        val total = authorizationRepo.countAllByUserId(decodedUserId)

        val views = records.map {
            AuthorizationView(
                id = it.id!!,
                snippetId = it.snippetId,
                scope = it.scope.name,
            )
        }

        logger.info("Successfully returning ${views.size} authorizations for user: $decodedUserId (Total: $total)")
        return AuthorizationPage(views, total)
    }

    @Transactional
    override fun revokeAllBySnippet(snippetId: String) {
        logger.info("Request received to revoke all authorizations for snippet: $snippetId")

        authorizationRepo.deleteAllBySnippetId(snippetId)

        logger.info("All authorizations revoked successfully for snippet: $snippetId")
    }

    override fun findOwner(snippetId: String): String {
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
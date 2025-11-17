package com.printscript.authorization

import com.printscript.authorization.db.repository.AuthorizationRepository
import com.printscript.authorization.db.repository.AuthorizationScopeRepository
import com.printscript.authorization.db.table.Authorization
import com.printscript.authorization.db.table.AuthorizationScope
import com.printscript.authorization.dto.AuthorizationCreateRequest
import com.printscript.authorization.exceptions.OwnerNotFound
import com.printscript.authorization.exceptions.ScopeNotFound
import com.printscript.authorization.exceptions.UserAlreadyAuthorized
import com.printscript.authorization.service.AuthorizationServiceImpl
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AuthorizationServiceTest {

    @Autowired lateinit var scopeRepo: AuthorizationScopeRepository

    @Autowired lateinit var authRepo: AuthorizationRepository

    @Autowired lateinit var service: AuthorizationServiceImpl

    @Autowired lateinit var entityManager: EntityManager

    private lateinit var scopeOwner: AuthorizationScope

    @BeforeAll
    fun setupBaseData() {
        scopeOwner = scopeRepo.save(AuthorizationScope("OWNER"))
    }

    @Test
    fun `createAuthorization crea una nueva autorizacion y decodifica el userId`() {
        val encodedUserId = "user%20encoded"
        val snippetId = "sn-create-ok"

        service.createAuthorization(
            AuthorizationCreateRequest(
                snippetId = snippetId,
                userId = encodedUserId,
                scope = scopeOwner.name,
            ),
        )

        val savedOpt = authRepo.findByUserIdAndSnippetId("user encoded", snippetId)
        assertTrue(savedOpt.isPresent)

        val saved = savedOpt.get()
        assertEquals("user encoded", saved.userId)
        assertEquals(snippetId, saved.snippetId)
        assertEquals(scopeOwner.name, saved.scope.name)
    }

    @Test
    fun `createAuthorization lanza UserAlreadyAuthorized cuando ya existe la autorizacion`() {
        val encodedUserId = "dup%20user"
        val snippetId = "sn-dup"

        service.createAuthorization(
            AuthorizationCreateRequest(
                snippetId = snippetId,
                userId = encodedUserId,
                scope = scopeOwner.name,
            ),
        )

        assertThrows<UserAlreadyAuthorized> {
            service.createAuthorization(
                AuthorizationCreateRequest(
                    snippetId = snippetId,
                    userId = encodedUserId,
                    scope = scopeOwner.name,
                ),
            )
        }
    }

    @Test
    fun `createAuthorization lanza ScopeNotFound cuando el scope no existe`() {
        assertThrows<ScopeNotFound> {
            service.createAuthorization(
                AuthorizationCreateRequest(
                    snippetId = "sn-scope-missing",
                    userId = "user-x",
                    scope = "NON_EXISTENT_SCOPE",
                ),
            )
        }
    }

    @Test
    fun testListForUser() {
        authRepo.save(
            Authorization(
                snippetId = "sn-map",
                userId = "mapper",
                scope = scopeOwner,
            ),
        )

        val page = service.listByUser("mapper", 0, 10)

        assertTrue(page.total > 0)
        assertEquals(1, page.total)
        assertEquals("sn-map", page.authorizations.first().snippetId)
        assertEquals(scopeOwner.name, page.authorizations.first().scope)
    }

    @Test
    fun `revokeAllBySnippet elimina todas las autorizaciones del snippet`() {
        val sn = "sn-delete-all"

        authRepo.save(
            Authorization(
                snippetId = sn,
                userId = "u1",
                scope = scopeOwner,
            ),
        )
        authRepo.save(
            Authorization(
                snippetId = sn,
                userId = "u2",
                scope = scopeOwner,
            ),
        )

        service.revokeAllBySnippet(sn)

        assertTrue(authRepo.findByUserIdAndSnippetId("u1", sn).isEmpty)
        assertTrue(authRepo.findByUserIdAndSnippetId("u2", sn).isEmpty)
    }

    @Test
    fun testFindOwner() {
        val sn = "sn-owner"

        authRepo.save(
            Authorization(
                snippetId = sn,
                userId = "u-owner",
                scope = scopeOwner,
            ),
        )

        val owner = service.findOwner(sn)

        assertEquals("u-owner", owner)
    }

    @Test
    fun `findOwner lanza OwnerNotFound cuando no hay autorizacion OWNER para el snippet`() {
        val sn = "sn-no-owner"

        assertThrows<OwnerNotFound> {
            service.findOwner(sn)
        }
    }

    @Test
    fun `createdAt y updatedAt se completan automaticamente al guardar Authorization`() {
        val auth = authRepo.save(
            Authorization(
                snippetId = "sn-timestamps",
                userId = "timestamps-user",
                scope = scopeOwner,
            ),
        )

        entityManager.flush()
        entityManager.refresh(auth)

        assertNotNull(auth.createdAt, "createdAt debería ser distinto de null")
        assertNotNull(auth.updatedAt, "updatedAt debería ser distinto de null")
    }
}

package com.printscript.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import com.printscript.authorization.JwtAuth.asUser
import com.printscript.authorization.db.repository.AuthorizationRepository
import com.printscript.authorization.db.repository.AuthorizationScopeRepository
import com.printscript.authorization.db.table.Authorization
import com.printscript.authorization.db.table.AuthorizationScope
import com.printscript.authorization.dto.AuthorizationCreateRequest
import com.printscript.authorization.service.AuthorizationService
import jakarta.transaction.Transactional
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestsAuthorizationApi {

    @Autowired lateinit var scopeRepo: AuthorizationScopeRepository

    @Autowired lateinit var authRepo: AuthorizationRepository

    @Autowired lateinit var service: AuthorizationService

    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var json: ObjectMapper

    private val base = "/authorization"
    private lateinit var scopeOwner: AuthorizationScope
    private lateinit var scopeEditor: AuthorizationScope

    @BeforeAll
    fun setupBaseData() {
        scopeOwner = scopeRepo.save(AuthorizationScope("OWNER"))
        scopeEditor = scopeRepo.save(AuthorizationScope("EDITOR"))
    }

    private fun body(snippet: String, user: String, scope: String) =
        json.writeValueAsString(AuthorizationCreateRequest(snippet, user, scope))

    @Nested
    inner class Creation {

        @Test
        fun testReturns404IfScopeDoesNotExist() {
            val payload = body("sn404", "u404", "NON_EXISTENT")

            mockMvc.perform(
                post("$base/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .with(asUser("whoever")),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.message", containsString("Scope")))
        }

        @Test
        fun testCreatesAuthorizationAndReturns200() {
            val payload = body("sn-ok", "user-ok", scopeEditor.name)

            mockMvc.perform(
                post("$base/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .with(asUser("tester")),
            ).andExpect(status().isOk)
        }

        @Test
        fun testReturns409IfAlreadyExists() {
            val payload = body("sn-dupe", "user-dupe", scopeEditor.name)

            mockMvc.perform(
                post("$base/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .with(asUser("tester")),
            ).andExpect(status().isOk)

            mockMvc.perform(
                post("$base/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .with(asUser("tester")),
            ).andExpect(status().isConflict)
        }
    }

    @Nested
    inner class Deletion {

        @Test
        fun testDeletesAllAuthorizationsFromSnippet() {
            val sn = "sn-clear"
            authRepo.saveAll(
                listOf(
                    Authorization(snippetId = sn, userId = "a1", scope = scopeEditor),
                    Authorization(snippetId = sn, userId = "a2", scope = scopeEditor),
                ),
            )

            mockMvc.perform(
                delete("$base/snippet/$sn").with(asUser("owner")),
            ).andExpect(status().isOk)

            assertTrue(authRepo.findByUserIdAndSnippetId("a1", sn).isEmpty)
            assertTrue(authRepo.findByUserIdAndSnippetId("a2", sn).isEmpty)
        }
    }

    @Nested
    @Transactional
    open inner class Queries {
        @Test
        fun testFindOwner() {
            val sn = "sn-owner"
            authRepo.save(Authorization(snippetId = sn, userId = "u-owner", scope = scopeOwner))

            val owner = service.findOwner(sn)
            assertEquals("u-owner", owner)
        }

        @Test
        fun testFindOwnerReturns404IfNotFound() {
            val nonExistentSnippetId = "sn-no-owner-404"

            mockMvc.perform(
                get("$base/owner/$nonExistentSnippetId")
                    .with(asUser("requester")),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code", equalTo("OWNER_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Owner")))
        }

        @Test
        fun testListForUser() {
            authRepo.save(Authorization(snippetId = "sn-map", userId = "mapper", scope = scopeOwner))

            val page = service.listByUser("mapper", 0, 10)
            assertTrue(page.total > 0)
        }

        @Test
        fun testListMineReturnsAuthorizationsForAuthenticatedUser() {
            val userId = "auth-user-123"
            val sn1 = "sn-mine-1"
            val sn2 = "sn-mine-2"

            authRepo.saveAll(
                listOf(
                    Authorization(snippetId = sn1, userId = userId, scope = scopeEditor),
                    Authorization(snippetId = sn2, userId = userId, scope = scopeEditor),
                ),
            )

            mockMvc.perform(
                get("$base/my")
                    .param("page", "0")
                    .param("size", "10")
                    .with(asUser(userId)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.total", greaterThan(0))) // Verifica que haya registros
                .andExpect(jsonPath("$.authorizations.length()", greaterThan(0)))
                .andExpect(jsonPath("$.authorizations[0].snippetId", equalTo(sn1)))
                .andExpect(jsonPath("$.authorizations[1].snippetId", equalTo(sn2)))
        }
    }
}

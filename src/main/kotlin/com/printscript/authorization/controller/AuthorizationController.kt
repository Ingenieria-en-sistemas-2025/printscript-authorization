package com.printscript.authorization.controller

import com.printscript.authorization.config.Routes
import com.printscript.authorization.dto.AuthorizationCreateRequest
import com.printscript.authorization.dto.AuthorizationPage
import com.printscript.authorization.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Routes.AUTHORIZATION)
class AuthorizationController(
    private val service: AuthorizationService,
) {

    @PostMapping(Routes.CREATE)
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody input: AuthorizationCreateRequest
    ): ResponseEntity<Unit> {
        service.createAuthorization(input.copy(userId = jwt.subject))
        return ResponseEntity.ok().build()
    }

    @GetMapping(Routes.MY)
    fun listMine(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int
    ): AuthorizationPage {
        return service.listByUser(jwt.subject, page, size)
    }

    @DeleteMapping(Routes.SNIPPET_ID)
    fun revokeAllBySnippet(@PathVariable snippetId: String): ResponseEntity<Unit> {
        service.revokeAllBySnippet(snippetId)
        return ResponseEntity.ok().build()
    }

    @GetMapping(Routes.OWNER)
    fun findOwner(@PathVariable snippetId: String): String {
        return service.findOwner(snippetId)
    }
}

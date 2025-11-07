package com.printscript.authorization.controller

import com.printscript.authorization.config.Routes
import com.printscript.authorization.dto.AuthorizationCreateRequest
import com.printscript.authorization.dto.AuthorizationPage
import com.printscript.authorization.service.AuthorizationService
import org.springframework.http.ResponseEntity
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
        @RequestBody input: AuthorizationCreateRequest,
    ): ResponseEntity<Unit> {
        service.createAuthorization(input)
        return ResponseEntity.ok().build()
    }

    @GetMapping(Routes.MY)
    fun listMine(
        @RequestParam("userId") userId: String,
        @RequestParam("pageNum", defaultValue = "0") pageNum: Int,
        @RequestParam("pageSize", defaultValue = "20") pageSize: Int,
    ): ResponseEntity<AuthorizationPage> {
        return ResponseEntity.ok(service.listByUser(userId, pageNum, pageSize))
    }

    @DeleteMapping(Routes.SNIPPET_ID)
    fun revokeAllBySnippet(@PathVariable snippetId: String): ResponseEntity<Unit> {
        service.revokeAllBySnippet(snippetId)
        return ResponseEntity.ok().build()
    }

    @GetMapping(Routes.OWNER)
    fun findOwner(@PathVariable snippetId: String): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("ownerId" to service.findOwner(snippetId)))
    }
}

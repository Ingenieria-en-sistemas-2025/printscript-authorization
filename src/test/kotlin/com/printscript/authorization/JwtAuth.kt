package com.printscript.authorization

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import java.time.Instant

object JwtAuth {
    fun asUser(sub: String) =
        SecurityMockMvcRequestPostProcessors.jwt().jwt { j: Jwt.Builder ->
            j.subject(sub)
                .issuer("https://test-issuer/")
                .audience(listOf("https://snippet-authorization"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
        }
}

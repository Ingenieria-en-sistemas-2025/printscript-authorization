package com.printscript.authorization.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import com.printscript.authorization.config.Routes as R

@Configuration
@EnableWebSecurity
@Profile("!test") // para poder usar TestSecurityConfig en los tests que no necesita Auth0
class SecurityConfig(
    @param:Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    // descargar claves publicas con las que valida la firma del JWT
    private val issuer: String,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests {
                it
                    // GET /authorization/my
                    .requestMatchers(GET, "/${R.AUTHORIZATION}/${R.MY}")
                    .hasAuthority("SCOPE_read:authorizations")
                    // POST /authorization/create
                    .requestMatchers(POST, "/${R.AUTHORIZATION}/${R.CREATE}")
                    .hasAuthority("SCOPE_write:authorizations")
                    // DELETE /authorization/snippet/{snippetId}
                    .requestMatchers(DELETE, "/${R.AUTHORIZATION}/snippet/*")
                    .hasAuthority("SCOPE_write:authorizations")
                    // GET /authorization/owner/{snippetId}
                    .requestMatchers(GET, "/${R.AUTHORIZATION}/owner/*")
                    .hasAuthority("SCOPE_read:authorizations")
                    .anyRequest().authenticated() // cualquier otro endpoint necesita token pero sin permisos
            }
            .oauth2ResourceServer { rs ->
                rs.jwt { jwt -> jwt.jwtAuthenticationConverter(permissionsConverter()) }
            }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()

    private fun permissionsConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        val base = JwtGrantedAuthoritiesConverter().apply { setAuthorityPrefix("SCOPE_") }
        return Converter { jwt ->
            val authorities = base.convert(jwt)?.toMutableSet()
            val perms = jwt.getClaimAsStringList("permissions") ?: emptyList()
            authorities?.addAll(perms.map { SimpleGrantedAuthority("SCOPE_$it") })
            JwtAuthenticationToken(jwt, authorities, jwt.subject)
        }
    //conversor para que lea el campo permissions del token y lo transforme en objetos GrantedAuthority,
    // que son los que Spring usa para verificar accesos
    }

    @Bean
    fun jwtDecoder(): JwtDecoder = JwtDecoders.fromIssuerLocation(issuer)
    /*
    Descarga las claves publicas de Auth0
    Valida la firma de los tokens que llegan
    Chequea que el claim iss coincida con el configurado
    Chequea expiracion (exp), emisión (iat), etc.

    Si algo falla -> 401 Unauthorized.
     */
}

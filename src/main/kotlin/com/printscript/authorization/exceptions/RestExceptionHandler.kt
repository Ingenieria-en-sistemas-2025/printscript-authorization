package com.printscript.authorization.exceptions

import com.printscript.authorization.dto.ApiError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(ScopeNotFound::class)
    fun handleScopeNotFound(ex: ScopeNotFound, req: HttpServletRequest): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError(
                message = ex.message ?: "Scope not found",
                code = "SCOPE_NOT_FOUND",
                path = req.requestURI,
            ),
        )

    @ExceptionHandler(UserAlreadyAuthorized::class)
    fun handleUserAlreadyAuthorized(ex: UserAlreadyAuthorized, req: HttpServletRequest): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiError(
                message = ex.message ?: "User already has an authorization for this snippet",
                code = "USER_ALREADY_AUTHORIZED",
                path = req.requestURI,
            ),
        )
}

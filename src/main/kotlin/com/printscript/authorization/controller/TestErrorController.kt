package com.printscript.authorization.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal-test")
open class TestErrorController {

    @GetMapping("/always-500")
    fun triggerError(): ResponseEntity<Map<String, String>> {
        val errorBody = mapOf("error" to "Simulated internal server error")
        return ResponseEntity(errorBody, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

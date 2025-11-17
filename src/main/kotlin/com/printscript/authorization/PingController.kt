package com.printscript.authorization

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.processing.Generated

@Generated
@RestController
class PingController {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<Void> = ResponseEntity.noContent().build()
}

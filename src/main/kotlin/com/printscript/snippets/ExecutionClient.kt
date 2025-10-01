package com.printscript.snippets

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ExecutionClient(
    @Value("\${execution.base-url}") private val baseUrl: String
) {
    private val rest = RestClient.create()

    fun ping(): Boolean = try {
        val res = rest.get()
            .uri("$baseUrl/ping")
            .retrieve()
            .toBodilessEntity()
        res.statusCode.is2xxSuccessful
    } catch (e: Exception) {
        false
    }

    fun target(): String = "$baseUrl/ping"
}
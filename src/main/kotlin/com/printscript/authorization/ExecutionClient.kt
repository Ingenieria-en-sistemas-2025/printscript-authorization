package com.printscript.authorization

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class ExecutionClient(
    @Value("\${execution.base-url:http://localhost:8082}") private val baseUrl: String,
) {
    private val rest = RestClient.create()
    private val log = LoggerFactory.getLogger(ExecutionClient::class.java)

    fun ping(): Boolean = try {
        val res = rest.get()
            .uri("$baseUrl/ping")
            .retrieve()
            .toBodilessEntity()

        res.statusCode.is2xxSuccessful
    } catch (e: RestClientResponseException) {
        // Errores HTTP (4xx/5xx) con cuerpo/status
        log.warn(
            "Ping a execution falló (HTTP {}), body='{}'",
            e.rawStatusCode,
            e.responseBodyAsString,
        )
        false
    } catch (e: ResourceAccessException) {
        // Problemas de red / timeouts / DNS / conexión rechazada
        log.warn("Ping a execution no accesible: ${e.message}", e)
        false
    }

    fun target(): String = "$baseUrl/ping"
}

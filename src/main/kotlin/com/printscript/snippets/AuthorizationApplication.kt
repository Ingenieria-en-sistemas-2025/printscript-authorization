package com.printscript.snippets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthorizationApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<AuthorizationApplication>(*args)
}

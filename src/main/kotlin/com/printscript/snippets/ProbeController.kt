package com.printscript.snippets

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class ProbeRes(val ok: Boolean, val target: String)

@RestController
class ProbeController(private val exec: ExecutionClient) {

    @GetMapping("/_probe/execution")
    fun probe(): ProbeRes = ProbeRes(ok = exec.ping(), target = exec.target())
}

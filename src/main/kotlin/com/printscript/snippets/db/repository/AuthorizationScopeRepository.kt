package com.printscript.snippets.db.repository

import com.printscript.snippets.db.table.AuthorizationScope
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AuthorizationScopeRepository : JpaRepository<AuthorizationScope, String> {
    fun findByName(name: String): Optional<AuthorizationScope>
}
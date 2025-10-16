package com.printscript.authorization.db.repository

import com.printscript.authorization.db.table.AuthorizationScope
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AuthorizationScopeRepository : JpaRepository<AuthorizationScope, String> {
    fun findByName(name: String): Optional<AuthorizationScope>
}

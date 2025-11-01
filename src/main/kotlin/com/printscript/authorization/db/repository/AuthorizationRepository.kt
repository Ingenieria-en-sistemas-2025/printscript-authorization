package com.printscript.authorization.db.repository

import com.printscript.authorization.db.table.Authorization
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface AuthorizationRepository : JpaRepository<Authorization, String> {
    fun findAllByUserId(userId: String, pageable: Pageable): List<Authorization>
    fun countAllByUserId(userId: String): Int
    fun findByUserIdAndSnippetId(userId: String, snippetId: String): Optional<Authorization>
    fun deleteAllBySnippetId(snippetId: String)

    @Query( // uso JPQL es el SQL de Java
        // para hacer un JOIN para buscar la autorizacion por el nombre del scope
        """
        SELECT a
        FROM Authorization a
        JOIN a.scope s
        WHERE s.name = :scopeName AND a.snippetId = :snippetId
    """,
    )
    fun findByScopeNameAndSnippetId(
        @Param("scopeName") scopeName: String,
        @Param("snippetId") snippetId: String,
    ): Optional<Authorization>
}

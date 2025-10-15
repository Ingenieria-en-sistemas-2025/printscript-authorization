package com.printscript.snippets.db.repository

import com.printscript.snippets.db.table.Authorization
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AuthorizationRepository : JpaRepository<Authorization, String> {
    fun findAllByUserId(userId: String, pageable: Pageable): List<Authorization>
    fun countAllByUserId(userId: String): Int
    fun findByUserIdAndSnippetId(userId: String, snippetId: String): Optional<Authorization>
    fun deleteAllBySnippetId(snippetId: String)
    fun getByScope_NameAndSnippetId(name: String, snippetId: String): Authorization
}
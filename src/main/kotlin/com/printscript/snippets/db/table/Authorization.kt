package com.printscript.snippets.db.table

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class Authorization(
    val snippetId: String,
    val userId: String,

    @ManyToOne
    @JoinColumn(name = "authorization_scope_id")
    val scope: AuthorizationScope
) : CommonFields()
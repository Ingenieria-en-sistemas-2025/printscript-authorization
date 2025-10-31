package com.printscript.authorization.db.table

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "\"authorization\"")
data class Authorization(
    val snippetId: String,
    val userId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorization_scope_id")
    val scope: AuthorizationScope,
) : CommonFields()

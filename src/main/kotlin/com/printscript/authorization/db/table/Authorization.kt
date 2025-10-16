package com.printscript.authorization.db.table

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "\"AUTHORIZATION\"") // para que H2 no lo interprete como keyword
data class Authorization(
    val snippetId: String,
    val userId: String,

    @ManyToOne
    @JoinColumn(name = "authorization_scope_id")
    val scope: AuthorizationScope,
) : CommonFields()

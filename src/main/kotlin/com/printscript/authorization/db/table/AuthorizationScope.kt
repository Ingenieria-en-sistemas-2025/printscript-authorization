package com.printscript.authorization.db.table

import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
data class AuthorizationScope(
    val name: String,

    @OneToMany(mappedBy = "scope")
    val authorizations: List<Authorization>? = null,
) : CommonFields()

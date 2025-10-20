package com.printscript.authorization.db.table

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "authorization_scope")
data class AuthorizationScope(
    @Column(name = "name", nullable = false, unique = true, length = 100)
    val name: String,

    @JsonIgnore
    @OneToMany(mappedBy = "scope", fetch = FetchType.LAZY)
    val authorizations: List<Authorization> = emptyList(),
) : CommonFields()

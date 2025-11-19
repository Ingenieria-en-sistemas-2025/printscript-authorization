package com.printscript.authorization.db.table

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "authorization_scope")
data class AuthorizationScope(

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    val name: AuthorizationScopeType,

    @JsonIgnore
    @OneToMany(mappedBy = "scope", fetch = FetchType.LAZY)
    val authorizations: List<Authorization> = emptyList(),
) : CommonFields()

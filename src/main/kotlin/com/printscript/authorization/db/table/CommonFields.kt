package com.printscript.authorization.db.table

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@MappedSuperclass
abstract class CommonFields(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    open val id: String? = null,

    @Column(updatable = false)
    @CreationTimestamp
    open val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    open val updatedAt: LocalDateTime? = null,
)

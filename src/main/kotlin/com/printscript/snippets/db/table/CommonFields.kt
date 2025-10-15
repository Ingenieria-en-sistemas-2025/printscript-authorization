package com.printscript.snippets.db.table

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
    val id: String? = null,

    @Column(updatable = false)
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null
)
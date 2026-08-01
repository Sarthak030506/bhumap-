package com.bhumap.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Land(
    val id: String,
    val name: String,
    val location: String,
    val areaAcres: Double,
    val totalCost: Double,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class Farmer(
    val id: String,
    val landId: String,
    val name: String,
    val phone: String,
    val aadhaar: String? = null,
    val totalAgreed: Double,
    val totalPaid: Double,
    val createdAt: String,
    val updatedAt: String,
) {
    val remaining: Double get() = totalAgreed - totalPaid
}

@Serializable
data class Partner(

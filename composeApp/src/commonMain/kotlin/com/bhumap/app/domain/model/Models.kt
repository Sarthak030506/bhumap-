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
    val id: String,
    val landId: String,
    val name: String,
    val phone: String,
    val committedAmount: Double,
    val paidAmount: Double,
    val profitSharePct: Double,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
) {
    val remaining: Double get() = committedAmount - paidAmount
}

@Serializable
data class Plot(
    val id: String,
    val landId: String,
    val plotNumber: String,
    val areaSqft: Double,
    val status: PlotStatus,
    val boundaryJson: String? = null,
    val pricePerSqft: Double? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
) {
    val totalPrice: Double? get() = pricePerSqft?.let { it * areaSqft }
}

enum class PlotStatus(val label: String) {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    SOLD_PENDING("Sold - Pending"),
    SOLD_PAID("Sold - Paid"),
    BLOCKED("Blocked"),
}

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val aadhaar: String? = null,
    val address: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class Sale(
    val id: String,
    val plotId: String,
    val customerId: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentType: PaymentType,
    val emiMonths: Int? = null,
    val emiAmount: Double? = null,
    val saleDate: String,
    val status: SaleStatus,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
) {

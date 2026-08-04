package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.PaymentType
import com.bhumap.app.domain.model.Sale
import com.bhumap.app.domain.model.SaleStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SaleRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.saleQueries

    /** Observe single Sale by saleId */
    fun observeBySaleId(saleId: String): Flow<Sale?> =
        queries.selectById(saleId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() }

    /** Observe single Sale by plotId */
    fun observeByPlotId(plotId: String): Flow<Sale?> =
        queries.selectByPlot(plotId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() }

    /** Observe all Sales by customerId */
    fun observeByCustomerId(customerId: String): Flow<List<Sale>> =
        queries.selectByCustomer(customerId)
            .asFlow()
            .map { query -> query.executeAsList().map { it.toDomain() } }

    /** Fetch sales from Supabase and upsert into local SQLDelight DB */
    suspend fun sync() {
        runCatching {
            val remote = supabase.postgrest["sales"]
                .select()
                .decodeList<RemoteSale>()

            println("BhumapApp SaleRepository.sync(): Fetched ${remote.size} sales from Supabase")

            remote.forEach { s ->
                queries.upsert(
                    id           = s.id,
                    plot_id      = s.plotId,
                    customer_id  = s.customerId,
                    total_amount = s.totalAmount,
                    paid_amount  = s.paidAmount,
                    payment_type = s.paymentType,
                    emi_months   = s.emiMonths?.toLong(),
                    emi_amount   = s.emiAmount,
                    sale_date    = s.saleDate,
                    status       = s.status,
                    notes        = s.notes,
                    created_at   = s.createdAt,
                    updated_at   = s.updatedAt,
                )
            }
        }.onFailure { e ->
            println("BhumapApp SaleRepository.sync() error: ${e.message}")
        }
    }

    /**
     * LOCAL-FIRST insert: write to local SQLDelight FIRST, then push to Supabase.
     */
    suspend fun insert(sale: Sale) {
        // 1. Write local
        queries.upsert(
            id           = sale.id,
            plot_id      = sale.plotId,
            customer_id  = sale.customerId,
            total_amount = sale.totalAmount,
            paid_amount  = sale.paidAmount,
            payment_type = sale.paymentType.name.lowercase(),
            emi_months   = sale.emiMonths?.toLong(),
            emi_amount   = sale.emiAmount,
            sale_date    = sale.saleDate,
            status       = sale.status.name.lowercase(),
            notes        = sale.notes,
            created_at   = sale.createdAt,
            updated_at   = sale.updatedAt,
        )
        println("BhumapApp SaleRepository.insert(): Saved locally (id=${sale.id})")

        // 2. Push remote
        val remoteDto = RemoteSale(
            id          = sale.id,
            plotId      = sale.plotId,
            customerId  = sale.customerId,
            totalAmount = sale.totalAmount,
            paidAmount  = sale.paidAmount,
            paymentType = sale.paymentType.name.lowercase(),
            emiMonths   = sale.emiMonths,
            emiAmount   = sale.emiAmount,
            saleDate    = sale.saleDate,
            status      = sale.status.name.lowercase(),
            notes       = sale.notes,
            createdAt   = sale.createdAt,
            updatedAt   = sale.updatedAt,
        )
        supabase.postgrest["sales"].insert(remoteDto)
        println("BhumapApp SaleRepository.insert(): Pushed to Supabase (id=${sale.id})")
    }

    /** Update total paid and status on both local DB and remote Supabase */
    suspend fun updateTotalPaid(
        saleId: String,
        totalPaid: Double,
        pendingAmount: Double,
        status: String,
    ) {
        val existing = queries.selectById(saleId).executeAsOneOrNull() ?: return
        val updated = existing.copy(
            paid_amount = totalPaid,
            status      = status,
        )

        // 1. Local update
        queries.upsert(
            id           = updated.id,
            plot_id      = updated.plot_id,
            customer_id  = updated.customer_id,
            total_amount = updated.total_amount,
            paid_amount  = updated.paid_amount,
            payment_type = updated.payment_type,
            emi_months   = updated.emi_months,
            emi_amount   = updated.emi_amount,
            sale_date    = updated.sale_date,
            status       = updated.status,
            notes        = updated.notes,
            created_at   = updated.created_at,
            updated_at   = updated.updated_at,
        )

        // 2. Remote update
        runCatching {
            supabase.postgrest["sales"].update({
                set("total_paid", totalPaid)
                set("status", status)
            }) {
                filter { eq("id", saleId) }
            }
        }
    }
}

private fun com.bhumap.app.data.local.db.Sale.toDomain() = Sale(
    id          = id,
    plotId      = plot_id,
    customerId  = customer_id,
    totalAmount = total_amount,
    paidAmount  = paid_amount,
    paymentType = parsePaymentType(payment_type),
    emiMonths   = emi_months?.toInt(),
    emiAmount   = emi_amount,
    saleDate    = sale_date,
    status      = parseSaleStatus(status),
    notes       = notes,
    createdAt   = created_at,
    updatedAt   = updated_at,
)

private fun parsePaymentType(type: String): PaymentType = when (type.lowercase()) {
    "outright" -> PaymentType.OUTRIGHT
    else       -> PaymentType.EMI
}

private fun parseSaleStatus(status: String): SaleStatus = when (status.lowercase()) {
    "completed" -> SaleStatus.COMPLETED
    "cancelled" -> SaleStatus.CANCELLED
    else        -> SaleStatus.ACTIVE
}

@Serializable
private data class RemoteSale(
    @SerialName("id")            val id: String,
    @SerialName("plot_id")       val plotId: String,
    @SerialName("customer_id")   val customerId: String,
    @SerialName("sale_price")    val totalAmount: Double,
    @SerialName("total_paid")    val paidAmount: Double = 0.0,
    @SerialName("payment_type")  val paymentType: String = "emi",
    @SerialName("emi_months")    val emiMonths: Int? = null,
    @SerialName("emi_amount")    val emiAmount: Double? = null,
    @SerialName("sale_date")     val saleDate: String,
    @SerialName("status")        val status: String = "active",
    @SerialName("notes")         val notes: String? = null,
    @SerialName("created_at")    val createdAt: String,
    @SerialName("updated_at")    val updatedAt: String,
)

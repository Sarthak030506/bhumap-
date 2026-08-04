package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.EmiSchedule
import com.bhumap.app.domain.model.EmiStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.floor
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EmiRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.emiScheduleQueries

    /** Observe EMI schedule for a sale ordered by installment_no */
    fun observeBySaleId(saleId: String): Flow<List<EmiSchedule>> =
        queries.selectBySale(saleId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    EmiSchedule(
                        id            = row.id,
                        saleId        = row.sale_id,
                        installmentNo = row.installment_no.toInt(),
                        dueDate       = row.due_date,
                        amount        = row.amount,
                        status        = parseEmiStatus(row.status),
                        paidDate      = row.paid_date,
                        txnId         = row.txn_id,
                    )
                }
            }

    /** Fetch EMI schedules from Supabase and upsert into local SQLDelight DB */
    suspend fun sync() {
        runCatching {
            val remote = supabase.postgrest["emi_schedule"]
                .select()
                .decodeList<RemoteEmi>()

            println("BhumapApp EmiRepository.sync(): Fetched ${remote.size} EMIs from Supabase")

            remote.forEach { e ->
                queries.upsert(
                    id             = e.id,
                    sale_id        = e.saleId,
                    installment_no = e.installmentNo.toLong(),
                    due_date       = e.dueDate,
                    amount         = e.amount,
                    status         = e.status,
                    paid_date      = e.paidDate,
                    txn_id         = e.txnId,
                )
            }
        }.onFailure { err ->
            println("BhumapApp EmiRepository.sync() error: ${err.message}")
        }
    }

    /**
     * Generate an EMI schedule for a sale.
     * Calculates base EMI amount as floor(principal / numEmis) and adds any rounding remainder to the final EMI.
     * Local-first: Writes all rows to SQLDelight FIRST, then pushes to Supabase.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun generateSchedule(
        saleId: String,
        principal: Double,
        numEmis: Int,
        startDate: String,
    ) {
        if (numEmis <= 0) return

        val baseEmi = floor(principal / numEmis)
        val remainder = principal - (baseEmi * numEmis)

        val scheduleList = mutableListOf<EmiSchedule>()

        for (i in 1..numEmis) {
            val emiAmount = if (i == numEmis) baseEmi + remainder else baseEmi
            val dueDate = calculateDueDate(startDate, i - 1)
            val emiId = Uuid.random().toString()

            val emi = EmiSchedule(
                id            = emiId,
                saleId        = saleId,
                installmentNo = i,
                dueDate       = dueDate,
                amount        = emiAmount,
                status        = EmiStatus.PENDING,
            )
            scheduleList.add(emi)

            // 1. Write local
            queries.upsert(
                id             = emi.id,
                sale_id        = emi.saleId,
                installment_no = emi.installmentNo.toLong(),
                due_date       = emi.dueDate,
                amount         = emi.amount,
                status         = emi.status.name.lowercase(),
                paid_date      = null,
                txn_id         = null,
            )
        }
        println("BhumapApp EmiRepository.generateSchedule(): Generated ${scheduleList.size} EMIs locally for sale=$saleId")

        // 2. Push remote
        val remoteDtos = scheduleList.map { e ->
            RemoteEmi(
                id            = e.id,
                saleId        = e.saleId,
                installmentNo = e.installmentNo,
                dueDate       = e.dueDate,
                amount        = e.amount,
                status        = e.status.name.lowercase(),
                paidDate      = null,
                txnId         = null,
            )
        }
        runCatching {
            supabase.postgrest["emi_schedule"].insert(remoteDtos)
            println("BhumapApp EmiRepository.generateSchedule(): Pushed ${remoteDtos.size} EMIs to Supabase")
        }.onFailure { err ->
            println("BhumapApp EmiRepository.generateSchedule() remote push error: ${err.message}")
        }
    }

    /** Mark an EMI as paid on local DB first, then push to Supabase */
    suspend fun markPaid(emiId: String, paidAt: String, txnId: String? = null) {
        // 1. Write local
        queries.markPaid(
            paid_date = paidAt,
            txn_id    = txnId,
            id        = emiId,
        )
        println("BhumapApp EmiRepository.markPaid(): Marked paid locally (id=$emiId)")

        // 2. Push remote
        runCatching {
            supabase.postgrest["emi_schedule"].update({
                set("status", "paid")
                set("paid_date", paidAt)
                if (txnId != null) set("payment_id", txnId)
            }) {
                filter { eq("id", emiId) }
            }
            println("BhumapApp EmiRepository.markPaid(): Pushed to Supabase (id=$emiId)")
        }
    }
}

/** Simple date string arithmetic helper for YYYY-MM-DD strings */
private fun calculateDueDate(startDateStr: String, addMonths: Int): String {
    val parts = startDateStr.split("-")
    if (parts.size < 3) return startDateStr
    var year = parts[0].toIntOrNull() ?: 2026
    var month = parts[1].toIntOrNull() ?: 1
    val day = parts[2].toIntOrNull() ?: 1

    month += addMonths
    while (month > 12) {
        month -= 12
        year += 1
    }

    return "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun parseEmiStatus(status: String): EmiStatus = when (status.lowercase()) {
    "paid"    -> EmiStatus.PAID
    "overdue" -> EmiStatus.OVERDUE
    else      -> EmiStatus.PENDING
}

@Serializable
private data class RemoteEmi(
    @SerialName("id")                 val id: String,
    @SerialName("sale_id")            val saleId: String,
    @SerialName("installment_number") val installmentNo: Int,
    @SerialName("due_date")           val dueDate: String,
    @SerialName("amount")             val amount: Double,
    @SerialName("status")             val status: String = "pending",
    @SerialName("paid_date")          val paidDate: String? = null,
    @SerialName("payment_id")         val txnId: String? = null,
)

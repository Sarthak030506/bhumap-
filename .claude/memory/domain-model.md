# Domain Model — BhuMap KMP

Source of truth: `composeApp/src/commonMain/kotlin/com/bhumap/app/domain/model/Models.kt`
All models are `@Serializable` data classes (kotlinx.serialization) — serialized to/from Supabase PostgREST JSON.

---

## Entity Map

```
land ──< farmer        (one land has many farmers)
land ──< partner       (one land has many investment partners)
land ──< plot          (one land is subdivided into many plots)
plot ──< sale          (one plot has at most one active sale)
sale ──< emi_schedule  (one sale has many installments)
sale ──< txn           (transactions reference the sale)
customer >── sale      (one customer can have many sales)
txn  — entity_type + entity_id (polymorphic: farmer|partner|customer)
```

---

## Land

**Kotlin:** `data class Land` — `Models.kt:6–15`
**SQLDelight table:** `land` — `Land.sq`
**Supabase table:** `lands`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | UUID string |
| `name` | `name` | `TEXT` | Land parcel name |
| `location` | `location` | `TEXT` | Village / taluka description |
| `areaAcres` | `area_acres` | `REAL` | Total area in acres |
| `totalCost` | `total_cost` | `REAL` | Agreed acquisition price (₹) |
| `notes` | `notes` | `TEXT?` | Optional |
| `createdAt` | `created_at` | `TEXT` | ISO 8601 string |
| `updatedAt` | `updated_at` | `TEXT` | ISO 8601 string |

**SQLDelight queries:** `selectAll`, `selectById`, `upsert`, `delete`, `count`

---

## Farmer

**Kotlin:** `data class Farmer` — `Models.kt:17–30`
**SQLDelight table:** `farmer` — `Farmer.sq`
**Supabase table:** `farmers`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `landId` | `land_id` | `TEXT` FK→land | CASCADE delete |
| `name` | `name` | `TEXT` | |
| `phone` | `phone` | `TEXT` | |
| `aadhaar` | `aadhaar` | `TEXT?` | |
| `totalAgreed` | `total_agreed` | `REAL` | Total acquisition cost agreed with farmer |
| `totalPaid` | `total_paid` | `REAL` | Running total paid to farmer |
| `createdAt` | `created_at` | `TEXT` | |
| `updatedAt` | `updated_at` | `TEXT` | |

**Computed:** `remaining = totalAgreed - totalPaid` (Kotlin property, not stored)
**SQLDelight queries:** `selectByLand`, `upsert`, `delete`

---

## Partner

**Kotlin:** `data class Partner` — `Models.kt:32–46`
**SQLDelight table:** `partner` — `Partner.sq`
**Supabase table:** `partners`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `landId` | `land_id` | `TEXT` FK→land | CASCADE delete |
| `name` | `name` | `TEXT` | |
| `phone` | `phone` | `TEXT` | |
| `committedAmount` | `committed_amount` | `REAL` | Investment committed |
| `paidAmount` | `paid_amount` | `REAL` | Paid so far |
| `profitSharePct` | `profit_share_pct` | `REAL` | % of land profit (0–100) |
| `notes` | `notes` | `TEXT?` | |
| `createdAt` | `created_at` | `TEXT` | |
| `updatedAt` | `updated_at` | `TEXT` | |

**Computed:** `remaining = committedAmount - paidAmount` (Kotlin property)
**SQLDelight queries:** `selectByLand`, `upsert`, `delete`

---

## Plot

**Kotlin:** `data class Plot` + `enum class PlotStatus` — `Models.kt:48–70`
**SQLDelight table:** `plot` — `Plot.sq`
**Supabase table:** `plots`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `landId` | `land_id` | `TEXT` FK→land | CASCADE delete |
| `plotNumber` | `plot_number` | `TEXT` | Human-readable label |
| `areaSqft` | `area_sqft` | `REAL` | Area in sq ft |
| `status` | `status` | `TEXT` | See PlotStatus below |
| `boundaryJson` | `boundary_json` | `TEXT?` | GeoJSON polygon as raw TEXT — `[[lng,lat],[lng,lat],...]` |
| `pricePerSqft` | `price_per_sqft` | `REAL?` | |
| `notes` | `notes` | `TEXT?` | |
| `createdAt` | `created_at` | `TEXT` | |
| `updatedAt` | `updated_at` | `TEXT` | |

**PlotStatus values:** `AVAILABLE`, `RESERVED`, `SOLD_PENDING`, `SOLD_PAID`, `BLOCKED`
(stored as lowercase strings in DB: `available`, `reserved`, `sold_pending`, `sold_paid`, `blocked`)

**Computed:** `totalPrice = pricePerSqft * areaSqft` (Kotlin property, nullable)
**SQLDelight queries:** `selectByLand`, `selectById`, `upsert`, `updateStatus`, `countByStatus`

---

## Customer

**Kotlin:** `data class Customer` — `Models.kt:72–82`
**SQLDelight table:** `customer` — `Customer.sq`
**Supabase table:** `customers`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `name` | `name` | `TEXT` | |
| `phone` | `phone` | `TEXT` | |
| `email` | `email` | `TEXT?` | |
| `aadhaar` | `aadhaar` | `TEXT?` | |
| `address` | `address` | `TEXT?` | |
| `createdAt` | `created_at` | `TEXT` | |
| `updatedAt` | `updated_at` | `TEXT` | |

**SQLDelight queries:** `selectAll`, `selectById`, `search`, `upsert`, `count`

---

## Sale

**Kotlin:** `data class Sale` + `enum class PaymentType` + `enum class SaleStatus` — `Models.kt:84–104`
**SQLDelight table:** `sale` — `Sale.sq`
**Supabase table:** `sales`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `plotId` | `plot_id` | `TEXT` FK→plot | |
| `customerId` | `customer_id` | `TEXT` FK→customer | |
| `totalAmount` | `total_amount` | `REAL` | Full agreed sale price |
| `paidAmount` | `paid_amount` | `REAL` DEFAULT 0 | Running total received |
| `paymentType` | `payment_type` | `TEXT` | `emi` or `outright` |
| `emiMonths` | `emi_months` | `INTEGER?` | Number of installments |
| `emiAmount` | `emi_amount` | `REAL?` | Per-installment amount |
| `saleDate` | `sale_date` | `TEXT` | |
| `status` | `status` | `TEXT` | `active`, `completed`, `cancelled` |
| `notes` | `notes` | `TEXT?` | |
| `createdAt` | `created_at` | `TEXT` | |
| `updatedAt` | `updated_at` | `TEXT` | |

**Computed:** `remaining = totalAmount - paidAmount`
**SQLDelight queries:** `selectAll`, `selectById`, `selectByCustomer`, `selectByPlot`, `upsert`

---

## Transaction (txn)

**Kotlin:** `data class Transaction` + `enum class EntityType` + `enum class PaymentMode` — `Models.kt:106–121`
**SQLDelight table:** `txn` — `Transaction.sq`
**Supabase table:** `transactions`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `entityType` | `entity_type` | `TEXT` | `farmer`, `partner`, or `customer` |
| `entityId` | `entity_id` | `TEXT` | FK to whichever entity_type |
| `saleId` | `sale_id` | `TEXT?` | Present when payment tied to a sale |
| `amount` | `amount` | `REAL` | ₹ amount |
| `paymentMode` | `payment_mode` | `TEXT` | `cash`, `upi`, `cheque`, `transfer` |
| `referenceNo` | `reference_no` | `TEXT?` | Cheque no, UPI ref, etc. |
| `paymentDate` | `payment_date` | `TEXT` | |
| `notes` | `notes` | `TEXT?` | |
| `createdAt` | `created_at` | `TEXT` | Insert-only — no updatedAt |

**Pattern:** Polymorphic entity reference via `entity_type + entity_id`.
**SQLDelight queries:** `selectBySale`, `selectByEntity`, `insert`, `sumBySale`

---

## EmiSchedule

**Kotlin:** `data class EmiSchedule` + `enum class EmiStatus` — `Models.kt:123–135`
**SQLDelight table:** `emi_schedule` — `EmiSchedule.sq`
**Supabase table:** `emi_schedules`

| Kotlin field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `id` | `TEXT` PK | |
| `saleId` | `sale_id` | `TEXT` FK→sale | CASCADE delete |
| `installmentNo` | `installment_no` | `INTEGER` | 1-based |
| `dueDate` | `due_date` | `TEXT` | ISO 8601 date |
| `amount` | `amount` | `REAL` | |
| `status` | `status` | `TEXT` | `pending`, `paid`, `overdue` |
| `paidDate` | `paid_date` | `TEXT?` | Set when `markPaid` called |
| `txnId` | `txn_id` | `TEXT?` | FK to `txn.id` when paid |

**EmiStatus values:** `PENDING`, `PAID`, `OVERDUE`
**SQLDelight queries:** `selectBySale`, `selectOverdue`, `selectPendingByDueDate`, `markPaid`, `upsert`, `countPendingBySale`

# Schema — BhuMap SQLDelight

All files live under:
`composeApp/src/commonMain/sqldelight/com/bhumap/app/data/local/db/`

Database name: `BhumapDatabase` (configured in `composeApp/build.gradle.kts:170`)
Generated package: `com.bhumap.app.data.local.db`
Schema output dir: `src/commonMain/sqldelight/databases/` (migration tracking, `verifyMigrations=true`)

---

## Land.sq

```sql
CREATE TABLE IF NOT EXISTS land (
    id          TEXT    NOT NULL PRIMARY KEY,
    name        TEXT    NOT NULL,
    location    TEXT    NOT NULL,
    area_acres  REAL    NOT NULL,
    total_cost  REAL    NOT NULL,
    notes       TEXT,
    created_at  TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL
);
```

**Named queries:**
- `selectAll` — `SELECT * FROM land ORDER BY created_at DESC`
- `selectById` — `WHERE id = :id`
- `upsert` — `INSERT OR REPLACE` — all columns
- `delete` — `WHERE id = :id`
- `count` — `SELECT COUNT(*) FROM land`

---

## Plot.sq

```sql
CREATE TABLE IF NOT EXISTS plot (
    id              TEXT    NOT NULL PRIMARY KEY,
    land_id         TEXT    NOT NULL,
    plot_number     TEXT    NOT NULL,
    area_sqft       REAL    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'available',
    -- status: available | reserved | sold_pending | sold_paid | blocked
    boundary_json   TEXT,   -- GeoJSON polygon stored as TEXT
    price_per_sqft  REAL,
    notes           TEXT,
    created_at      TEXT    NOT NULL,
    updated_at      TEXT    NOT NULL,
    FOREIGN KEY (land_id) REFERENCES land(id) ON DELETE CASCADE
);
```

**Named queries:**
- `selectByLand` — `WHERE land_id = :land_id ORDER BY plot_number`
- `selectById` — `WHERE id = :id`
- `upsert` — `INSERT OR REPLACE` — all columns
- `updateStatus` — `SET status = :status, updated_at = :updated_at WHERE id = :id`
- `countByStatus` — `SELECT status, COUNT(*) AS cnt ... GROUP BY status` — for KPI badges

---

## Farmer.sq

```sql
CREATE TABLE IF NOT EXISTS farmer (
    id           TEXT    NOT NULL PRIMARY KEY,
    land_id      TEXT    NOT NULL,
    name         TEXT    NOT NULL,
    phone        TEXT    NOT NULL,
    aadhaar      TEXT,
    total_agreed REAL    NOT NULL DEFAULT 0,
    total_paid   REAL    NOT NULL DEFAULT 0,
    created_at   TEXT    NOT NULL,
    updated_at   TEXT    NOT NULL,
    FOREIGN KEY (land_id) REFERENCES land(id) ON DELETE CASCADE
);
```

**Named queries:**
- `selectByLand` — `WHERE land_id = :land_id`
- `upsert` — `INSERT OR REPLACE` — all columns
- `delete` — `WHERE id = :id`

---

## Partner.sq

```sql
CREATE TABLE IF NOT EXISTS partner (
    id               TEXT    NOT NULL PRIMARY KEY,
    land_id          TEXT    NOT NULL,
    name             TEXT    NOT NULL,
    phone            TEXT    NOT NULL,
    committed_amount REAL    NOT NULL DEFAULT 0,
    paid_amount      REAL    NOT NULL DEFAULT 0,
    profit_share_pct REAL    NOT NULL DEFAULT 0,
    notes            TEXT,
    created_at       TEXT    NOT NULL,
    updated_at       TEXT    NOT NULL,
    FOREIGN KEY (land_id) REFERENCES land(id) ON DELETE CASCADE
);
```

**Named queries:**
- `selectByLand` — `WHERE land_id = :land_id`
- `upsert` — `INSERT OR REPLACE` — all columns
- `delete` — `WHERE id = :id`

---

## Customer.sq

```sql
CREATE TABLE IF NOT EXISTS customer (
    id          TEXT    NOT NULL PRIMARY KEY,
    name        TEXT    NOT NULL,
    phone       TEXT    NOT NULL,
    email       TEXT,
    aadhaar     TEXT,
    address     TEXT,
    created_at  TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL
);
```

**Named queries:**
- `selectAll` — `ORDER BY name`
- `selectById` — `WHERE id = :id`
- `search` — `WHERE name LIKE :query OR phone LIKE :query`
- `upsert` — `INSERT OR REPLACE` — all columns
- `count` — `SELECT COUNT(*) FROM customer`

---

## Sale.sq

```sql
CREATE TABLE IF NOT EXISTS sale (
    id              TEXT    NOT NULL PRIMARY KEY,
    plot_id         TEXT    NOT NULL,
    customer_id     TEXT    NOT NULL,
    total_amount    REAL    NOT NULL,
    paid_amount     REAL    NOT NULL DEFAULT 0,
    payment_type    TEXT    NOT NULL DEFAULT 'emi',  -- emi | outright
    emi_months      INTEGER,
    emi_amount      REAL,
    sale_date       TEXT    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'active',  -- active | completed | cancelled
    notes           TEXT,
    created_at      TEXT    NOT NULL,
    updated_at      TEXT    NOT NULL,
    FOREIGN KEY (plot_id)     REFERENCES plot(id),
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);
```

**Named queries:**
- `selectAll` — `ORDER BY sale_date DESC`
- `selectById` — `WHERE id = :id`
- `selectByCustomer` — `WHERE customer_id = :customer_id`
- `selectByPlot` — `WHERE plot_id = :plot_id LIMIT 1`
- `upsert` — `INSERT OR REPLACE` — all columns

---

## Transaction.sq

Table name in DB: **`txn`** (not `transaction` — reserved word conflict avoided)

```sql
CREATE TABLE IF NOT EXISTS txn (
    id              TEXT    NOT NULL PRIMARY KEY,
    entity_type     TEXT    NOT NULL,  -- farmer | partner | customer
    entity_id       TEXT    NOT NULL,
    sale_id         TEXT,
    amount          REAL    NOT NULL,
    payment_mode    TEXT    NOT NULL,  -- cash | upi | cheque | transfer
    reference_no    TEXT,
    payment_date    TEXT    NOT NULL,
    notes           TEXT,
    created_at      TEXT    NOT NULL
    -- NO updated_at — transactions are insert-only
);
```

**Named queries:**
- `selectBySale` — `WHERE sale_id = :sale_id ORDER BY payment_date DESC`
- `selectByEntity` — `WHERE entity_type = :entity_type AND entity_id = :entity_id`
- `insert` — plain `INSERT INTO txn` (no REPLACE — transactions are immutable)
- `sumBySale` — `SELECT COALESCE(SUM(amount), 0) FROM txn WHERE sale_id = :sale_id`

---

## EmiSchedule.sq

```sql
CREATE TABLE IF NOT EXISTS emi_schedule (
    id              TEXT    NOT NULL PRIMARY KEY,
    sale_id         TEXT    NOT NULL,
    installment_no  INTEGER NOT NULL,
    due_date        TEXT    NOT NULL,
    amount          REAL    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'pending',  -- pending | paid | overdue
    paid_date       TEXT,
    txn_id          TEXT,
    FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE
);
```

**Named queries:**
- `selectBySale` — `WHERE sale_id = :sale_id ORDER BY installment_no`
- `selectOverdue` — `WHERE status = 'overdue' ORDER BY due_date`
- `selectPendingByDueDate` — `WHERE status = 'pending' AND due_date <= :cutoff`
- `markPaid` — `SET status='paid', paid_date=:paid_date, txn_id=:txn_id WHERE id=:id`
- `upsert` — `INSERT OR REPLACE` — all columns
- `countPendingBySale` — `SELECT COUNT(*) ... WHERE sale_id = :sale_id AND status = 'pending'`

---

## FK / Cascade Summary

| Child table | Parent | On delete |
|---|---|---|
| `plot` | `land` | CASCADE |
| `farmer` | `land` | CASCADE |
| `partner` | `land` | CASCADE |
| `sale` | `plot` | (no action specified) |
| `sale` | `customer` | (no action specified) |
| `emi_schedule` | `sale` | CASCADE |

## How Repositories Use Generated Code

```kotlin
// Access via BhumapDatabase (generated by SQLDelight):
val db: BhumapDatabase   // injected by Koin
db.landQueries.selectAll().asFlow().mapToList(Dispatchers.IO)
db.plotQueries.selectByLand(landId).asFlow().mapToList(Dispatchers.IO)
db.customerQueries.upsert(id, name, phone, email, aadhaar, address, createdAt, updatedAt)
db.emiScheduleQueries.markPaid(paidDate, txnId, id)
```

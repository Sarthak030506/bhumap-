# Payment Matching — How Transactions Map to EMI Rows

---

## Relationship

```
Sale ──< Transactions (many — each payment event)
Sale ──< EMI Schedule (many — generated rows)

Transaction → covers → EMI Schedule rows (admin decides which rows it covers)
EMI Schedule row → linked_to → Transaction (via transaction_id FK)
```

A single transaction can cover one or more EMI rows.
An EMI row is covered by exactly one transaction (once paid).

---

## Recording a Payment (Admin Flow)

1. Admin selects a sale
2. Admin taps `[Record Payment]`
3. Admin enters: amount, payment_date, payment_method, optional notes
4. App shows pending EMI rows for that sale
5. Admin selects which EMI row(s) this payment covers
6. App inserts transaction row
7. App updates each selected EMI row:
   - `status = 'paid'`
   - `paid_date = payment_date`
   - `transaction_id = new_transaction.id`

---

## Cases

### Case 1: Exact payment (one installment)
- Transaction amount = EMI amount
- Admin selects that one EMI row
- Mark it paid, link transaction_id

### Case 2: Payment covers multiple installments
- Buyer pays 3 months at once
- Admin selects 3 EMI rows
- One transaction, three EMI rows all updated to paid
- Validation: transaction.amount should equal sum of selected EMI amounts (warn if different, don't block)

### Case 3: Partial payment (amount < EMI amount)
- Buyer pays ₹15,000 against an EMI of ₹20,000
- **Current behavior (v1):** Admin records transaction, optionally links to EMI row
  - If linked: EMI row shows paid (even though under-paid) — this is a known gap
  - If not linked: transaction recorded, EMI stays pending
- **Mitigation:** Add `amount_received` field to `emi_schedule` in v2
  - `amount_received` allows tracking partial payments
  - `status = 'partial'` — new enum value to add if needed

### Case 4: Overpayment
- Transaction amount > EMI amount
- Admin may link it to multiple EMI rows that together equal or exceed the amount
- Excess is admin's problem — no auto-credit system in v1

### Case 5: Lump sum sale payment
- No EMI rows
- Admin records transaction(s) against the sale directly
- No EMI row to link — transaction.sale_id is enough
- Fully paid is tracked by: `SUM(transactions.amount) >= sales.sale_price`

---

## Data Integrity Rules

| Rule | Enforcement |
|------|------------|
| Transaction is append-only | RLS: no UPDATE or DELETE for transactions |
| One EMI row → one transaction | FK `emi_schedule.transaction_id` is nullable but should be unique once set |
| EMI status 'paid' is terminal | App-level guard: cannot un-pay an EMI row |
| Correction = new negative transaction | No deletes; corrections recorded as new transactions with negative amounts + notes |

---

## Outstanding Balance Calculation

```typescript
// For a given sale:
const totalPaid = transactions.reduce((sum, t) => sum + t.amount, 0);
const outstanding = sale.sale_price - totalPaid;

// For EMI-specific view:
const paidInstallments = emiSchedule.filter(e => e.status === 'paid').length;
const pendingInstallments = emiSchedule.filter(e => e.status === 'pending').length;
const overdueInstallments = emiSchedule.filter(e => e.status === 'overdue').length;
const nextDue = emiSchedule
  .filter(e => e.status === 'pending' || e.status === 'overdue')
  .sort((a, b) => new Date(a.due_date).getTime() - new Date(b.due_date).getTime())[0];
```

---

## React Query Hook Pattern

```typescript
// hooks/useSalePayments.ts
export const useSalePayments = (saleId: string) =>
  useQuery({
    queryKey: ['sale-payments', saleId],
    queryFn: () => supabase
      .from('transactions')
      .select('*')
      .eq('sale_id', saleId)
      .order('payment_date', { ascending: false }),
  });

export const useSaleEmi = (saleId: string) =>
  useQuery({
    queryKey: ['sale-emi', saleId],
    queryFn: () => supabase
      .from('emi_schedule')
      .select('*')
      .eq('sale_id', saleId)
      .order('installment_number', { ascending: true }),
  });
```

# EMI Calculation — Formula and Schedule Generation

---

## Core Formula

This platform uses a **flat/simple installment plan**, not a reducing-balance loan EMI.
The sale_price is divided evenly across months, with the last installment absorbing any rounding remainder.

```
monthly_amount = FLOOR(sale_price / emi_months * 100) / 100
last_amount    = sale_price - (monthly_amount * (emi_months - 1))
```

### Example

- `sale_price = ₹10,00,001`
- `emi_months = 3`

```
monthly_amount = FLOOR(1000001 / 3 * 100) / 100 = FLOOR(33333366.67) / 100 = 333333.00

Wait — let me recalculate:
monthly_amount = FLOOR((1000001 / 3) × 100) / 100
               = FLOOR(33333366.67) / 100
               ← WRONG: this should be FLOOR(amount * 100) / 100 to get 2dp truncation

Correct:
monthly_amount = Math.floor((1000001 / 3) * 100) / 100
               = Math.floor(333333.6667 * 100) / 100
               = Math.floor(33333366.67) / 100
               = 33333366 / 100
               = 333333.66

last_amount = 1000001 - (333333.66 × 2) = 1000001 - 666667.32 = 333333.68
```

Verify: `333333.66 + 333333.66 + 333333.68 = 1000001.00` ✅

---

## Due Date Calculation

- First EMI due = `sale_date + 1 month`
- EMI #n due = `sale_date + n months`
- Rule for month overflow: if sale_date is the 31st, and target month has 28 days → snap to last day of month (Feb 28 or 29)

```typescript
import { addMonths, endOfMonth, setDate, getDaysInMonth } from 'date-fns';

function getEmiDueDate(saleDate: Date, installmentNumber: number): Date {
  const targetDate = addMonths(saleDate, installmentNumber);
  const daysInMonth = getDaysInMonth(targetDate);
  const originalDay = saleDate.getDate();

  if (originalDay > daysInMonth) {
    return endOfMonth(targetDate); // snap to last day
  }
  return setDate(targetDate, originalDay);
}
```

---

## Schedule Generation (Full Algorithm)

```typescript
function generateEmiSchedule(
  saleId: string,
  salePrice: number,
  emiMonths: number,
  saleDate: Date
): EmiScheduleRow[] {
  const monthly = Math.floor((salePrice / emiMonths) * 100) / 100;
  const last = Math.round((salePrice - monthly * (emiMonths - 1)) * 100) / 100;

  return Array.from({ length: emiMonths }, (_, i) => ({
    sale_id: saleId,
    installment_number: i + 1,
    due_date: getEmiDueDate(saleDate, i + 1),
    amount: i === emiMonths - 1 ? last : monthly,
    status: 'pending' as const,
    paid_date: null,
    transaction_id: null,
  }));
}
```

---

## Validation Before Generation

| Check | Rule |
|-------|------|
| `emi_months > 0` | Must be positive integer |
| `sale_price > 0` | Must be positive |
| `sale_price >= emi_months` | Each installment must be ≥ ₹0.01 |
| No duplicate generation | Check `emi_schedule` has 0 rows for `sale_id` before inserting |

---

## Amount Rounding Decision (Locked)

- **Method:** Truncate to 2dp for monthly installments (use `Math.floor`)
- **Last installment:** `Math.round` to absorb accumulated remainder
- This ensures `SUM(emi_schedule.amount) = sale_price` exactly
- Never use `Math.round` for intermediate installments — it can cause total to diverge

---

## Displaying EMI Summary to Buyer

```
Sale Price:     ₹10,00,001
EMI Plan:       3 months
Monthly EMI:    ₹3,33,333.66
Last EMI:       ₹3,33,333.68
Total:          ₹10,00,001.00 ✓
```

Show the last installment amount explicitly if it differs from monthly amount.

# EMI Rules

## EMI Formula
```
monthly  = FLOOR(sale_price / emi_months * 100) / 100
last_emi = sale_price - (monthly × (emi_months - 1))
```
- Last installment absorbs rounding remainder
- All amounts in INR, stored as numeric(15,2)

## Due Date Calculation
- Installment 1 due: sale_date + 1 month
- Installment N due: sale_date + N months
- Month overflow rule: if day doesn't exist in target month, snap to end-of-month
  - e.g., Jan 31 + 1 month = Feb 28/29 (not March 2/3)

## EMI Generation
- Triggered by `generate-emi-schedule` Edge Function at sale creation
- Generates exactly emi_months rows in emi_schedule table
- Must be idempotent: if called twice for same sale_id, no duplicate rows (check first)

## Status Transitions
- Created → `pending`
- Admin marks paid → `paid` (terminal — no reversal)
- Cron job runs nightly → flips `pending` rows past due_date to `overdue`
- `paid` rows are NEVER touched by cron

## Payment Recording
- Admin selects which EMI installment(s) a transaction covers
- Partial payment: transaction recorded, EMI row stays `pending` until fully paid
- Admin manually links transaction_id to emi_schedule row on full payment
- One transaction can cover multiple EMI rows (e.g., bulk catch-up payment)

## Cron Schedule
- Runs nightly (e.g., 00:01 IST)
- Updates: `UPDATE emi_schedule SET status = 'overdue' WHERE status = 'pending' AND due_date < CURRENT_DATE`
- Sends push notification for 7-day-before, day-of, and day-after-overdue events

## Notification Triggers
- 7 days before due_date: reminder notification to buyer
- On due_date: due today notification to buyer
- Day after overdue transition: overdue alert to buyer + admin

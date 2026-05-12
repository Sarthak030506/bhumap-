---
name: emi-and-payments
description: >
  Trigger phrases: EMI, installment, payment, transaction, schedule, overdue, notification,
  commission, partial payment, EMI due, payment recording, EMI reminder, cron, payment match,
  lump sum, monthly payment
---

# EMI and Payments Skill

## When to activate
Load this skill when the conversation involves:
- EMI schedule generation (how many rows, what amounts, what due dates)
- Recording a payment against a sale or EMI installment
- Matching a payment transaction to a specific EMI row
- Overdue detection and status transitions
- Push notification rules for EMI reminders (7-day, day-of, overdue)
- Agent commission payment workflow

## Reference files
| File | Contents |
|------|---------|
| `references/emi-calculation.md` | Exact EMI formula, schedule generation logic, rounding rules |
| `references/payment-matching.md` | How a transaction maps to EMI rows; partial payment handling |
| `references/notification-logic.md` | Cron job rules, alert types, timing, and Expo push delivery |

## Key facts
- EMI amount = `ROUND(sale_price / emi_months, 2)` — last installment absorbs rounding remainder
- Due dates: monthly from sale_date (e.g., sale on Jan 15 → EMIs on Feb 15, Mar 15, ...)
- `emi_schedule.status` transitions: `pending → paid` (never back); admin can mark overdue manually or cron does it automatically
- Transactions are append-only — never edit or delete; corrections are new transactions with notes
- Commission is tracked separately from EMI — it's on the `sales` row, not a transaction

## Design principle
EMI rows are records of obligation. Transactions are records of cash received. They are linked but distinct — a transaction covers one or more EMI rows; an EMI row is satisfied by a transaction reference.

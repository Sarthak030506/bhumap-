# Business Rules — Extracted from Spec

## Plot Status
- Plot created → status = `available`
- Sale created → plot status = `sold`
- Sale cancelled → plot status = `available`
- Optional: `reserved` state for admin holds (not in v1 by default)

## Sale Creation
1. Admin creates sale with: plot_id, buyer_id, sale_price, payment_type, sale_date
2. IF payment_type = 'emi': emi_months required; call `generate-emi-schedule` Edge Function
3. IF agent_id set: call `calculate-commission` Edge Function
4. IF agent_id set AND matching lead exists: set lead.status = 'converted'
5. Set plot.status = 'sold'

## Agent Commission
- commission_amount = ROUND(sale_price × commission_percent / 100, 2)
- Computed by Edge Function at sale creation — never by client
- commission_paid starts as false
- Admin triggers `mark-commission-paid` → commission_paid = true + agents.total_earned incremented
- If sale cancelled before commission paid → commission voided
- If sale cancelled after commission paid → admin handles manually (open question)

## EMI Formula
- monthly = FLOOR(sale_price / emi_months × 100) / 100
- last = sale_price - (monthly × (emi_months - 1))
- Due dates: sale_date + 1 month, +2 months, ... +N months
- Month overflow: snap to end-of-month

## Lead Lifecycle
- interested → site_visit_done → negotiating → converted | lost
- Any status can go to 'lost'
- 'converted' and 'lost' are terminal
- Conversion requires sale.agent_id to match lead.agent_id

## Partner Profit
- profit_share = net_project_profit × (percentage / 100)
- Admin sees per-partner profit via Edge Function (read-only)
- Partner percentages should sum to ≤ 100% (app warns, doesn't block)

## Payments
- Transactions are append-only (no UPDATE, no DELETE)
- Corrections = new transaction with negative amount + notes
- Admin selects which EMI row(s) a transaction covers
- EMI status: pending → overdue (cron) or pending → paid (admin)
- paid is terminal

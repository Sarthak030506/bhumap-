# Sale Rules

## Sale Creation Steps (in order)
1. Admin provides: plot_id, buyer_id, sale_price, payment_type, sale_date
2. IF payment_type = 'emi': emi_months required → call `generate-emi-schedule` Edge Function
3. IF agent_id provided: call `calculate-commission` Edge Function
4. IF agent_id provided AND matching lead exists (buyer_phone match): set lead.status = 'converted'
5. Set plot.status = 'sold'

All steps are atomic — if any fail, rollback everything.

## Sale Cancellation
- Set sale.status = 'cancelled'
- Set plot.status = 'available'
- IF commission NOT yet paid: void commission (commission_amount = 0 or set flag)
- IF commission already paid: admin handles manually — no automated reversal
- EMI rows: mark all pending/overdue rows as cancelled (add `cancelled` to status enum)
- Transactions already recorded: remain as-is (append-only); admin notes the cancellation

## Constraints
- One plot can have at most one active sale (status = 'active')
- sale_price must be > 0
- sale_date cannot be in the future
- buyer_id must have role = 'buyer'
- agent_id (if set) must reference a valid agents row

## Payment Types
- `lump_sum`: full amount expected as one or more transactions; no EMI rows generated
- `emi`: down payment + installments; EMI schedule generated at sale creation

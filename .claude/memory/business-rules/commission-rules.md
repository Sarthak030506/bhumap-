# Commission Rules

## Calculation
```
commission_amount = ROUND(sale_price × commission_percent / 100, 2)
```
- Computed by `calculate-commission` Edge Function at sale creation
- Never calculated on the client side
- Stored on the sales row as commission_amount
- commission_paid starts as false

## Commission Lifecycle
1. Sale created with agent_id → commission_amount computed and stored
2. Admin manually marks commission paid → commission_paid = true
3. agents.total_earned incremented by commission_amount (done by Edge Function)
4. IF sale cancelled BEFORE commission paid → commission voided (commission_amount = 0)
5. IF sale cancelled AFTER commission paid → admin resolves manually (no automated reversal)

## Direct Sales
- If agent_id is NULL on a sale → no commission row, commission_amount = NULL
- Admin can add agent_id after sale creation only if commission_paid = false

## Commission Disputes (open question)
- Two agents claim same buyer → admin decides which agent_id to set
- Buyer contacts admin directly after agent introduced them → admin decides
- No automatic split-commission in v1

## Agent Earnings View
- Agent sees: list of sales where agent_id = their agents.id
- Shows: sale_date, plot_number, buyer_name (first name only?), commission_amount, commission_paid status
- Agent never sees: sale_price, buyer contact info, other agents' data

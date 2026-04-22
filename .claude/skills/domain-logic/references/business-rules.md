# Business Rules — IF/THEN/ELSE Format

---

## Plot Status

**Rule: Plot creation**
```
IF admin creates a plot
THEN status = 'available'
```

**Rule: Plot sold**
```
IF admin creates a sale for a plot
THEN plots.status = 'sold'
AND plot can no longer appear in agent available-plots view
```

**Rule: Sale cancellation — plot status**
```
IF admin cancels a sale (sets sales.status = 'cancelled')
THEN plots.status = 'available'
AND emi_schedule rows for that sale are soft-deleted or marked cancelled
AND commission_amount on that sale is voided (not paid to agent if not yet paid)
```

**Rule: Plot reservation (optional — open question)**
```
IF admin wants to hold a plot without full sale
THEN status = 'reserved'  (not available for agents/buyers, not yet sold)
ELSE if not using reservation: skip directly to 'sold' on sale creation
```

---

## Sale Creation

**Rule: Standard sale**
```
IF admin creates a sale with plot_id, buyer_id, sale_price, payment_type
THEN:
  1. Set plots.status = 'sold'
  2. IF payment_type = 'emi' AND emi_months is set:
       THEN call generate-emi-schedule Edge Function
  3. IF agent_id is set:
       THEN call calculate-commission Edge Function
  4. IF agent_id is set AND lead exists for this buyer from this agent:
       THEN set leads.status = 'converted' AND leads.converted_sale_id = sale.id
```

**Rule: Lump sum sale**
```
IF payment_type = 'lump_sum'
THEN emi_months is NULL
AND no emi_schedule rows are generated
AND admin records a single transaction for full amount
```

---

## EMI Rules

**Rule: EMI schedule generation**
```
IF payment_type = 'emi'
THEN generate emi_months rows in emi_schedule
  WHERE amount[1..n-1] = FLOOR(sale_price / emi_months * 100) / 100
  AND amount[n] = sale_price - (amount[1] * (emi_months - 1))   [last absorbs rounding]
  AND due_date[i] = sale_date + i months (same calendar day)
  AND status = 'pending' for all rows
```

**Rule: Marking an EMI paid**
```
IF admin records a transaction against a sale
THEN admin selects which emi_schedule row(s) it covers
  AND emi_schedule.status = 'paid'
  AND emi_schedule.paid_date = transaction.payment_date
  AND emi_schedule.transaction_id = transaction.id
```

**Rule: EMI overdue**
```
IF emi_schedule.due_date < today AND status = 'pending'
THEN status = 'overdue'
  [triggered by daily cron OR detected on-demand in UI]
```

**Rule: EMI row immutability**
```
IF an emi_schedule row has been created
THEN amount and due_date are immutable
ELSE if admin needs to renegotiate: cancel old sale, create new sale with new terms
```

---

## Agent Commission

**Rule: Commission creation**
```
IF a sale is created WITH agent_id set
THEN commission_amount = ROUND(sale_price × (agent.commission_percent / 100), 2)
AND commission_paid = false
AND this is computed by Edge Function, not by client
```

**Rule: No agent**
```
IF a sale is created WITHOUT agent_id
THEN commission_amount = NULL
AND commission_paid = false (default)
```

**Rule: Commission payout**
```
IF admin marks commission_paid = true on a sale
THEN sales.commission_paid = true
AND agents.total_earned += sales.commission_amount
[executed via mark-commission-paid Edge Function]
```

**Rule: Commission after sale cancellation**
```
IF admin cancels a sale AND commission_paid = false
THEN commission_amount is voided (do not pay; set to NULL or leave with cancelled status)
IF admin cancels a sale AND commission_paid = true
THEN commission is already disbursed — admin must handle reversal manually
  [this is an open question — see open-questions.md]
```

---

## Partner Profit

**Rule: Partner record creation**
```
IF admin adds a partner to a project
THEN partners.percentage must be set
AND admin is responsible for ensuring all partner percentages add up to ≤ 100%
  [validation: warn if sum > 100%, allow admin to proceed — it's their record]
```

**Rule: Profit calculation**
```
IF admin requests P&L for a project
THEN call calculate-partner-profit Edge Function
THEN each partner's share = net_profit × (percentage / 100)
AND this is a READ operation — no database mutation
```

**Rule: Partner data visibility**
```
IF role = 'partner'
THEN can see own projects, own percentage, own profit share
CANNOT see: individual buyer names, sale prices, agent data, other partner percentages
```

---

## Lead Lifecycle

**Rule: Lead creation**
```
IF agent creates a lead
THEN status = 'interested'
AND agent_id = the creating agent's ID
AND lead cannot be created for another agent's agent_id (RLS enforced)
```

**Rule: Lead progression**
```
interested → site_visit_done: agent updates after taking buyer to site
site_visit_done → negotiating: agent updates when price discussions begin
negotiating → converted: admin (or agent) updates when sale is created with this agent's lead
negotiating → lost: agent or admin marks when buyer drops out
```

**Rule: Lead conversion**
```
IF a sale is created with agent_id = X AND buyer matches a lead by agent X
THEN leads.status = 'converted'
AND leads.converted_sale_id = sale.id
[this linkage is best-effort — admin may need to manually confirm; see open-questions.md]
```

**Rule: Lead ownership**
```
IF two agents both claim the same buyer (same phone number)
THEN admin must manually decide which agent_id to set on the sale
THEN the other agent's lead remains in 'negotiating' or is marked 'lost'
[See edge-cases.md for conflict handling]
```

---

## Payment Recording

**Rule: Transaction recording**
```
IF admin records a payment
THEN a new transaction row is inserted (append-only)
AND amount, payment_date, payment_method are required
AND recorded_by = admin's user.id
AND no transaction can be edited or deleted after creation
```

**Rule: Payment correction**
```
IF admin made an error in a transaction
THEN create a new transaction with a negative amount (reversal)
AND add notes explaining the correction
```

**Rule: Overpayment**
```
IF total transactions for a sale > sale_price
THEN record as-is — this is valid (e.g., penalties, interest)
AND admin is responsible for reconciliation
```

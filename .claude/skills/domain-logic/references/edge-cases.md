# Real-World Edge Cases — Land Developer Platform

Severity: 🔴 Critical | 🟡 High | 🟢 Medium

---

## Payments & Transactions

### 🔴 Buyer claims payment made but no transaction recorded
- Admin received cash but forgot to record in app
- Buyer now shows "overdue" and gets reminder notifications
- **Risk:** Buyer disputes; admin has no app audit trail
- **Gap:** No offline/verbal payment acknowledgment workflow
- **Question:** Should admin be able to back-date transactions? (Currently: yes — payment_date is a date field, not a system timestamp)

### 🔴 Partial payment recorded against an EMI row
- Buyer pays ₹15,000 but EMI row is ₹20,000
- If admin links the transaction to the EMI row, it will show as "paid" even though short
- **Risk:** Under-collection silently passes as complete payment
- **Gap:** No partial-payment flag on emi_schedule; transaction.amount ≠ emi.amount is not validated
- **Mitigation needed:** Either split EMI rows, or add `amount_received` field to emi_schedule

### 🟡 Transaction recorded for wrong sale
- Admin selects wrong sale_id when entering a transaction
- **Risk:** One sale looks overpaid, another looks underpaid
- **Gap:** Transactions are append-only — correction requires reversal transaction, which creates confusion
- **Mitigation:** Admin confirmation dialog showing buyer name and plot before saving

### 🟡 Multiple transactions linked to same EMI row
- Admin accidentally marks same EMI paid twice
- **Risk:** Sale total appears inflated; buyer looks overpaid
- **Gap:** No UNIQUE constraint on `emi_schedule.transaction_id`; one transaction can cover multiple EMIs (intended) but the reverse (multiple transactions → one EMI) needs guarding

---

## Sale & Commission

### 🔴 Agent's lead converts but admin forgets to set agent_id on sale
- Sale is created with no agent_id
- Commission is never generated
- Agent claims they brought the buyer
- **Risk:** Agent dispute; no app-level proof of referral
- **Gap:** No automatic lead-to-sale linking; relies on admin memory
- **Mitigation needed:** On sale creation, check if any open lead from any agent matches buyer_phone; warn admin

### 🔴 Two agents both claim the same buyer
- Agent A and Agent B both have a lead with buyer phone 9876543210
- Admin creates sale — which agent gets commission?
- **Risk:** One agent loses commission; relationship damage
- **Gap:** No duplicate lead detection across agents; admin has no alert
- **Mitigation needed:** On lead creation, check if buyer_phone already exists in leads table (any agent); flag to agent and admin

### 🟡 Commission percent changed after sale is created
- Agent's commission_percent is updated by admin from 2% to 3%
- Old sales still reference agent.commission_percent at query time (if not denormalized)
- **Risk:** Historical commissions appear wrong if recalculated
- **Gap:** `commission_amount` is written at sale creation (denormalized) so this is safe — but if it was ever computed on-the-fly it would break
- **Confirm:** `sales.commission_amount` must always be computed at creation and stored, never derived at read time

### 🟡 Sale cancelled after commission already paid
- Admin marks commission_paid = true, then later cancels the sale
- `agents.total_earned` was already incremented
- **Risk:** Agent keeps commission for a cancelled deal
- **Gap:** No reversal logic in `mark-commission-paid` for cancelled sales
- **Open question:** Should admin be blocked from cancelling a sale if commission_paid = true?

---

## EMI Schedule

### 🔴 EMI due date falls on a non-existent date
- Sale date is January 31. Next month: February 31 doesn't exist.
- `addMonths(Jan 31, 1)` behavior depends on date library — some snap to Feb 28, some throw
- **Risk:** EMI row has wrong due_date or generation fails
- **Mitigation:** Define date handling rule: always snap to end-of-month if day doesn't exist in target month

### 🟡 Rounding error across EMI schedule
- `sale_price = ₹10,00,001` and `emi_months = 3`
- Monthly = ₹3,33,333.67; Last = ₹3,33,333.66
- Sum = ₹10,00,000.99 ≠ ₹10,00,001
- **Risk:** Total EMI collected never exactly matches sale_price
- **Mitigation:** Last installment = sale_price - (monthly × (emi_months - 1)) — defined in business rules

### 🟡 Admin creates EMI sale without calling Edge Function
- If client creates sale row directly without triggering `generate-emi-schedule`
- Sale exists but no EMI rows — buyer has no schedule to pay against
- **Risk:** Data integrity failure; buyer shows ₹0 outstanding
- **Mitigation:** `generate-emi-schedule` must be called from admin app, or triggered by a PostgreSQL trigger on sales INSERT where payment_type = 'emi'

---

## Partners

### 🟡 Partner percentages don't add up to 100%
- Admin adds 3 partners at 30%, 40%, 15% = 85% total
- Remaining 15% is implicitly the admin's, but it's not recorded anywhere
- **Risk:** Profit distribution appears incomplete; disputes
- **Gap:** No validation or warning for under/over allocation
- **Mitigation:** Show running total during partner entry; warn if < 100% or > 100%

### 🟢 Partner exits mid-project
- A partner wants out after some plots are sold
- No concept of partner exit in current schema
- **Risk:** Profit calculations become wrong mid-project
- **Gap:** No `exit_date`, percentage_history, or effective-date on partner records
- **Open question:** Is partner exit a real scenario? If yes, needs design before coding partners module

---

## Plots

### 🟡 Plot boundary overlaps with another plot
- Admin draws polygon that partially covers an adjacent plot
- Both plots show "available" but their land physically overlaps
- **Risk:** Two buyers purchase overlapping land; legal dispute
- **Gap:** No server-side boundary intersection check
- **Mitigation:** Client-side overlap warning during draw; server-side geometry check in Edge Function before saving plot

### 🟢 Plot number duplicate across projects
- Admin creates "Plot-1" in Project A and "Plot-1" in Project B — valid
- But UNIQUE constraint is on (project_id, plot_number) — correct
- **Non-issue** if schema is correct — verify this constraint exists

---

## Access & Security

### 🔴 Agent sees a buyer's contact details via lead they didn't create
- If an agent knows another lead's `id` they could try to access it directly
- **RLS guard:** `leads.agent_id` must match auth'd agent's record — this blocks it
- **Verify:** Test that agent cannot fetch any lead by UUID not owned by them

### 🟡 Buyer sees another buyer's plot via shared sale_id
- If buyer guesses a sale UUID, RLS must block them
- **RLS guard:** `sales.buyer_id = auth.uid()` — this blocks it
- **Verify:** Test RLS with two buyer accounts

### 🟢 Admin accidentally deletes a project with active sales
- `ON DELETE RESTRICT` on `plots.project_id` prevents orphan plots
- But does CASCADE reach sales and emi_schedule?
- **Mitigation:** Keep RESTRICT everywhere; require admin to cancel all sales before deleting project

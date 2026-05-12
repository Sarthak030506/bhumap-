# Supabase Edge Functions

All Edge Functions run with the **service-role key** to bypass RLS for computed writes.
Client code never calls these with user credentials for write operations.

---

## 1. `generate-emi-schedule`

**Trigger:** Called by admin when creating a sale with `payment_type = 'emi'`

**Input:**
```typescript
{
  sale_id: string;       // uuid of the newly created sale
  sale_price: number;    // numeric(15,2)
  emi_months: number;    // integer > 0
  sale_date: string;     // ISO date, e.g. "2024-01-15" — first EMI due the following month
}
```

**Logic:**
```typescript
const monthly = Math.floor((sale_price / emi_months) * 100) / 100; // truncate to 2dp
const remainder = sale_price - (monthly * (emi_months - 1));       // last installment absorbs rounding

for (let i = 1; i <= emi_months; i++) {
  const dueDate = addMonths(sale_date, i);      // same day of month, +i months
  const amount = (i === emi_months) ? remainder : monthly;
  // INSERT INTO emi_schedule (sale_id, installment_number, due_date, amount, status)
}
```

**Output:** `emi_months` rows inserted into `emi_schedule`

**Errors to handle:**
- `sale_id` not found → 404
- Sale already has EMI rows → 409 (idempotency check)
- `emi_months <= 0` → 400

---

## 2. `calculate-commission`

**Trigger:** Called by admin when creating a sale with `agent_id` set

**Input:**
```typescript
{
  sale_id: string;    // uuid
  agent_id: string;  // uuid of agents record (not users)
}
```

**Logic:**
```typescript
const agent = await getAgent(agent_id);
const sale = await getSale(sale_id);
const commission = ROUND(sale.sale_price * agent.commission_percent / 100, 2);

await updateSale(sale_id, {
  commission_amount: commission,
  commission_paid: false
});
```

**Output:** `sales.commission_amount` set, `sales.commission_paid = false`

**Errors to handle:**
- Agent not found → 404
- Sale not found → 404
- Sale already has commission_amount set → 409 (prevent re-calculation)

---

## 3. `mark-commission-paid`

**Trigger:** Called by admin to pay out agent commission

**Input:**
```typescript
{
  sale_id: string;  // uuid
}
```

**Logic:**
```typescript
const sale = await getSale(sale_id);
if (sale.commission_paid) throw new Error('Already paid');
if (!sale.agent_id || !sale.commission_amount) throw new Error('No commission on this sale');

// Mark commission paid on sale
await updateSale(sale_id, { commission_paid: true });

// Add to agent's running total
await incrementAgentEarnings(sale.agent_id, sale.commission_amount);
// UPDATE agents SET total_earned = total_earned + commission_amount WHERE id = agent_id
```

**Output:** `sales.commission_paid = true`, `agents.total_earned` incremented

---

## 4. `calculate-partner-profit`

**Trigger:** Called by admin for per-project P&L report

**Input:**
```typescript
{
  project_id: string;  // uuid
}
```

**Logic:**
```typescript
// Total revenue = sum of all transactions for sales in this project
const totalRevenue = await sumTransactions(project_id);

// Total costs = sum of capital_contributed by all partners in project
// (This is a simplification — admin may add explicit cost fields later)
const costs = await sumPartnerCapital(project_id);

const netProfit = totalRevenue - costs;

// Per partner share
const partners = await getPartners(project_id);
return partners.map(p => ({
  partner_id: p.id,
  user_id: p.user_id,
  percentage: p.percentage,
  profit_share: ROUND(netProfit * p.percentage / 100, 2)
}));
```

**Output:** Array of `{ partner_id, user_id, percentage, profit_share }`

**Open question:** Does admin track land acquisition cost and development costs separately? 
Until decided, total_revenue - partner_capital is the placeholder formula.

---

## 5. `send-emi-notifications` (Cron)

**Schedule:** Daily at 08:00 IST (`0 2 * * *` in UTC)

**Logic:**
```typescript
const today = new Date();
const sevenDaysLater = addDays(today, 7);

// 1. EMIs due in exactly 7 days
const upcomingEMIs = await query(`
  SELECT es.*, s.buyer_id, u.full_name, u.id as user_id
  FROM emi_schedule es
  JOIN sales s ON s.id = es.sale_id
  JOIN users u ON u.id = s.buyer_id
  WHERE es.status = 'pending'
    AND es.due_date = $1
`, [sevenDaysLater]);

// 2. EMIs due today
const todayEMIs = await query(`... WHERE es.due_date = $1`, [today]);

// 3. Overdue EMIs (due_date < today AND status = 'pending')
const overdueEMIs = await query(`
  SELECT ... WHERE es.status = 'pending' AND es.due_date < $1
`, [today]);

// Mark overdue EMIs
await query(`
  UPDATE emi_schedule SET status = 'overdue'
  WHERE status = 'pending' AND due_date < $1
`, [today]);

// Send push notifications via Expo Push API
for (const emi of [...upcomingEMIs, ...todayEMIs, ...overdueEMIs]) {
  await sendExpoPushNotification(emi.user_id, buildMessage(emi));
}
```

**Notification messages:**
- 7-day: `"EMI of ₹{amount} for Plot {plot_number} is due on {date}"`
- Today: `"Your EMI of ₹{amount} for Plot {plot_number} is due today"`
- Overdue: `"OVERDUE: EMI of ₹{amount} for Plot {plot_number} was due on {date}"`

**See:** `emi-and-payments/references/notification-logic.md` for full cron rules

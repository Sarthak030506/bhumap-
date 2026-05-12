# Domain Model — Entities, Relationships, State Machines

---

## Entities

### User
```
id, email, full_name, role (admin|buyer|partner|agent), phone, created_at
```
- One user has one role (no multi-role)
- Agent users also have a corresponding `agents` record

### Project
```
id, name, location, total_area_sqft, description, status, created_by, created_at
```
- A project is a physical land parcel that gets divided into plots
- Has many: Plots, Partners
- Status: `active → completed | cancelled`

### Plot
```
id, project_id, plot_number, area_sqft, boundary (jsonb), status, base_price, created_at
```
- Belongs to: Project
- Has one: Sale (at most — a plot can only be sold once at a time)
- **State machine:** `available → sold` (or `available → reserved → sold`)
- `base_price` is a guide price; actual `sale_price` is on the Sale record

### Partner
```
id, user_id, project_id, percentage, capital_contributed, joined_at
```
- Links a partner User to a Project
- One user can be a partner in multiple projects (different rows)
- A project can have multiple partners
- `percentage` is the profit share — admin must ensure total ≤ 100%

### Agent
```
id, user_id, commission_percent, total_earned, created_at
```
- One-to-one extension of a User with role=agent
- `commission_percent` defaults to 2.00%; set per agent by admin
- `total_earned` is a running total updated when commission_paid = true

### Sale
```
id, plot_id, buyer_id, agent_id (nullable), sale_price, payment_type,
sale_date, emi_months (nullable), commission_amount (nullable),
commission_paid, status, created_at
```
- Represents the agreement to sell a specific plot to a specific buyer
- Links: Plot ← Sale → Buyer (User), Sale → Agent (optional)
- Has many: Transactions, EMI Schedule rows
- `commission_amount` set at creation by Edge Function if agent_id present

### Transaction
```
id, sale_id, amount, payment_date, payment_method, notes, recorded_by, created_at
```
- A single cash receipt against a sale
- Append-only: no updates or deletes
- May cover multiple EMI installments (admin links when recording)
- Belongs to: Sale

### EMI Schedule
```
id, sale_id, installment_number, due_date, amount, status, paid_date,
transaction_id (nullable), created_at
```
- Auto-generated rows: one per month for `emi_months` months
- Belongs to: Sale
- Points to: Transaction (when paid)
- **State machine:** `pending → paid` (terminal); `pending → overdue → paid` (if overdue then paid)

### Lead
```
id, agent_id, buyer_name, buyer_phone, buyer_email, project_id (nullable),
plot_id (nullable), status, notes, converted_sale_id (nullable), created_at, updated_at
```
- An agent's tracked buyer prospect
- Belongs to: Agent
- May reference: Project (interest), Plot (specific interest), Sale (when converted)
- **State machine:** see below

---

## State Machines

### Plot Status
```
         [Admin creates plot]
               │
               ▼
          ┌─────────┐
          │ available │ ◄──────────────────────────┐
          └─────────┘                              │
               │ Admin creates sale                │ Admin cancels sale
               ▼                                   │
           ┌──────┐                                │
           │ sold  │ ─────────────────────────────►┘
           └──────┘
           
Optional reservation step:
available → reserved → sold
available → reserved → available (if reservation falls through)
```

### EMI Installment Status
```
[Generated at sale creation]
          │
          ▼
      ┌─────────┐
      │ pending │
      └─────────┘
          │                    │
          │ Admin records       │ Cron: due_date < today
          │ payment             │ AND still pending
          ▼                     ▼
       ┌──────┐           ┌─────────┐
       │ paid │ ◄─────────│ overdue │
       └──────┘  Admin    └─────────┘
                records
                payment
```

### Lead Lifecycle
```
[Agent creates lead]
          │
          ▼
    ┌───────────┐
    │ interested│
    └───────────┘
          │ Agent updates
          ▼
  ┌────────────────┐
  │ site_visit_done│
  └────────────────┘
          │ Agent updates
          ▼
   ┌─────────────┐
   │ negotiating │ ──────────────────► lost
   └─────────────┘     (any time)
          │ Admin creates sale
          │ with this agent_id
          ▼
     ┌──────────┐
     │ converted│
     └──────────┘
     
Any status can go to 'lost' at admin or agent discretion.
'converted' and 'lost' are terminal states.
```

### Sale Status
```
[Admin creates sale]
          │
          ▼
      ┌────────┐
      │ active │
      └────────┘
          │ Admin cancels
          ▼
    ┌───────────┐
    │ cancelled │
    └───────────┘
    
Cancellation side effects:
- Plot status → available
- EMI rows → (decide: delete or leave with cancelled marker)
- Commission → voided if not yet paid
```

---

## Relationship Overview

```
User (admin)  ──creates──►  Project
                                │
                         ┌──────┴──────┐
                         │             │
                       Plot          Partner
                         │             │
                         │           User (partner)
                   ┌─────┘
                   │
                 Sale ─────────────► User (buyer)
                   │
                   │ ──────────────► Agent ◄── User (agent)
                   │                   │
                   │                 Lead
                   │
         ┌─────────┴──────────┐
         │                    │
    Transaction        EMI Schedule
```

---

## Glossary

| Term | Meaning |
|------|---------|
| Plot | A numbered parcel of land within a project |
| Sale | The recorded agreement: admin sells plot to buyer |
| EMI | Equated Monthly Installment — a scheduled payment row |
| Transaction | An actual cash receipt recorded by admin |
| Lead | An agent's tracked prospective buyer |
| Commission | Agent's fee: % of sale_price, computed at sale creation |
| Partner | Co-investor in a project; receives % of net profit |
| 7/12 Document | Government land record document; stored in Supabase Storage |

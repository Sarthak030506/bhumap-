# PostgreSQL Schema — Quick Reference

## Enums
```
user_role:      admin | buyer | partner | agent
plot_status:    available | reserved | sold
payment_type:   lump_sum | emi
payment_method: cash | bank_transfer | cheque | upi
emi_status:     pending | paid | overdue
lead_status:    interested | site_visit_done | negotiating | converted | lost
project_status: active | completed | cancelled
sale_status:    active | cancelled
```

## Tables (columns only — types in supabase-architecture/references/schema.md)
```
users:         id, email, full_name, role, phone, created_at
projects:      id, name, location, total_area_sqft, description, status, created_by, created_at
plots:         id, project_id, plot_number, area_sqft, boundary(jsonb), status, base_price, created_at
partners:      id, user_id, project_id, percentage, capital_contributed, joined_at
agents:        id, user_id, commission_percent, total_earned, created_at
sales:         id, plot_id, buyer_id, agent_id, sale_price, payment_type, sale_date,
               emi_months, commission_amount, commission_paid, status, created_at
transactions:  id, sale_id, amount, payment_date, payment_method, notes, recorded_by, created_at
emi_schedule:  id, sale_id, installment_number, due_date, amount, status, paid_date,
               transaction_id, created_at
leads:         id, agent_id, buyer_name, buyer_phone, buyer_email, project_id, plot_id,
               status, notes, converted_sale_id, created_at, updated_at
```

## Key Relationships
- plots.project_id → projects.id (RESTRICT)
- sales.plot_id → plots.id
- sales.buyer_id → users.id
- sales.agent_id → agents.id (nullable)
- transactions.sale_id → sales.id
- emi_schedule.sale_id → sales.id (CASCADE)
- emi_schedule.transaction_id → transactions.id (nullable)
- leads.agent_id → agents.id
- leads.converted_sale_id → sales.id (nullable)
- partners.user_id → users.id
- partners.project_id → projects.id

## Key Indexes
- plots: (project_id), (status)
- sales: (buyer_id), (agent_id), (plot_id)
- emi_schedule: (sale_id), (status, due_date)
- leads: (agent_id), (status)
- partners: (project_id), (user_id)

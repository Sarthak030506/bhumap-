# Database Schema — All 9 Tables

## Enums

```sql
CREATE TYPE user_role AS ENUM ('admin', 'buyer', 'partner', 'agent');
CREATE TYPE plot_status AS ENUM ('available', 'reserved', 'sold');
CREATE TYPE payment_type AS ENUM ('lump_sum', 'emi');
CREATE TYPE payment_method AS ENUM ('cash', 'bank_transfer', 'cheque', 'upi');
CREATE TYPE emi_status AS ENUM ('pending', 'paid', 'overdue');
CREATE TYPE lead_status AS ENUM ('interested', 'site_visit_done', 'negotiating', 'converted', 'lost');
CREATE TYPE project_status AS ENUM ('active', 'completed', 'cancelled');
CREATE TYPE sale_status AS ENUM ('active', 'cancelled');
```

---

## 1. users

```sql
CREATE TABLE users (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email         text UNIQUE NOT NULL,
  full_name     text NOT NULL,
  role          user_role NOT NULL,
  phone         text,
  created_at    timestamptz NOT NULL DEFAULT now()
);
```

Notes:
- Mirrors `auth.users` — insert here after Supabase Auth signup via trigger or Edge Function
- `id` matches `auth.users.id`

---

## 2. projects

```sql
CREATE TABLE projects (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name            text NOT NULL,
  location        text,
  total_area_sqft numeric(15,2),
  description     text,
  status          project_status NOT NULL DEFAULT 'active',
  created_by      uuid NOT NULL REFERENCES users(id),
  created_at      timestamptz NOT NULL DEFAULT now()
);
```

---

## 3. plots

```sql
CREATE TABLE plots (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  project_id    uuid NOT NULL REFERENCES projects(id) ON DELETE RESTRICT,
  plot_number   text NOT NULL,               -- e.g. "A-101", "Plot-23"
  area_sqft     numeric(10,2) NOT NULL,
  boundary      jsonb,                        -- [{lat, lng}, ...] min 3 points
  status        plot_status NOT NULL DEFAULT 'available',
  base_price    numeric(15,2),
  created_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE(project_id, plot_number)
);

CREATE INDEX idx_plots_project_id ON plots(project_id);
CREATE INDEX idx_plots_status ON plots(status);
```

---

## 4. partners

```sql
CREATE TABLE partners (
  id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id              uuid NOT NULL REFERENCES users(id),
  project_id           uuid NOT NULL REFERENCES projects(id),
  percentage           numeric(5,2) NOT NULL,    -- e.g. 30.00 = 30%
  capital_contributed  numeric(15,2) NOT NULL DEFAULT 0,
  joined_at            timestamptz NOT NULL DEFAULT now(),
  UNIQUE(user_id, project_id)
);

CREATE INDEX idx_partners_project_id ON partners(project_id);
CREATE INDEX idx_partners_user_id ON partners(user_id);
```

---

## 5. agents

```sql
CREATE TABLE agents (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id             uuid NOT NULL UNIQUE REFERENCES users(id),
  commission_percent  numeric(5,2) NOT NULL DEFAULT 2.00,
  total_earned        numeric(15,2) NOT NULL DEFAULT 0,
  created_at          timestamptz NOT NULL DEFAULT now()
);
```

Notes:
- `commission_percent` is per-agent; can differ per agent
- `total_earned` updated by Edge Function when admin marks commission_paid = true

---

## 6. sales

```sql
CREATE TABLE sales (
  id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  plot_id           uuid NOT NULL REFERENCES plots(id),
  buyer_id          uuid NOT NULL REFERENCES users(id),     -- role = buyer
  agent_id          uuid REFERENCES agents(id),             -- nullable: direct sale has no agent
  sale_price        numeric(15,2) NOT NULL,
  payment_type      payment_type NOT NULL,
  sale_date         date NOT NULL,
  emi_months        integer,                                 -- required if payment_type = 'emi'
  commission_amount numeric(15,2),                          -- computed by Edge Function if agent_id set
  commission_paid   boolean NOT NULL DEFAULT false,
  status            sale_status NOT NULL DEFAULT 'active',
  created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_sales_buyer_id ON sales(buyer_id);
CREATE INDEX idx_sales_agent_id ON sales(agent_id);
CREATE INDEX idx_sales_plot_id ON sales(plot_id);
```

Constraints (enforce in Edge Function or CHECK):
- IF `payment_type = 'emi'` THEN `emi_months IS NOT NULL AND emi_months > 0`
- `commission_amount` written only by Edge Function, never by client

---

## 7. transactions

```sql
CREATE TABLE transactions (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  sale_id         uuid NOT NULL REFERENCES sales(id),
  amount          numeric(15,2) NOT NULL,
  payment_date    date NOT NULL,
  payment_method  payment_method NOT NULL DEFAULT 'bank_transfer',
  notes           text,
  recorded_by     uuid NOT NULL REFERENCES users(id),    -- always admin
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_sale_id ON transactions(sale_id);
```

Notes:
- Append-only: no UPDATE or DELETE allowed (RLS enforced)
- Corrections done by recording a new transaction with explanatory notes

---

## 8. emi_schedule

```sql
CREATE TABLE emi_schedule (
  id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  sale_id              uuid NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
  installment_number   integer NOT NULL,
  due_date             date NOT NULL,
  amount               numeric(15,2) NOT NULL,
  status               emi_status NOT NULL DEFAULT 'pending',
  paid_date            date,
  transaction_id       uuid REFERENCES transactions(id),    -- set when marked paid
  created_at           timestamptz NOT NULL DEFAULT now(),
  UNIQUE(sale_id, installment_number)
);

CREATE INDEX idx_emi_sale_id ON emi_schedule(sale_id);
CREATE INDEX idx_emi_status_due ON emi_schedule(status, due_date);
```

---

## 9. leads

```sql
CREATE TABLE leads (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  agent_id            uuid NOT NULL REFERENCES agents(id),
  buyer_name          text NOT NULL,
  buyer_phone         text NOT NULL,
  buyer_email         text,
  project_id          uuid REFERENCES projects(id),         -- nullable: general interest
  plot_id             uuid REFERENCES plots(id),            -- nullable: specific plot interest
  status              lead_status NOT NULL DEFAULT 'interested',
  notes               text,
  converted_sale_id   uuid REFERENCES sales(id),            -- set when status = 'converted'
  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_leads_agent_id ON leads(agent_id);
CREATE INDEX idx_leads_status ON leads(status);
```

---

## Foreign Key Relationship Map

```
users ──< partners >── projects ──< plots ──< sales >── transactions
                                                │
users (buyer) ─────────────────────────────────┘
                                                │
agents <── users (agent) ──────────── sales.agent_id
agents ──< leads
sales ──< emi_schedule
```

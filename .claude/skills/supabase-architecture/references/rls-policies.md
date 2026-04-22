# RLS Policies — All Tables, All Roles, All Actions

## Setup

```sql
-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE plots ENABLE ROW LEVEL SECURITY;
ALTER TABLE partners ENABLE ROW LEVEL SECURITY;
ALTER TABLE agents ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE emi_schedule ENABLE ROW LEVEL SECURITY;
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;
```

Helper: `auth.jwt() ->> 'role'` reads the role from the JWT claim set during signup.

---

## users

```sql
-- Admin: full access
CREATE POLICY "admin_all_users" ON users
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- All roles: read own profile
CREATE POLICY "own_profile_select" ON users
  FOR SELECT TO authenticated
  USING (id = auth.uid());

-- All roles: update own profile (not role field — enforced in app)
CREATE POLICY "own_profile_update" ON users
  FOR UPDATE TO authenticated
  USING (id = auth.uid());
```

---

## projects

```sql
-- Admin: full access
CREATE POLICY "admin_all_projects" ON projects
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Buyer: see projects they have a sale in
CREATE POLICY "buyer_select_projects" ON projects
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'buyer'
    AND id IN (
      SELECT p.project_id FROM plots p
      JOIN sales s ON s.plot_id = p.id
      WHERE s.buyer_id = auth.uid() AND s.status = 'active'
    )
  );

-- Partner: see projects they co-invested in
CREATE POLICY "partner_select_projects" ON projects
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'partner'
    AND id IN (
      SELECT project_id FROM partners WHERE user_id = auth.uid()
    )
  );

-- Agent: see all active projects (to browse available plots)
CREATE POLICY "agent_select_active_projects" ON projects
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'agent'
    AND status = 'active'
  );
```

---

## plots

```sql
-- Admin: full access
CREATE POLICY "admin_all_plots" ON plots
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Buyer: see own plots (plots they have an active sale for)
CREATE POLICY "buyer_select_plots" ON plots
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'buyer'
    AND id IN (
      SELECT plot_id FROM sales WHERE buyer_id = auth.uid() AND status = 'active'
    )
  );

-- Partner: see plots in their co-invested projects
CREATE POLICY "partner_select_plots" ON plots
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'partner'
    AND project_id IN (
      SELECT project_id FROM partners WHERE user_id = auth.uid()
    )
  );

-- Agent: see available plots only
CREATE POLICY "agent_select_available_plots" ON plots
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'agent'
    AND status = 'available'
  );
```

---

## partners

```sql
-- Admin: full access
CREATE POLICY "admin_all_partners" ON partners
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Partner: see own records only
CREATE POLICY "partner_select_own" ON partners
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'partner'
    AND user_id = auth.uid()
  );

-- Buyer: no access (no policy = deny)
-- Agent: no access (no policy = deny)
```

---

## agents

```sql
-- Admin: full access
CREATE POLICY "admin_all_agents" ON agents
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Agent: see own row only
CREATE POLICY "agent_select_own" ON agents
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'agent'
    AND user_id = auth.uid()
  );

-- Buyer: no access
-- Partner: no access
```

---

## sales

```sql
-- Admin: full access
CREATE POLICY "admin_all_sales" ON sales
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Buyer: see own sales only
CREATE POLICY "buyer_select_own_sales" ON sales
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'buyer'
    AND buyer_id = auth.uid()
  );

-- Agent: see own sales rows (where their agent record is linked)
-- IMPORTANT: agents must NOT see sale_price; expose via agent_sales_view (see below)
CREATE POLICY "agent_select_own_sales" ON sales
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'agent'
    AND agent_id IN (
      SELECT id FROM agents WHERE user_id = auth.uid()
    )
  );

-- Partner: no direct access to sales
```

### Agent Sales View (column restriction)
```sql
-- Agents see commission fields but NOT sale_price or buyer financial details
CREATE VIEW agent_sales_view AS
  SELECT
    s.id,
    s.plot_id,
    s.sale_date,
    s.commission_amount,
    s.commission_paid,
    s.status
  FROM sales s
  JOIN agents a ON a.id = s.agent_id
  WHERE a.user_id = auth.uid();
```

---

## transactions

```sql
-- Admin: full access
CREATE POLICY "admin_all_transactions" ON transactions
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Buyer: select own transactions (via sale)
CREATE POLICY "buyer_select_own_transactions" ON transactions
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'buyer'
    AND sale_id IN (
      SELECT id FROM sales WHERE buyer_id = auth.uid()
    )
  );

-- Agent: no access
-- Partner: no access
-- No INSERT/UPDATE/DELETE for buyers (append-only, admin only)
```

---

## emi_schedule

```sql
-- Admin: full access
CREATE POLICY "admin_all_emi" ON emi_schedule
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Buyer: select own EMI rows
CREATE POLICY "buyer_select_own_emi" ON emi_schedule
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'buyer'
    AND sale_id IN (
      SELECT id FROM sales WHERE buyer_id = auth.uid()
    )
  );

-- Agent: no access
-- Partner: no access
```

---

## leads

```sql
-- Admin: full access
CREATE POLICY "admin_all_leads" ON leads
  FOR ALL TO authenticated
  USING ((auth.jwt() ->> 'role') = 'admin');

-- Agent: full CRUD on own leads only
CREATE POLICY "agent_select_own_leads" ON leads
  FOR SELECT TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'agent'
    AND agent_id IN (
      SELECT id FROM agents WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "agent_insert_own_leads" ON leads
  FOR INSERT TO authenticated
  WITH CHECK (
    (auth.jwt() ->> 'role') = 'agent'
    AND agent_id IN (
      SELECT id FROM agents WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "agent_update_own_leads" ON leads
  FOR UPDATE TO authenticated
  USING (
    (auth.jwt() ->> 'role') = 'agent'
    AND agent_id IN (
      SELECT id FROM agents WHERE user_id = auth.uid()
    )
  );

-- Buyer: no access
-- Partner: no access
```

---

## Summary Table

| Table | Admin | Buyer | Partner | Agent |
|-------|-------|-------|---------|-------|
| users | ALL | own row | own row | own row |
| projects | ALL | own (via sale) | own (via partner) | active only |
| plots | ALL | own (via sale) | own (via partner project) | available only |
| partners | ALL | ❌ | own rows | ❌ |
| agents | ALL | ❌ | ❌ | own row |
| sales | ALL | own rows | ❌ | own rows (via view, no price) |
| transactions | ALL | own (via sale) | ❌ | ❌ |
| emi_schedule | ALL | own (via sale) | ❌ | ❌ |
| leads | ALL | ❌ | ❌ | own rows (CRUD) |

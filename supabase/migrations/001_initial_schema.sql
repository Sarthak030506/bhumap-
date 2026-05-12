-- ============================================================
-- BhuMap v1 — Initial Schema
-- Run in: Supabase SQL Editor (or supabase db push)
-- ============================================================


-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- ============================================================
-- ENUMS
-- ============================================================

CREATE TYPE area_unit AS ENUM ('sqft', 'guntha', 'acre');

CREATE TYPE payment_method AS ENUM ('cash', 'upi', 'cheque', 'bank_transfer');

CREATE TYPE payment_type AS ENUM ('lump_sum', 'emi');

-- available → reserved → sold → blocked
CREATE TYPE plot_status AS ENUM ('available', 'reserved', 'sold', 'blocked');

CREATE TYPE plot_facing AS ENUM ('north', 'south', 'east', 'west');

-- upcoming: future instalment not yet due
-- due_today: due_date = today (set by daily cron)
-- pending: due_date passed, not yet paid, not yet flagged overdue
-- overdue: explicitly marked overdue by cron
-- paid: payment recorded
CREATE TYPE emi_status AS ENUM ('upcoming', 'due_today', 'pending', 'paid', 'overdue');

CREATE TYPE sale_status AS ENUM ('active', 'cancelled');

-- partial: total_paid < total_deal_value
-- fully_paid: total_paid >= total_deal_value
-- overdue: at least one EMI row is overdue (set by cron)
CREATE TYPE sale_payment_status AS ENUM ('partial', 'fully_paid', 'overdue');


-- ============================================================
-- HELPER FUNCTIONS
-- ============================================================

-- Stamps updated_at on every UPDATE
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

-- ============================================================
-- TABLE 1 — users
-- One row per authenticated user. In v1 this is always the admin.
-- id mirrors auth.users.id.
-- ============================================================
CREATE TABLE public.users (
  id          uuid        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  phone       text,
  full_name   text,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON public.users
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- Auto-create public.users row when a new auth.users row is inserted (OTP signup).
CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
  INSERT INTO public.users (id, phone, full_name)
  VALUES (
    NEW.id,
    NEW.phone,
    COALESCE(NEW.raw_user_meta_data->>'full_name', '')
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_auth_user();

-- RLS helper: true if calling session has a row in public.users (= is admin in v1).
-- Defined here — after users table exists — because LANGUAGE sql validates refs at creation.
-- Phase 2: add AND role = 'admin' check when buyer/agent login is introduced.
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.users WHERE id = auth.uid()
  );
$$;


-- ============================================================
-- TABLE 2 — lands
-- Raw land acquired from a farmer/owner. One row per acquisition.
-- Farmer contact info stored inline (no separate farmers table in v1).
-- ============================================================
CREATE TABLE public.lands (
  id                    uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id              uuid          NOT NULL REFERENCES public.users(id),

  -- Identification
  name                  text          NOT NULL,
  gat_number            text,                           -- Maharashtra survey number
  village               text,
  taluka                text,
  district              text          NOT NULL DEFAULT 'Maharashtra',
  location_description  text,

  -- Area
  total_area_sqft       numeric(12,2) NOT NULL CHECK (total_area_sqft > 0),
  area_unit             area_unit     NOT NULL DEFAULT 'sqft',  -- user display preference

  -- Farmer / owner details (inline; no separate table in v1)
  owner_name            text,
  owner_phone           text,

  -- Financials
  agreed_price          numeric(12,2) NOT NULL CHECK (agreed_price >= 0),
  total_paid            numeric(12,2) NOT NULL DEFAULT 0,
  -- GENERATED: remaining_to_owner = agreed_price - total_paid
  remaining_to_owner    numeric(12,2) GENERATED ALWAYS AS (agreed_price - total_paid) STORED,

  -- Map data
  boundary_coordinates  jsonb,                          -- [{lat, lng}, ...] polygon ≥ 3 points

  -- Documents: [{type: '7/12'|'sale_deed'|'other', url: '...', name: '...'}]
  document_urls         jsonb         NOT NULL DEFAULT '[]'::jsonb,

  notes                 text,
  created_at            timestamptz   NOT NULL DEFAULT now(),
  updated_at            timestamptz   NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_lands_updated_at
  BEFORE UPDATE ON public.lands
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


-- ============================================================
-- TABLE 3 — land_payments
-- Individual payments made to the farmer/owner for a land.
-- Append-only (no UPDATE/DELETE via RLS).
-- ============================================================
CREATE TABLE public.land_payments (
  id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
  land_id        uuid          NOT NULL REFERENCES public.lands(id) ON DELETE RESTRICT,
  amount         numeric(12,2) NOT NULL CHECK (amount > 0),
  payment_date   date          NOT NULL,
  payment_method payment_method NOT NULL,
  notes          text,
  created_at     timestamptz   NOT NULL DEFAULT now()
);

-- Maintain lands.total_paid whenever a land_payment is inserted, updated, or deleted.
CREATE OR REPLACE FUNCTION public.sync_land_total_paid()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
  v_land_id uuid;
BEGIN
  v_land_id := COALESCE(NEW.land_id, OLD.land_id);
  UPDATE public.lands
  SET
    total_paid = (
      SELECT COALESCE(SUM(amount), 0)
      FROM public.land_payments
      WHERE land_id = v_land_id
    ),
    updated_at = now()
  WHERE id = v_land_id;
  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_land_payments_sync
  AFTER INSERT OR UPDATE OF amount OR DELETE ON public.land_payments
  FOR EACH ROW EXECUTE FUNCTION public.sync_land_total_paid();


-- ============================================================
-- TABLE 4 — partners
-- Co-investors for a land. No partner login in v1 — stored as contacts only.
-- DB enforces: sum(ownership_percent) per land CANNOT EXCEED 100 (trigger below).
-- "Exactly 100" is a business rule; app/Edge Function validates before key ops
-- (e.g. before creating a sale) using land_partners_sum_ok() helper below.
-- ============================================================
CREATE TABLE public.partners (
  id                uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
  land_id           uuid          NOT NULL REFERENCES public.lands(id) ON DELETE RESTRICT,

  name              text          NOT NULL,
  phone             text,
  email             text,

  -- Investment
  ownership_percent numeric(5,2)  NOT NULL
                    CHECK (ownership_percent > 0 AND ownership_percent <= 100),
  committed_amount  numeric(12,2) NOT NULL DEFAULT 0
                    CHECK (committed_amount >= 0),
  total_paid        numeric(12,2) NOT NULL DEFAULT 0,
  -- GENERATED: remaining = committed_amount - total_paid
  remaining         numeric(12,2) GENERATED ALWAYS AS (committed_amount - total_paid) STORED,

  notes             text,
  created_at        timestamptz   NOT NULL DEFAULT now(),
  updated_at        timestamptz   NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_partners_updated_at
  BEFORE UPDATE ON public.partners
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- Hard DB guard: block any INSERT or UPDATE that would push the per-land
-- ownership_percent total above 100. Raises EXCEPTION — client gets an error.
-- Does NOT require the sum to equal exactly 100 (partners are added incrementally).
CREATE OR REPLACE FUNCTION public.check_partner_ownership_cap()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
  v_existing_sum numeric(5,2);
  v_new_sum      numeric(5,2);
BEGIN
  -- Sum all OTHER rows for this land (exclude current row on UPDATE)
  SELECT COALESCE(SUM(ownership_percent), 0)
  INTO v_existing_sum
  FROM public.partners
  WHERE land_id = NEW.land_id
    AND id <> NEW.id;         -- id <> NEW.id is safe on INSERT (NEW.id is new uuid, no match)

  v_new_sum := v_existing_sum + NEW.ownership_percent;

  IF v_new_sum > 100 THEN
    RAISE EXCEPTION
      'ownership_percent total would be %.2f%% for land %, maximum is 100%%',
      v_new_sum, NEW.land_id
      USING ERRCODE = 'check_violation';
  END IF;

  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_partners_ownership_cap
  BEFORE INSERT OR UPDATE OF ownership_percent ON public.partners
  FOR EACH ROW EXECUTE FUNCTION public.check_partner_ownership_cap();

-- Soft check helper: returns true only when sum = exactly 100 for the given land.
-- Call this before creating a sale or calculating profit splits.
-- Usage: SELECT public.land_partners_sum_ok('<land_id>');
CREATE OR REPLACE FUNCTION public.land_partners_sum_ok(p_land_id uuid)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER AS $$
  SELECT COALESCE(SUM(ownership_percent), 0) = 100
  FROM public.partners
  WHERE land_id = p_land_id;
$$;


-- ============================================================
-- TABLE 5 — partner_payments
-- Payments received from a partner toward their committed_amount.
-- ============================================================
CREATE TABLE public.partner_payments (
  id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
  partner_id     uuid          NOT NULL REFERENCES public.partners(id) ON DELETE RESTRICT,
  amount         numeric(12,2) NOT NULL CHECK (amount > 0),
  payment_date   date          NOT NULL,
  payment_method payment_method NOT NULL,
  notes          text,
  created_at     timestamptz   NOT NULL DEFAULT now()
);

-- Maintain partners.total_paid when a partner_payment is inserted, updated, or deleted.
CREATE OR REPLACE FUNCTION public.sync_partner_total_paid()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
  v_partner_id uuid;
BEGIN
  v_partner_id := COALESCE(NEW.partner_id, OLD.partner_id);
  UPDATE public.partners
  SET
    total_paid = (
      SELECT COALESCE(SUM(amount), 0)
      FROM public.partner_payments
      WHERE partner_id = v_partner_id
    ),
    updated_at = now()
  WHERE id = v_partner_id;
  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_partner_payments_sync
  AFTER INSERT OR UPDATE OF amount OR DELETE ON public.partner_payments
  FOR EACH ROW EXECUTE FUNCTION public.sync_partner_total_paid();


-- ============================================================
-- TABLE 6 — plots
-- Individual plots carved from a land. Each has a polygon boundary.
-- status transitions: available → reserved → sold → (back to available if sale cancelled)
-- ============================================================
CREATE TABLE public.plots (
  id                    uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
  land_id               uuid          NOT NULL REFERENCES public.lands(id) ON DELETE RESTRICT,

  plot_number           text          NOT NULL,          -- e.g. "A-12", "Plot-23"
  area_sqft             numeric(10,2) NOT NULL CHECK (area_sqft > 0),
  area_unit             area_unit     NOT NULL DEFAULT 'sqft',

  -- Pricing (optional; admin may override per-sale)
  base_price_per_sqft   numeric(10,2) CHECK (base_price_per_sqft >= 0),
  base_price            numeric(12,2) CHECK (base_price >= 0),

  -- Physical attributes
  facing                plot_facing,
  is_corner             boolean       NOT NULL DEFAULT false,
  is_road_facing        boolean       NOT NULL DEFAULT false,

  -- Map polygon inside the parent land boundary
  boundary_coordinates  jsonb,                           -- [{lat, lng}, ...] ≥ 3 points

  status                plot_status   NOT NULL DEFAULT 'available',

  notes                 text,
  created_at            timestamptz   NOT NULL DEFAULT now(),
  updated_at            timestamptz   NOT NULL DEFAULT now(),

  UNIQUE (land_id, plot_number)
);

CREATE TRIGGER trg_plots_updated_at
  BEFORE UPDATE ON public.plots
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


-- ============================================================
-- TABLE 7 — customers
-- Buyers who purchase plots. No customer login in v1.
-- ============================================================
CREATE TABLE public.customers (
  id            uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id      uuid        NOT NULL REFERENCES public.users(id),
  name          text        NOT NULL,
  phone         text        NOT NULL,
  email         text,
  address       text,
  id_proof_url  text,                                   -- Aadhaar / PAN scan URL
  notes         text,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_customers_updated_at
  BEFORE UPDATE ON public.customers
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


-- ============================================================
-- TABLE 8 — sales
-- A plot sold to a customer. Only one ACTIVE sale per plot at a time
-- (enforced via partial unique index below).
-- total_deal_value and pending_amount are GENERATED.
-- total_paid and payment_status are maintained by triggers.
-- ============================================================
CREATE TABLE public.sales (
  id                    uuid               PRIMARY KEY DEFAULT gen_random_uuid(),
  plot_id               uuid               NOT NULL REFERENCES public.plots(id) ON DELETE RESTRICT,
  customer_id           uuid               NOT NULL REFERENCES public.customers(id) ON DELETE RESTRICT,
  admin_id              uuid               NOT NULL REFERENCES public.users(id),

  -- Pricing breakdown
  sale_price            numeric(12,2)      NOT NULL CHECK (sale_price > 0),
  registration_charges  numeric(12,2)      NOT NULL DEFAULT 0,  -- Maharashtra stamp duty
  other_charges         numeric(12,2)      NOT NULL DEFAULT 0,

  -- GENERATED totals (cannot reference other generated cols, so expand inline)
  total_deal_value      numeric(12,2)      GENERATED ALWAYS AS
                          (sale_price + registration_charges + other_charges) STORED,

  -- Maintained by trigger (sync_sale_totals)
  total_paid            numeric(12,2)      NOT NULL DEFAULT 0,

  -- GENERATED: pending = total_deal_value - total_paid (expanded inline)
  pending_amount        numeric(12,2)      GENERATED ALWAYS AS
                          (sale_price + registration_charges + other_charges - total_paid) STORED,

  -- Payment plan
  payment_type          payment_type       NOT NULL,
  emi_months            integer            CHECK (emi_months IS NULL OR emi_months > 0),
  emi_amount            numeric(12,2)      CHECK (emi_amount IS NULL OR emi_amount > 0),
  emi_start_date        date,

  sale_date             date               NOT NULL,

  -- Maintained by trigger
  payment_status        sale_payment_status NOT NULL DEFAULT 'partial',
  status                sale_status         NOT NULL DEFAULT 'active',

  agreement_url         text,
  notes                 text,

  created_at            timestamptz        NOT NULL DEFAULT now(),
  updated_at            timestamptz        NOT NULL DEFAULT now(),

  -- EMI fields required when payment_type = 'emi'
  CONSTRAINT chk_emi_fields CHECK (
    payment_type <> 'emi'
    OR (emi_months IS NOT NULL AND emi_amount IS NOT NULL AND emi_start_date IS NOT NULL)
  )
);

CREATE TRIGGER trg_sales_updated_at
  BEFORE UPDATE ON public.sales
  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- Only one ACTIVE sale per plot (cancelled sales don't block re-sale)
CREATE UNIQUE INDEX idx_sales_plot_active
  ON public.sales(plot_id) WHERE status = 'active';

-- Sync plots.status when a sale becomes active or is cancelled.
CREATE OR REPLACE FUNCTION public.sync_plot_status_on_sale()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'INSERT' AND NEW.status = 'active' THEN
    UPDATE public.plots
    SET status = 'sold', updated_at = now()
    WHERE id = NEW.plot_id;

  ELSIF TG_OP = 'UPDATE'
        AND NEW.status = 'cancelled'
        AND OLD.status = 'active' THEN
    UPDATE public.plots
    SET status = 'available', updated_at = now()
    WHERE id = NEW.plot_id;
  END IF;
  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_sale_sync_plot_status
  AFTER INSERT OR UPDATE OF status ON public.sales
  FOR EACH ROW EXECUTE FUNCTION public.sync_plot_status_on_sale();


-- ============================================================
-- TABLE 9 — sale_payments
-- Individual payments received from a customer for a sale.
-- Append-only in practice (no UPDATE/DELETE via RLS).
-- ============================================================
CREATE TABLE public.sale_payments (
  id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
  sale_id        uuid          NOT NULL REFERENCES public.sales(id) ON DELETE RESTRICT,
  amount         numeric(12,2) NOT NULL CHECK (amount > 0),
  payment_date   date          NOT NULL,
  payment_method payment_method NOT NULL,
  notes          text,
  created_at     timestamptz   NOT NULL DEFAULT now()
);

-- Maintain sales.total_paid and payment_status after each sale_payment change.
CREATE OR REPLACE FUNCTION public.sync_sale_totals()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
  v_sale_id        uuid;
  v_total_paid     numeric(12,2);
  v_total_deal     numeric(12,2);
  v_cur_status     sale_payment_status;
  v_new_status     sale_payment_status;
BEGIN
  v_sale_id := COALESCE(NEW.sale_id, OLD.sale_id);

  SELECT COALESCE(SUM(amount), 0)
  INTO v_total_paid
  FROM public.sale_payments
  WHERE sale_id = v_sale_id;

  SELECT total_deal_value, payment_status
  INTO v_total_deal, v_cur_status
  FROM public.sales
  WHERE id = v_sale_id;

  -- Determine new payment_status:
  -- fully_paid beats everything once reached.
  -- If currently overdue and still not fully paid, keep overdue (cron owns that flag).
  IF v_total_paid >= v_total_deal THEN
    v_new_status := 'fully_paid';
  ELSIF v_cur_status = 'overdue' THEN
    v_new_status := 'overdue';
  ELSE
    v_new_status := 'partial';
  END IF;

  UPDATE public.sales
  SET
    total_paid     = v_total_paid,
    payment_status = v_new_status,
    updated_at     = now()
  WHERE id = v_sale_id;

  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_sale_payments_sync
  AFTER INSERT OR UPDATE OF amount OR DELETE ON public.sale_payments
  FOR EACH ROW EXECUTE FUNCTION public.sync_sale_totals();


-- ============================================================
-- TABLE 10 — emi_schedule
-- Auto-generated installment rows, one per month per EMI sale.
-- Generated by Edge Function on sale creation (never by client directly).
-- Status lifecycle managed by daily cron.
-- ============================================================
CREATE TABLE public.emi_schedule (
  id                  uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  sale_id             uuid        NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
  installment_number  integer     NOT NULL CHECK (installment_number > 0),
  due_date            date        NOT NULL,
  amount              numeric(12,2) NOT NULL CHECK (amount > 0),
  status              emi_status  NOT NULL DEFAULT 'upcoming',
  paid_date           date,
  payment_id          uuid        REFERENCES public.sale_payments(id),
  created_at          timestamptz NOT NULL DEFAULT now(),

  UNIQUE (sale_id, installment_number)
);


-- ============================================================
-- VIEW — plots_view
-- Joins plots with their active sale to expose display_status
-- and status_color for map rendering.
-- sold_pending = sold but customer still owes money
-- sold_paid    = sold and fully paid
-- ============================================================
CREATE VIEW public.plots_view AS
SELECT
  p.*,
  -- Derived display status (sold splits into sold_pending / sold_paid)
  CASE
    WHEN p.status = 'sold' AND s.pending_amount > 0  THEN 'sold_pending'
    WHEN p.status = 'sold' AND s.pending_amount <= 0 THEN 'sold_paid'
    ELSE p.status::text
  END AS display_status,

  -- Hex color for map polygon fill
  CASE
    WHEN p.status = 'available'                           THEN '#22C55E'
    WHEN p.status = 'reserved'                            THEN '#F59E0B'
    WHEN p.status = 'sold' AND s.pending_amount > 0      THEN '#EF4444'
    WHEN p.status = 'sold' AND s.pending_amount <= 0     THEN '#991B1B'
    WHEN p.status = 'blocked'                             THEN '#6B7280'
  END AS status_color,

  -- Active sale fields surfaced for convenience
  s.id               AS sale_id,
  s.customer_id      AS customer_id,
  s.total_deal_value AS total_deal_value,
  s.total_paid       AS sale_total_paid,
  s.pending_amount   AS pending_amount,
  s.payment_status   AS sale_payment_status

FROM public.plots p
LEFT JOIN public.sales s
  ON s.plot_id = p.id AND s.status = 'active';


-- ============================================================
-- ROW LEVEL SECURITY
-- ============================================================

ALTER TABLE public.users            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.lands            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.land_payments    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.partners         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.partner_payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.plots            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sale_payments    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.emi_schedule     ENABLE ROW LEVEL SECURITY;

-- users: admin owns own row
CREATE POLICY "users_self_all" ON public.users
  FOR ALL TO authenticated
  USING  (id = auth.uid())
  WITH CHECK (id = auth.uid());

-- lands: admin owns lands they created
CREATE POLICY "lands_admin_all" ON public.lands
  FOR ALL TO authenticated
  USING  (admin_id = auth.uid())
  WITH CHECK (admin_id = auth.uid());

-- land_payments: allowed if parent land belongs to admin
CREATE POLICY "land_payments_admin_all" ON public.land_payments
  FOR ALL TO authenticated
  USING (
    land_id IN (
      SELECT id FROM public.lands WHERE admin_id = auth.uid()
    )
  )
  WITH CHECK (
    land_id IN (
      SELECT id FROM public.lands WHERE admin_id = auth.uid()
    )
  );

-- partners: allowed if parent land belongs to admin
CREATE POLICY "partners_admin_all" ON public.partners
  FOR ALL TO authenticated
  USING (
    land_id IN (
      SELECT id FROM public.lands WHERE admin_id = auth.uid()
    )
  )
  WITH CHECK (
    land_id IN (
      SELECT id FROM public.lands WHERE admin_id = auth.uid()
    )
  );

-- partner_payments: allowed if parent partner → land belongs to admin
CREATE POLICY "partner_payments_admin_all" ON public.partner_payments
  FOR ALL TO authenticated
  USING (
    partner_id IN (
      SELECT pt.id
      FROM public.partners pt
      JOIN public.lands l ON l.id = pt.land_id
      WHERE l.admin_id = auth.uid()
    )
  )
  WITH CHECK (
    partner_id IN (
      SELECT pt.id
      FROM public.partners pt
      JOIN public.lands l ON l.id = pt.land_id
      WHERE l.admin_id = auth.uid()
    )
  );

-- plots: allowed if parent land belongs to admin
CREATE POLICY "plots_admin_all" ON public.plots
  FOR ALL TO authenticated
  USING (
    land_id IN (
      SELECT id FROM public.lands WHERE admin_id = auth.uid()
    )
  )
  WITH CHECK (
    land_id IN (
      SELECT id FROM public.lands WHERE admin_id = auth.uid()
    )
  );

-- customers: admin owns customers they created
CREATE POLICY "customers_admin_all" ON public.customers
  FOR ALL TO authenticated
  USING  (admin_id = auth.uid())
  WITH CHECK (admin_id = auth.uid());

-- sales: allowed if parent plot → land belongs to admin
CREATE POLICY "sales_admin_all" ON public.sales
  FOR ALL TO authenticated
  USING (
    plot_id IN (
      SELECT pl.id
      FROM public.plots pl
      JOIN public.lands l ON l.id = pl.land_id
      WHERE l.admin_id = auth.uid()
    )
  )
  WITH CHECK (
    plot_id IN (
      SELECT pl.id
      FROM public.plots pl
      JOIN public.lands l ON l.id = pl.land_id
      WHERE l.admin_id = auth.uid()
    )
  );

-- sale_payments: allowed if parent sale → plot → land belongs to admin
CREATE POLICY "sale_payments_admin_all" ON public.sale_payments
  FOR ALL TO authenticated
  USING (
    sale_id IN (
      SELECT s.id
      FROM public.sales s
      JOIN public.plots pl ON pl.id = s.plot_id
      JOIN public.lands l  ON l.id  = pl.land_id
      WHERE l.admin_id = auth.uid()
    )
  )
  WITH CHECK (
    sale_id IN (
      SELECT s.id
      FROM public.sales s
      JOIN public.plots pl ON pl.id = s.plot_id
      JOIN public.lands l  ON l.id  = pl.land_id
      WHERE l.admin_id = auth.uid()
    )
  );

-- emi_schedule: cascades through sale → plot → land → admin
CREATE POLICY "emi_schedule_admin_all" ON public.emi_schedule
  FOR ALL TO authenticated
  USING (
    sale_id IN (
      SELECT s.id
      FROM public.sales s
      JOIN public.plots pl ON pl.id = s.plot_id
      JOIN public.lands l  ON l.id  = pl.land_id
      WHERE l.admin_id = auth.uid()
    )
  )
  WITH CHECK (
    sale_id IN (
      SELECT s.id
      FROM public.sales s
      JOIN public.plots pl ON pl.id = s.plot_id
      JOIN public.lands l  ON l.id  = pl.land_id
      WHERE l.admin_id = auth.uid()
    )
  );


-- ============================================================
-- INDEXES
-- ============================================================

-- Foreign key indexes (prevent seq scans on FK lookups)
CREATE INDEX idx_lands_admin_id              ON public.lands(admin_id);
CREATE INDEX idx_land_payments_land_id       ON public.land_payments(land_id);
CREATE INDEX idx_partners_land_id            ON public.partners(land_id);
CREATE INDEX idx_partner_payments_partner_id ON public.partner_payments(partner_id);
CREATE INDEX idx_plots_land_id               ON public.plots(land_id);
CREATE INDEX idx_customers_admin_id          ON public.customers(admin_id);
CREATE INDEX idx_sales_plot_id               ON public.sales(plot_id);
CREATE INDEX idx_sales_customer_id           ON public.sales(customer_id);
CREATE INDEX idx_sales_admin_id              ON public.sales(admin_id);
CREATE INDEX idx_sale_payments_sale_id       ON public.sale_payments(sale_id);
CREATE INDEX idx_emi_schedule_sale_id        ON public.emi_schedule(sale_id);

-- Map rendering: plots filtered by status per land
CREATE INDEX idx_plots_status               ON public.plots(status);
CREATE INDEX idx_plots_land_status          ON public.plots(land_id, status);

-- Cron: daily EMI status update queries on due_date + status
CREATE INDEX idx_emi_due_status             ON public.emi_schedule(due_date, status);

-- Dashboard / filter queries
CREATE INDEX idx_sales_status               ON public.sales(status);
CREATE INDEX idx_sales_payment_status       ON public.sales(payment_status);
CREATE INDEX idx_land_payments_land_date    ON public.land_payments(land_id, payment_date);
CREATE INDEX idx_sale_payments_sale_date    ON public.sale_payments(sale_id, payment_date);


-- ============================================================
-- STORAGE BUCKETS
-- Create via Supabase dashboard or CLI after running this migration.
--
-- bucket: "land-documents"
--   path pattern: {land_id}/{filename}
--   public: false
--   allowed MIME: application/pdf, image/*
--
-- bucket: "sale-documents"
--   path pattern: {sale_id}/{filename}
--   public: false
--   allowed MIME: application/pdf, image/*
--
-- bucket: "customer-id-proofs"
--   path pattern: {customer_id}/{filename}
--   public: false
--   allowed MIME: application/pdf, image/*
-- ============================================================

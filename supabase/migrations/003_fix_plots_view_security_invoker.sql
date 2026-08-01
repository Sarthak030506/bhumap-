-- ============================================================
-- BhuMap — Migration 003: Fix plots_view SECURITY INVOKER
--
-- Problem: Supabase implicitly creates views as SECURITY DEFINER,
-- which bypasses RLS on underlying tables (plots, sales).
-- A logged-in admin could potentially see another admin's plots
-- through this view.
--
-- Fix: Recreate plots_view as SECURITY INVOKER so PostgreSQL
-- evaluates it using the *querying user's* identity and RLS
-- policies — exactly the same protection as querying plots directly.
--
-- No data change. No schema change. View definition is identical.
-- Safe to run on a live database.
-- ============================================================

CREATE OR REPLACE VIEW public.plots_view
  WITH (security_invoker = true)               -- ← explicit SECURITY INVOKER
AS
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
    WHEN p.status = 'available'                       THEN '#22C55E'
    WHEN p.status = 'reserved'                        THEN '#F59E0B'
    WHEN p.status = 'sold' AND s.pending_amount > 0  THEN '#EF4444'
    WHEN p.status = 'sold' AND s.pending_amount <= 0 THEN '#991B1B'
    WHEN p.status = 'blocked'                         THEN '#6B7280'
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
-- Why SECURITY INVOKER is correct here:
--
-- plots_view is a pure join/projection of plots + their active sale.
-- It needs NO elevated privileges. The underlying tables already have:
--
--   plots:  RLS policy "plots_admin_all"
--           USING (land_id IN (SELECT id FROM lands WHERE admin_id = auth.uid()))
--
--   sales:  RLS policy "sales_admin_all"
--           USING (plot_id IN (SELECT pl.id FROM plots pl
--                              JOIN lands l ON l.id = pl.land_id
--                              WHERE l.admin_id = auth.uid()))
--
-- With SECURITY INVOKER, querying plots_view is identical in
-- security to querying plots + sales directly. Admin A only sees
-- their own plots. The view adds zero privilege escalation.
-- ============================================================

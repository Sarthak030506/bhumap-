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

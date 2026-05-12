-- ============================================================================
-- 002_land_owner_extras.sql
-- Adds operational columns to public.lands:
--   - acquisition_date    : when admin acquired the land (drives age-of-inventory)
--   - registration_status : 'pending' | 'registered' (gates plot sales legally)
-- No KYC / Aadhaar fields. App is operational, not a legal-record system.
-- RLS: existing "lands_admin_all" policy is FOR ALL, so new columns inherit.
-- ============================================================================

ALTER TABLE public.lands
  ADD COLUMN acquisition_date    date NOT NULL DEFAULT CURRENT_DATE,
  ADD COLUMN registration_status text NOT NULL DEFAULT 'pending'
    CHECK (registration_status IN ('pending', 'registered'));

CREATE INDEX idx_lands_registration_status
  ON public.lands(registration_status)
  WHERE registration_status = 'pending';

COMMENT ON COLUMN public.lands.acquisition_date    IS 'Date land was acquired from owner';
COMMENT ON COLUMN public.lands.registration_status IS 'pending until land registered in admin name; required before plot sales';

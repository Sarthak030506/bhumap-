-- Seed: Two test plots (one per land) to verify polygon rendering on the map.
-- Nashik-area coordinates so tiles are visible at zoom 15.

-- Plot A-01 on Sinhgad 5 acre land (available — green)
INSERT INTO public.plots
  (land_id, plot_number, area_sqft, boundary_coordinates, status, base_price_per_sqft, notes)

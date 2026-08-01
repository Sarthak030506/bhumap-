-- Seed: Two test plots (one per land) to verify polygon rendering on the map.
-- Nashik-area coordinates so tiles are visible at zoom 15.

-- Plot A-01 on Sinhgad 5 acre land (available — green)
INSERT INTO public.plots
  (land_id, plot_number, area_sqft, boundary_coordinates, status, base_price_per_sqft, notes)
VALUES
  (
    '6f5301b7-3881-4fe1-8f8f-480a5eefcb8c',
    'A-01',
    1200,
    '[{"lat":19.9975,"lng":73.7898},{"lat":19.9985,"lng":73.7898},{"lat":19.9985,"lng":73.7910},{"lat":19.9975,"lng":73.7910}]',
    'available',
    850,
    'Test plot – Sinhgad land'
  )
ON CONFLICT (land_id, plot_number) DO NOTHING;

-- Plot A-02 on Sinhgad 5 acre land (reserved — amber), slightly east of A-01
INSERT INTO public.plots
  (land_id, plot_number, area_sqft, boundary_coordinates, status, base_price_per_sqft, notes)
VALUES
  (
    '6f5301b7-3881-4fe1-8f8f-480a5eefcb8c',
    'A-02',
    1100,
    '[{"lat":19.9975,"lng":73.7910},{"lat":19.9985,"lng":73.7910},{"lat":19.9985,"lng":73.7921},{"lat":19.9975,"lng":73.7921}]',
    'reserved',
    900,
    'Test plot – Sinhgad land'
  )
ON CONFLICT (land_id, plot_number) DO NOTHING;

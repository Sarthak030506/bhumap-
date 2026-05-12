# Plot Rules

## Status Transitions
- Plot created → status = `available`
- Admin creates sale → plot status = `sold` (atomic with sale creation)
- Sale cancelled → plot status = `available`
- Optional: `reserved` state for admin holds — NOT in v1

## Constraints
- A plot can only have one active sale (status ≠ cancelled)
- plot_number is UNIQUE per project (enforced at DB level)
- boundary must have ≥ 3 coordinate points (validated before save)
- base_price is a guide price only — actual price lives on the sale row

## Plot Deletion
- Plots with any sale record (even cancelled) must NOT be deleted
- Only plots with no sales history can be deleted
- Admin UI should hide delete button once a sale exists

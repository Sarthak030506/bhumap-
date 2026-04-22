# Plot Status Colors

---

## Color Definitions

| Status | Fill Color | Stroke Color | Fill Opacity | Label |
|--------|-----------|--------------|--------------|-------|
| `available` | `#22C55E` (green) | `#16A34A` | 0.35 | Available |
| `reserved` | `#F59E0B` (amber) | `#D97706` | 0.35 | Reserved |
| `sold` | `#EF4444` (red) | `#DC2626` | 0.40 | Sold |

---

## Usage in Code

```tsx
export const PLOT_COLORS: Record<PlotStatus, { fill: string; stroke: string; fillOpacity: number }> = {
  available: { fill: '#22C55E', stroke: '#16A34A', fillOpacity: 0.35 },
  reserved:  { fill: '#F59E0B', stroke: '#D97706', fillOpacity: 0.35 },
  sold:      { fill: '#EF4444', stroke: '#DC2626', fillOpacity: 0.40 },
};

export const getFillColor = (status: PlotStatus): string => {
  const { fill, fillOpacity } = PLOT_COLORS[status];
  // React Native Maps fillColor accepts rgba string or hex with alpha
  return fill + Math.round(fillOpacity * 255).toString(16).padStart(2, '0');
  // e.g. '#22C55E59' (35% opacity)
};

export const getStrokeColor = (status: PlotStatus): string => PLOT_COLORS[status].stroke;
```

---

## Map Legend Component

Display a legend overlay on the map view:

```tsx
const MapLegend = () => (
  <View style={styles.legend}>
    {(['available', 'reserved', 'sold'] as PlotStatus[]).map(status => (
      <View key={status} style={styles.legendRow}>
        <View style={[styles.colorSwatch, { backgroundColor: PLOT_COLORS[status].fill }]} />
        <Text style={styles.legendLabel}>{STATUS_LABELS[status]}</Text>
      </View>
    ))}
  </View>
);
```

---

## Role-Based Color Behavior

| Role | Visible Statuses | Notes |
|------|-----------------|-------|
| Admin | available, reserved, sold | All colors shown |
| Buyer | sold (own plot only) | Only their plot rendered; shown in red/sold |
| Partner | available, reserved, sold | All plots in their projects shown |
| Agent | available only | Only green plots rendered; sold/reserved hidden by RLS |

Agent map shows only green (available) plots — the absence of red/amber plots is intentional and enforced by RLS, not just by color logic.

---

## Accessibility Notes

- Color alone is not sufficient for accessibility; plots should also show their `plot_number` label
- On tap, show a callout/tooltip with plot number, area, and price (role-appropriate data)
- Consider adding a pattern fill option for users with color-vision deficiencies (post-MVP)

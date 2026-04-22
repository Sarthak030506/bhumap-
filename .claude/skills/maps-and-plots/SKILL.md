---
name: maps-and-plots
description: >
  Trigger phrases: polygon, boundary, coordinates, map, plot drawing, Google Maps,
  React Native Maps, coordinate storage, boundary overlap, plot rendering, map markers,
  plot colors, status colors
---

# Maps and Plots Skill

## When to activate
Load this skill when the conversation involves:
- Storing or retrieving plot boundary polygons
- Rendering plots on a Google Maps view
- Drawing plot boundaries in the admin create-plot flow
- Checking or preventing boundary overlaps between plots
- Coloring plots by status on the map
- React Native Maps component design decisions

## Reference files
| File | Contents |
|------|---------|
| `references/coordinate-logic.md` | How boundaries are stored (jsonb), drawn (polygon tool), and rendered (MapView polygons) |
| `references/status-colors.md` | Color codes per plot status and how the map legend works |

## Key design decisions
- Boundaries stored as `jsonb`: `[{ "lat": 17.385, "lng": 78.486 }, ...]`
- Minimum 3 coordinate points to form a valid polygon
- React Native Maps `<Polygon>` component renders the coordinates array directly
- Color driven by `plots.status` — see `status-colors.md` for exact hex values
- Boundary overlap validation happens in the Edge Function or admin UI — not enforced by DB constraint

# Coordinate Logic — Plot Boundaries

---

## Storage Format

Plot boundaries are stored in `plots.boundary` as a `jsonb` column:

```json
[
  { "lat": 17.385044, "lng": 78.486671 },
  { "lat": 17.385112, "lng": 78.486820 },
  { "lat": 17.384990, "lng": 78.486920 },
  { "lat": 17.384870, "lng": 78.486780 },
  { "lat": 17.385044, "lng": 78.486671 }
]
```

Rules:
- Minimum 3 points to form a valid polygon
- Last point should equal the first point (closed polygon) — or the rendering layer closes it
- No SRID/PostGIS required; `jsonb` is sufficient for display-only polygon rendering
- If PostGIS geometric validation is needed later (overlap detection), migrate to `geography` type

---

## Drawing Boundaries (Admin — Create Plot Screen)

Admin draws the plot polygon on Google Maps:

1. Admin taps `[Draw Plot]` button — enters polygon draw mode
2. Each tap on the map adds a `{ lat, lng }` vertex to a local array
3. Minimum 3 taps before `[Save Polygon]` button becomes active
4. Admin taps `[Save Polygon]` — polygon is closed and stored as `boundary` field
5. Admin can tap `[Clear]` to restart the drawing

Implementation:
```tsx
// React Native Maps — store drawn points in component state
const [polygonPoints, setPolygonPoints] = useState<LatLng[]>([]);

// On map press in draw mode:
const handleMapPress = (e: MapPressEvent) => {
  setPolygonPoints(prev => [...prev, e.nativeEvent.coordinate]);
};

// Render in-progress polygon
<Polygon coordinates={polygonPoints} strokeColor="#007AFF" fillColor="rgba(0,122,255,0.2)" />
```

---

## Rendering Boundaries (All Role Map Views)

Each plot is rendered as a `<Polygon>` on `<MapView>`:

```tsx
import MapView, { Polygon } from 'react-native-maps';

// plots: Plot[] fetched from Supabase
{plots.map(plot => (
  <Polygon
    key={plot.id}
    coordinates={plot.boundary}           // jsonb → LatLng[] — no transform needed
    strokeColor={getStrokeColor(plot.status)}
    fillColor={getFillColor(plot.status)}
    strokeWidth={2}
    tappable={true}
    onPress={() => handlePlotPress(plot)}
  />
))}
```

Notes:
- `plot.boundary` from Supabase is already `LatLng[]` (jsonb parsed automatically by JS)
- `getStrokeColor` / `getFillColor` — see `status-colors.md`
- `tappable={true}` enables `onPress` on the polygon, not just the marker

---

## Boundary Overlap Detection

**Client-side (UX warning during draw):**
- As admin draws, check if the in-progress polygon intersects any existing plot boundary
- Use a point-in-polygon check or line intersection test (e.g., `@turf/intersect` from `@turf/turf`)
- Show visual warning overlay if overlap detected; don't hard-block (admin override allowed)

**Server-side (validation before save):**
```sql
-- Option A: Use PostGIS (requires enabling extension)
-- ST_Intersects(new_boundary::geometry, existing_boundary::geometry)

-- Option B: Rely on admin UI warning only (simpler for v1)
```

For v1, client-side warning via Turf.js is sufficient. PostGIS can be added post-MVP.

---

## Map Region / Camera

On loading the map for a project, fit the camera to show all plots:

```tsx
const fitToPlots = (plots: Plot[]) => {
  const allCoords = plots.flatMap(p => p.boundary);
  const lats = allCoords.map(c => c.lat);
  const lngs = allCoords.map(c => c.lng);
  return {
    latitude: (Math.max(...lats) + Math.min(...lats)) / 2,
    longitude: (Math.max(...lngs) + Math.min(...lngs)) / 2,
    latitudeDelta: (Math.max(...lats) - Math.min(...lats)) * 1.3,
    longitudeDelta: (Math.max(...lngs) - Math.min(...lngs)) * 1.3,
  };
};
```

---

## Coordinate Validation

Before saving a boundary:
1. Array length ≥ 3
2. All objects have `lat` (number, -90 to 90) and `lng` (number, -180 to 180)
3. No two adjacent points are identical (prevents zero-length edges)
4. Area > minimum threshold (prevents accidental single-tap saves)

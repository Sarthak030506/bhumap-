# Architectural Decisions Log

Format: `[Date] Decision — Reason`
Append-only. Never edit past entries.

---

## Stack Migration

[2026-07-31] **Migrated from React Native / Expo to Kotlin Multiplatform (KMP).**
Prior stack (documented in git history and original `.claude/memory/` files):
React Native + Expo Router + Supabase JS SDK + Zustand + React Query + react-native-maps.
Current stack: Kotlin Multiplatform 2.0.21 + Compose Multiplatform 1.7.1 + Supabase-kt 3.0.0
+ SQLDelight 2.0.2 + Koin 4.0.0 + JetBrains Navigation Compose.
No comment in codebase explains the reason; migration happened before this session.

---

## Data Layer

[2026-07-31] **Local-first pattern: SQLDelight cache + Supabase pull-sync.**
`observeAll()` in each repository returns `Flow` from SQLDelight (instant, reactive).
`sync()` pulls from Supabase PostgREST → upserts rows locally. Called in ViewModel `init`.
Writes go Supabase-first, then `sync()` for consistency.
Reason: offline support + reactive UI without real-time subscriptions in Phase 1.

[2026-07-31] **Supabase credentials flow: `local.properties` → BuildConfig → Koin.**
`local.properties` holds `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
`buildconfig` Gradle plugin (`com.github.gmazzo.buildconfig:5.4.0`) bakes them into
`BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_ANON_KEY` at compile time.
`NetworkModule.kt` reads them and provides `SupabaseClient` as a Koin singleton.
`local.properties` is git-ignored. Must be manually populated from Supabase project dashboard.

[2026-07-31] **Supabase project URL: `https://izpypzoxojgiclosgtms.supabase.co`.**
Project was on free tier and auto-paused during initial testing (July 31 2026).
Reactivated manually. Free-tier projects pause after 7 days of inactivity.

[2026-07-31] **`MAPS_API_KEY` in `local.properties` → `AndroidManifest` via `manifestPlaceholders`.**
Also baked into `BuildConfig.MAPS_API_KEY`. Both Google Maps Compose and OSMDroid are
included as Android dependencies. OSMDroid is used in production (no API key / billing required);
Google Maps Compose is kept for potential fallback.

---

## Maps

[2026-07-31] **OSMDroid chosen over Google Maps Compose for Android map tile rendering.**
Reason: Google Maps Compose requires a Maps API key AND a billing account enabled.
OSMDroid uses OpenStreetMap tiles — free, no key required.
Android actual: `composeApp/src/androidMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt`
iOS actual (deferred): `composeApp/src/iosMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt` uses MapKit.

[2026-07-31] **Plot boundaries stored as raw GeoJSON TEXT in `plot.boundary_json`.**
Format: `[[lng,lat],[lng,lat],...]` — a flat JSON array of coordinate pairs.
Parsed by `parseBoundaryCoords()` in `PlatformMapView.kt` (Android) and iOS actual.
PostGIS not used for Phase 1. Overlap detection deferred.

---

## Auth

[2026-07-31] **Phone OTP auth via `supabase-kt` `Auth` plugin.**
`AuthRepository.sendOtp()` → `supabase.auth.signInWith(OTP) { this.phone = phone }`.
`AuthRepository.verifyOtp()` → `supabase.auth.verifyPhoneOtp(type=SMS, phone, token)`.
Session persistence handled by Supabase-kt internally (platform-specific secure storage).
`authRepo.isLoggedIn` (`currentSessionOrNull() != null`) used in `AppNavHost.kt:30`
to set start destination — no loading state / splash needed.

---

## iOS

[2026-07-31] **iOS target configured but not buildable on Windows — deferred.**
`iosX64()`, `iosArm64()`, `iosSimulatorArm64()` declared in `composeApp/build.gradle.kts:38–47`.
All iOS actual implementations exist in `composeApp/src/iosMain/`.
Gradle emits: `The following Kotlin/Native targets cannot be built on this machine and are disabled`.
Will be enabled when Mac CI or developer machine is available.

---

## DashboardViewModel

[2026-07-31] **`pendingEmis` hardcoded to `0` in DashboardStats — Phase 2 TODO.**
`DashboardViewModel.kt:36` comment: "populated from EmiRepository (Phase 2)".
`EmiRepository` does not exist yet. `emi_schedule` SQLDelight schema is ready.

---

## PlotRepository & Map Polygon Wiring

[2026-07-31] **`PlotRepository` created following `LandRepository` pattern.**
File: `composeApp/src/commonMain/kotlin/com/bhumap/app/data/repository/PlotRepository.kt`
Uses a private `RemotePlot` DTO with `@SerialName` annotations to bridge Supabase
snake_case column names (`boundary_coordinates`, `base_price_per_sqft`, etc.) to
Kotlin camelCase fields. No `@SerialName` annotations on the shared `Plot` domain model
— the DTO lives only in the repository layer.

[2026-07-31] **Boundary coordinate format conversion: Supabase jsonb → SQLDelight TEXT.**
Supabase `plots.boundary_coordinates` stores `[{lat: x, lng: y}, ...]` JSONB objects.
SQLDelight `plot.boundary_json` stores `[[lng,lat],...]` TEXT (flat array-of-arrays).
Conversion happens in `PlotRepository.convertBoundaryToJson()` at sync time, so
`parseBoundaryJson()` in `PlatformMapView.kt` (which expects `[[lng,lat],...]`) requires no changes.
Plots with fewer than 3 boundary points are stored as `null` and excluded by `selectAllWithBoundary`.

[2026-07-31] **`MapViewModel` now takes `PlotRepository` constructor parameter via Koin `viewModelOf`.**
`viewModelOf(::MapViewModel)` in `AppModule.kt` lets Koin auto-resolve the `PlotRepository` singleton.
`getAllPlotsWithBoundaries()` Flow observed in `init` — reactive to every DB write.
`sync()` called in a separate `launch` in `init` — mirrors `LandViewModel` / `CustomerViewModel` pattern.

[2026-07-31] **Map default center changed from Nashik to Maharashtra geographic center.**
`GeoPoint(19.9975, 73.7898)` → `GeoPoint(19.7515, 75.7139)`.
Nashik was the developer's city; Maharashtra center is appropriate for an app used across the state.
If plots exist, map centers on first plot's first boundary coordinate instead.

[2026-07-31] **Polygon fill/stroke colors updated to match `plots_view` status_color palette.**
Fill alpha changed from `0x55` (85) to `128` (≈50%) per spec.
Stroke colors unified with fill hue at alpha=255.
BLOCKED stroke was previously using terracotta (`#C8552B`) — corrected to slate (`#6B7280`).

[2026-07-31] **`selectAllWithBoundary` named query added to `Plot.sq`.**
Filters `WHERE boundary_json IS NOT NULL AND boundary_json != ''` in SQL rather than
filtering in Kotlin — avoids loading all plots into memory to discard null-boundary ones.

[2026-08-04] **Map tile layer switched to Esri World Imagery Satellite Tiles.**
File: `composeApp/src/androidMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt`
Replaced OSM Mapnik tile source with `XYTileSource("EsriWorldImagery", 5, 20, 256, ".jpg", ["https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"])`.
Free tile source requiring zero API keys. Default map center set to Maharashtra `GeoPoint(19.7515, 75.7139)`, zoom `7.0`.
Removed default osmdroid `zoomController` +/- buttons.

[2026-08-04] **Interactive Polygon Drawing Mode & Premium Map UI Overlays.**
Files: `MapScreen.kt`, `MapViewModel.kt`, `PlatformMapView.kt`
Added Top-Left Floating KPI Card (`#1A1A1A` 80% alpha) displaying live status counts, and Bottom-Left Legend Card.
Added interactive draw mode with `MapEventsOverlay`, `Polyline` connection, draft `Polygon` fill, point markers, and "Draw Plot" FAB.
Completing drawing opens `SavePlotDialog` to select land and insert plot boundary coordinates directly into Supabase PostgREST & SQLDelight cache.

[2026-08-04] **LOCAL-FIRST plot insert — SQLDelight write before Supabase push.**
File: `composeApp/src/commonMain/kotlin/com/bhumap/app/data/repository/PlotRepository.kt`
`insertPlot()` now generates a client-side UUID (`kotlin.uuid.Uuid.random()`) and ISO timestamp
(`io.ktor.util.date.GMTDate`), writes to SQLDelight FIRST so the polygon renders immediately
on the map, THEN pushes to Supabase PostgREST. If Supabase fails (offline / network error),
the local row persists — data is never lost. The Snackbar message says "Saved locally. Cloud
sync failed: ..." so the user knows the polygon is safe but not yet synced.
Rationale: Edge-case-hunter report identified issue #4 (🔴 silent data loss on Supabase failure)
as the highest-priority fix. Local-first is the standard pattern for offline-resilient mobile apps.

[2026-08-04] **Error feedback always visible — Snackbar for state.error.**
File: `composeApp/src/commonMain/kotlin/com/bhumap/app/ui/map/MapScreen.kt`
Added `SnackbarHost` + `LaunchedEffect(state.error)` that shows any error string as a
styled Snackbar (dark #1A1A1A background, white text, Long duration). After display,
calls `vm.clearError()` to reset. No error is ever silently swallowed in UI.

[2026-08-04] **Empty lands guard in SavePlotDialog — "Go to Land" empty state.**
Files: `MapScreen.kt`, `AppNavHost.kt`
When `lands` list is empty, `SavePlotDialog` hides all form fields and shows:
"No lands added yet. Add a land first before saving plots." with a "Go to Land" button
that dismisses the dialog, cancels draw mode, and navigates to `Screen.LandList`.
`MapScreen` now takes an optional `onNavigateToLand: (() -> Unit)?` parameter,
wired in `AppNavHost.kt` to `navController.navigate(Screen.LandList.route)`.

[2026-08-04] **Duplicate tap deduplication — haversine < 1m skip.**
File: `composeApp/src/commonMain/kotlin/com/bhumap/app/ui/map/MapViewModel.kt`
`MapPoint` gained a `distanceMetersTo()` method (Haversine formula). `addDrawingPoint()`
skips the point silently with a log message if it's within 1 meter of the last point.
Prevents degenerate zero-length polygon edges and invalid GeoJSON.

[2026-08-04] **Map viewport persists across rotation — saved in ViewModel state.**
Files: `MapViewModel.kt`, `PlatformMapView.kt` (Android + iOS)
`MapUiState` gained `mapCenter: MapPoint?` and `mapZoom: Double` fields.
`PlatformMapView.kt` (Android) adds a `MapListener` that calls `onCameraMoved()` on every
scroll/zoom, saving the viewport to ViewModel state. On `factory` recreate (rotation),
the saved center/zoom is restored instead of jumping to Maharashtra default or first plot.

[2026-08-04] **Boundary parse failures are logged, never silently swallowed.**
File: `composeApp/src/androidMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt`
`parseBoundaryJson()` catch block now prints raw JSON (first 120 chars) and exception
message to logcat. Previously `catch(_: Exception) { emptyList() }` made debugging blind.

[2026-08-04] **Add Land Supabase schema column mapping & Local-First save fix.**
File: `composeApp/src/commonMain/kotlin/com/bhumap/app/data/repository/LandRepository.kt`
Fixed error `Could not find the 'areaAcres' column of 'lands' in the schema cache` on Add Land save:
- `LandRepository.kt` now maps domain `areaAcres` to PostgreSQL `total_area_sqft` (`areaAcres * 43560.0`), `totalCost` to `agreed_price`, `location` to `village` / `location_description`, `createdAt` to `created_at`, `updatedAt` to `updated_at`.
- Updated `insert()` to write to SQLDelight FIRST (local-first) before pushing JSON payload to PostgREST.

[2026-08-04] **Google Satellite Tile Source (Option B) for High-Zoom India Rural Coverage.**
File: `composeApp/src/androidMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt`
Replaced Esri World Imagery with **Google Satellite** (`https://mt{0-3}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}`) via custom `OnlineTileSourceBase`.
- **Rationale**: Esri satellite tiles suffer from missing coverage/gray tiles at zoom levels >= 17 in rural India. Google Satellite provides continuous high-resolution imagery down to max zoom 20 across all survey numbers and plot boundaries in Maharashtra.
- **Performance**: Round-robins requests across `mt0`, `mt1`, `mt2`, `mt3` subdomains for maximum parallel tile fetch speed.
[2026-08-04] **Two Draw Modes (Land Boundary & Plot) Architecture & Centroid Map Animation.**
Files: `MapScreen.kt`, `MapViewModel.kt`, `AddLandScreen.kt`, `LandViewModel.kt`, `LandRepository.kt`, `AppNavHost.kt`, `Screen.kt`, `Models.kt`
Replaced single "Draw Plot" FAB with two-phase draw mode selector:
- **Draw Type Sheet**: Tapping FAB opens `DrawTypeSelectorSheet` allowing user to choose between "Land Boundary" and "Plot".
- **Draw Land Flow**: `DRAWING_LAND` mode shows "Drawing land boundary" banner. Completing polygon serializes coordinates as JSON array and navigates to `AddLandScreen` via `land/add?boundary={boundary}` route. `AddLandScreen` shows a green "✓ Boundary marked on map" confirmation chip and saves `boundary_coordinates` with land record.
- **Draw Plot Flow**: `SELECTING_LAND` mode presents `SelectParentLandSheet`. Selecting a land calculates its boundary centroid (`avg_lat`, `avg_lng`), animates the map to zoom 17, and enters `DRAWING_PLOT` mode with top banner "Drawing plot in [land name]". Completing polygon opens `SavePlotDialog` with parent land pre-selected and locked.
- **State Machine**: Managed in `MapViewModel` via `DrawMode` enum (`NONE`, `SELECTING_TYPE`, `SELECTING_LAND`, `DRAWING_LAND`, `DRAWING_PLOT`).






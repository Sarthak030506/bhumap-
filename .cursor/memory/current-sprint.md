# Current Sprint — BhuMap KMP v1

Last updated: 2026-07-31 (Map polygon wiring)
Sprint goal: Complete Phase 1 Admin-only build (Android).

---

## Working State (confirmed on physical Android device)

### ✅ LoginScreen — WORKING
- File: `composeApp/src/commonMain/kotlin/com/bhumap/app/ui/auth/LoginScreen.kt`
- Phone number input + "Send OTP" button renders correctly.
- `AuthRepository.sendOtp()` calls `supabase.auth.signInWith(OTP)`.
- Evidence: Supabase-Core logcat showed POST to `/auth/v1/token` attempted.
- Caveat: Supabase project was paused during initial testing → DNS failure.
  Project reactivated. OTP send not re-confirmed after reactivation.

### ✅ MapScreen — WORKING (Esri Satellite + Draw Mode + Edge Case Fixes)
- File: `composeApp/src/commonMain/kotlin/com/bhumap/app/ui/map/MapScreen.kt`
- Android actual: `composeApp/src/androidMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt`
- **Esri World Imagery Satellite Tiles**: Free, no API key, zoom 5–20.
- **Default center**: Maharashtra `GeoPoint(19.7515, 75.7139)`, zoom 7.0.
- **Top-Left KPI Card** + **Bottom-Left Legend Card** (`#1A1A1A` 80% alpha).
- **Interactive Polygon Draw Mode** with dedup, local-first save, error feedback.
- **Edge case fixes applied (2026-08-04):**
  - 🔴 FIX 1: LOCAL-FIRST insert — SQLDelight write BEFORE Supabase push.
    Supabase failure → polygon still renders from local DB. Snackbar shows error.
  - 🔴 FIX 2: Empty lands → SavePlotDialog shows "No lands added yet" with
    "Go to Land" button that navigates to LandList tab.
  - 🟡 FIX 3: Duplicate tap dedup — Haversine < 1m → skip silently.
  - 🟡 FIX 4: Viewport persists across rotation via ViewModel state
    (`mapCenter` + `mapZoom` saved on camera move, restored on factory recreate).
  - 🟡 FIX 5: Parse failures logged with raw JSON + error message, not silently swallowed.
- **osmdroid User-Agent & Tile Cache Configured**:
  - `BhumapApplication.kt` & `PlatformMapView.kt` explicitly set `userAgentValue = "BhuMap/1.0 (Android)"`.
  - Internal cache paths set: `osmdroidBasePath` = `cacheDir/osmdroid`, `osmdroidTileCache` = `cacheDir/osmdroid/tiles`.
  - Added `WRITE_EXTERNAL_STORAGE` permission (maxSdkVersion=32) in `AndroidManifest.xml`.
- **Deferred to Phase 2:** self-intersecting polygon validation, overlap detection, offline tile caching.


### ✅ DashboardScreen — WORKING (UI renders, data empty)
- File: `composeApp/src/commonMain/kotlin/com/bhumap/app/ui/dashboard/DashboardScreen.kt`
- `DashboardViewModel` resolved by Koin at `07-31 12:20:37` (logcat evidence).
- Calls `landRepo.sync()` and `customerRepo.sync()` on init.
- KPI cards show 0 values until Supabase `lands` / `customers` tables have data.
- `pendingEmis` hardcoded to `0` — comment in `DashboardViewModel.kt:36`: "populated from EmiRepository (Phase 2)".

---

## Auth Flow Status

### OTP end-to-end — NOT CONFIRMED
- `sendOtp()` → Supabase `POST /auth/v1/otp` ← DNS failed (project paused). Not re-tested after reactivation.
- `verifyOtp()` → `supabase.auth.verifyPhoneOtp()` — code exists, never run successfully in this session.
- Session persistence: `supabase.auth.currentSessionOrNull()` used in `AppNavHost.kt:30` to decide start destination.
- **Next action:** rebuild with active Supabase, enter real phone, confirm SMS arrives, enter OTP, confirm Dashboard navigation.

---

## Repositories — All 9 Complete & Registered in Koin

| Repository | File | Supabase wired | SQLDelight wired | Status |
|---|---|---|---|---|
| `AuthRepository` | `data/repository/AuthRepository.kt` | ✅ sendOtp / verifyOtp / signOut | N/A | Complete |
| `LandRepository` | `data/repository/LandRepository.kt` | ✅ select / insert / update / delete | ✅ upsert / observeAll | Complete |
| `CustomerRepository` | `data/repository/CustomerRepository.kt` | ✅ select / insert | ✅ upsert / observeAll | Complete |
| `PlotRepository` | `data/repository/PlotRepository.kt` | ✅ select / insertPlot | ✅ upsert / getAllPlotsWithBoundaries | Complete |
| `TransactionRepository` | `data/repository/TransactionRepository.kt` | ✅ select / insert | ✅ insert / observeBySale / observeByEntity | Complete |
| `SaleRepository` | `data/repository/SaleRepository.kt` | ✅ select / insert / update | ✅ upsert / observeBySaleId / observeByPlotId | Complete |
| `EmiRepository` | `data/repository/EmiRepository.kt` | ✅ select / insert / update | ✅ upsert / generateSchedule / markPaid | Complete |
| `PartnerRepository` | `data/repository/PartnerRepository.kt` | ✅ select / insert / delete | ✅ upsert / observeByLandId | Complete |
| `FarmerRepository` | `data/repository/FarmerRepository.kt` | ✅ select / insert / delete | ✅ upsert / observeByLandId | Complete |

All 9 repositories use local-first writes, SQLDelight reactive Flows, sync logging, and are registered as Koin singletons in `AppModule.kt`.

---

## Screens — What Exists vs. Functional

| Screen | File | Routed in NavHost | Renders on device | Data wired |
|---|---|---|---|---|
| `LoginScreen` | `ui/auth/LoginScreen.kt` | ✅ line 80 | ✅ confirmed | ✅ partial (OTP send untested after fix) |
| `OtpScreen` | `ui/auth/OtpScreen.kt` | ✅ line 87 | ❓ not confirmed | ❓ verifyOtp not run end-to-end |
| `DashboardScreen` | `ui/dashboard/DashboardScreen.kt` | ✅ line 100 | ✅ confirmed | ✅ (empty until data exists) |
| `LandListScreen` | `ui/land/LandListScreen.kt` | ✅ line 101 | ❓ not confirmed | ✅ wired to LandRepository |
| `AddLandScreen` | `ui/land/AddLandScreen.kt` | ✅ line 115 | ❓ not confirmed | ✅ wired to LandRepository |
| `LandDetailScreen` | `ui/land/LandDetailScreen.kt` | ✅ line 116 | ❓ not confirmed | ⚠️ LandRepository only, no PlotRepository |
| `MapScreen` | `ui/map/MapScreen.kt` | ✅ line 107 | ✅ confirmed (tiles) | ❌ PlotRepository missing |
| `CustomerListScreen` | `ui/customers/CustomerListScreen.kt` | ✅ line 108 | ❓ not confirmed | ✅ wired to CustomerRepository |
| `CustomerDetailScreen` | `ui/customers/CustomerDetailScreen.kt` | ✅ line 122 | ❓ not confirmed | ⚠️ no SaleRepository / EmiRepository |

---

## Navigation Structure

- Entry point: `AppNavHost.kt` → checks `authRepo.isLoggedIn` to set start destination.
- Bottom navigation bar visible on: Dashboard, LandList, Map, Customers.
- Defined in: `ui/navigation/Screen.kt` (routes) + `AppNavHost.kt` (composables).

## DI Module Structure

| Module | File | Provides |
|---|---|---|
| `networkModule` | `di/NetworkModule.kt` | `SupabaseClient` (singleton) |
| `databaseModule` | `di/DatabaseModule.kt` (expect/actual) | `BhumapDatabase` (singleton) |
| `repositoryModule` | `di/RepositoryModule.kt` | All repositories |
| `viewModelModule` | `di/ViewModelModule.kt` | All ViewModels |

---

## Immediate Next Steps (priority order)

1. **Confirm OTP auth end-to-end** — rebuild with active Supabase → real phone → verify SMS → enter token → Dashboard navigates.
2. **Build `PlotRepository`** — `Plot.sq` schema ready; wire `supabase.postgrest["plots"]` → SQLDelight upsert.
3. **Wire MapViewModel to PlotRepository** — replace `TODO` at `MapViewModel.kt:21`; draw polygons via `boundary_json`.
4. **Add GPS / "my location" button** to `MapScreen` / `PlatformMapView.kt` (Android: `FusedLocationProviderClient`).
5. **Build `FarmerRepository`** and wire into `LandDetailScreen`.
6. **Build `PartnerRepository`** and wire into `LandDetailScreen`.
7. **Build `SaleRepository`** + **`EmiRepository`** for `CustomerDetailScreen`.
8. **Build `TransactionRepository`** for payment recording across all detail screens.

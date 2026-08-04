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

### ✅ MapScreen — WORKING (Esri Satellite Tiles + Polygon Drawing Mode)
- File: `composeApp/src/commonMain/kotlin/com/bhumap/app/ui/map/MapScreen.kt`
- Android actual: `composeApp/src/androidMain/kotlin/com/bhumap/app/ui/map/PlatformMapView.kt`
- **Esri World Imagery Satellite Tiles**: High-resolution free satellite map layer (`https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}`), min zoom 5, max zoom 20, zero API keys required.
- **Default center**: Maharashtra state center `GeoPoint(19.7515, 75.7139)`, zoom 7.0. Auto-animates to plot locations when plots arrive.
- **Top-Left Floating KPI Card**: `#1A1A1A` 80% alpha card displaying real-time counts of Available, Reserved, and Sold plots.
- **Bottom-Left Legend Card**: `#1A1A1A` 80% alpha card with 5 status color dots.
- **Interactive Polygon Draw Mode**: Tap "Draw Plot" FAB → tap points on satellite map → `MapEventsOverlay` connects points with lines & draft polygon fill → "Complete" opens `SavePlotDialog` → saves plot directly into Supabase PostgREST & local SQLDelight cache.
- Outdated osmdroid default zoom +/- buttons removed.

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

## Repositories — What Exists vs. TODO

| Repository | File | Supabase wired | SQLDelight wired | Status |
|---|---|---|---|---|
| `AuthRepository` | `data/repository/AuthRepository.kt` | ✅ sendOtp / verifyOtp / signOut | N/A | Complete |
| `LandRepository` | `data/repository/LandRepository.kt` | ✅ select / insert / update / delete | ✅ upsert / observeAll | Complete |
| `CustomerRepository` | `data/repository/CustomerRepository.kt` | ✅ select / insert | ✅ upsert / observeAll | Complete |
| `PlotRepository` | `data/repository/PlotRepository.kt` | ✅ select (sync) | ✅ upsert / getAllPlotsWithBoundaries | **Complete** |
| `FarmerRepository` | ❌ Does not exist | — | — | **TODO** |
| `PartnerRepository` | ❌ Does not exist | — | — | **TODO** |
| `SaleRepository` | ❌ Does not exist | — | — | **TODO** |
| `TransactionRepository` | ❌ Does not exist | — | — | **TODO** |
| `EmiRepository` | ❌ Does not exist | — | — | **TODO** |

SQLDelight schemas exist for all entities (`Land.sq`, `Plot.sq`, `Farmer.sq`, `Partner.sq`,
`Customer.sq`, `Sale.sq`, `Transaction.sq`, `EmiSchedule.sq`) — the `.sq` files compile and
generate query classes. Only the Kotlin repository wrappers are missing.

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

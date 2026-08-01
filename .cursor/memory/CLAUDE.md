# Project Memory — BhuMap (KMP)

This directory contains structured memory for the BhuMap land developer platform.
Load relevant files based on the topic at hand.

## Quick Facts

- **App:** BhuMap — land developer platform (plots, sales, EMI, farmer/partner payments)
- **Roles:** Admin only (Phase 1 active). Agent/Buyer deferred to Phase 2.
- **Package:** `com.bhumap.app`

## Tech Stack (exact verified versions)

| Layer | Library | Version | Source |
|---|---|---|---|
| Language | Kotlin Multiplatform | 2.0.21 | `libs.versions.toml:6` |
| UI | Compose Multiplatform | 1.7.1 | `libs.versions.toml:7` |
| Remote DB | Supabase-kt (Auth + PostgREST + Storage) | 3.0.0 | `libs.versions.toml:21` |
| Local DB | SQLDelight | 2.0.2 | `libs.versions.toml:20` |
| Maps (Android) | osmdroid | 6.1.18 | `composeApp/build.gradle.kts:64` |
| Maps (Android alt) | Maps Compose + Play Services | 6.1.0 / 19.0.0 | `libs.versions.toml:15,18` |
| DI | Koin | 4.0.0 | `libs.versions.toml:11` |
| Navigation | JetBrains Navigation Compose | 2.8.0-alpha13 | `libs.versions.toml:17` |
| HTTP | Ktor | 3.0.1 | `libs.versions.toml:14` |
| Settings | multiplatform-settings | 1.2.0 | `libs.versions.toml:16` |

## Platform Targets

- **Android:** Actively building and running. `minSdk=26`, `targetSdk=35`, `compileSdk=35`.
- **iOS:** Configured (`iosX64`, `iosArm64`, `iosSimulatorArm64`) with `iosMain/` actual implementations,
  but **cannot build on Windows**. All iOS targets are disabled by Gradle with warning:
  `The following Kotlin/Native targets cannot be built on this machine and are disabled`.
  iOS is deferred until a Mac CI or developer machine is available.

## Data Architecture

**Pattern: Local-first with Supabase sync**

1. SQLDelight is the source of truth for the UI — all screens observe `Flow` from SQLDelight queries.
2. On ViewModel `init`, repositories call `sync()` to pull remote data into the local DB.
3. Writes go to Supabase first, then `sync()` is called to keep local DB consistent.
4. Auth is phone OTP via `supabase.auth.signInWith(OTP)` / `supabase.auth.verifyPhoneOtp()`.

**Credentials flow:** `local.properties` → `BuildConfig` (via `buildconfig` Gradle plugin) →
`NetworkModule` → Koin single → injected into repositories.

## File Index

| File | Contents | Load when… |
|------|----------|------------|
| `current-sprint.md` | What works, what's missing, next steps | Starting a session, planning next feature |
| `domain-model.md` | All entities with Kotlin types and field lists | Discussing schema, data structure, or entities |
| `schema.md` | SQLDelight `.sq` files — tables, queries, FK relationships | Writing SQL queries, designing migrations |
| `decisions.md` | Architectural decisions log (append-only) | Understanding why something was built a certain way |
| `open-questions.md` | Unresolved business logic questions | Starting a new feature, validating domain rules |
| `business-rules/` | Plot rules, sale rules, EMI rules, commission rules | Working on those specific features |

## Ground Rule

**Read `current-sprint.md` before writing any code.** It tracks which repositories and screens
actually exist vs. which are still TODO stubs.

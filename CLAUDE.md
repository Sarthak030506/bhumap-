# BhuMap — Land Developer Platform

## App Overview
BhuMap ("Apni Zameen, Apna Hisaab") is a mobile app for Indian land developers (Maharashtra focus). Single-login: only the developer/admin uses this app. Admin buys raw land from farmers, divides into plots, sells to customers on EMI or outright, manages partner investments.

> **v1 Scope: Admin only. No buyer login, no agent login.**

---

## Brand

| Token | Value |
|-------|-------|
| Name | BhuMap |
| Tagline | Apni Zameen, Apna Hisaab |
| Primary | Evergreen `#1F6F50` |
| Accent | Terracotta `#C8552B` |
| Base bg | Paper-50 `#FBF7F0` |
| Text | Soil-900 `#2A1F14` |
| Fonts | Plus Jakarta Sans + IBM Plex Sans Devanagari + JetBrains Mono |

---

## User Roles

| Role | Status | Description |
|------|--------|-------------|
| **Admin (Developer)** | Active | Single user. Full control: land acquisition, plots, sales, payments, partners, map |
| **Buyer** | Deferred (Phase 2) | Customer-facing portal — EMI schedule, payment history |
| **Agent** | Deferred (Phase 2) | Finds buyers, tracks leads, sees commission |

---

## Core Entities

| # | Entity | Status | Purpose |
|---|--------|--------|---------|
| 1 | **users** | Active | Admin user (single role for v1) |
| 2 | **lands** | Active | Raw land purchased from farmers (replaces "projects") |
| 3 | **farmers** | Active | Land sellers — name, phone, Aadhaar, payment tracking |
| 4 | **partners** | Active | Co-investors in a land — committed amount, paid, remaining |
| 5 | **plots** | Active | Numbered plots within a land; boundary polygon; status |
| 6 | **customers** | Active | Buyers — name, phone, linked to a plot sale |
| 7 | **sales** | Active | Plot sale: customer → plot, total deal, payment plan |
| 8 | **transactions** | Active | Individual payments (farmer / partner / customer); cash/UPI/cheque/transfer |
| 9 | **emi_schedule** | Active | Auto-generated installment rows per sale |
| 10 | **agents** | Deferred | Phase 2 |
| 11 | **leads** | Deferred | Phase 2 |

---

## 12 Screens (v1)

| # | Screen | Tab |
|---|--------|-----|
| 1 | Splash / Onboarding | — |
| 2 | Login (phone input) | — |
| 3 | OTP Verification | — |
| 4 | Dashboard | Dashboard |
| 5 | Land List | Land |
| 6 | Add Land (form) | Land |
| 7 | Land Detail (tabs: Overview / Partners / Plots) | Land |
| 8 | Partner Detail | Land |
| 9 | Map (full-screen, all plots as polygons) | Map |
| 10 | Plot Detail (bottom sheet) | Map |
| 11 | Customers List | Customers |
| 12 | Customer Detail (tabs: Plot & Sale / Payments / EMI Schedule) | Customers |
| + | Add Payment (reusable sheet: farmer / partner / customer) | — |

**Bottom tabs:** Dashboard · Land · Map · Customers

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| React Native + Expo | Cross-platform mobile (iOS + Android) |
| Expo Router | File-based navigation |
| Supabase Auth | Phone OTP only (+91). No email/password |
| Supabase PostgreSQL | Primary DB |
| Supabase RLS | Row-level security |
| Supabase Storage | 7/12 docs, sale agreements, payment receipts |
| Supabase Edge Functions | EMI generation, payment recording |
| Supabase Cron | EMI reminder notifications |
| Zustand | Session + active land state |
| React Query | Server state, caching |
| React Native Maps | Plot polygon overlay on Google Maps |
| Expo Notifications | Push for EMI reminders |

---

## Build Order (Phase 1 — Admin only)

1. **Supabase setup** — schema, enums, RLS, storage buckets, indexes
2. **Auth** — phone OTP login, session persist, splash → dashboard redirect
3. **Tokens + primitives** — tokens.ts, Button, Field, Card, StatusChip, Amount, Sheet, ListRow
4. **Dashboard** — KPI cards + recent activity feed
5. **Land** — list, add land form, land detail (overview + partners + plots tabs)
6. **Map** — full-screen map, plot polygons, plot detail bottom sheet
7. **Customers** — list, customer detail (sale / payments / EMI tabs)
8. **Add Payment sheet** — reusable across farmer / partner / customer
9. **EMI schedule** — auto-generate on sale create, mark paid
10. **Notifications** — cron-triggered EMI reminders

---

## Build Status

> Update at start and end of every coding session.

### Phase 1 — Admin only

| Area | Status | Notes |
|------|--------|-------|
| Supabase project setup | ⏳ Not started | |
| Schema + enums | ⏳ Not started | |
| RLS policies | ⏳ Not started | |
| Storage buckets | ⏳ Not started | |
| Auth — phone OTP login | ⏳ Not started | |
| Auth — session + redirect | ⏳ Not started | |
| Design tokens + primitives | ⏳ Not started | |
| Dashboard — KPI + activity | ⏳ Not started | |
| Land — list + add form | ⏳ Not started | |
| Land — detail tabs | ⏳ Not started | |
| Partner detail | ⏳ Not started | |
| Map — polygon overlay | ⏳ Not started | |
| Plot detail bottom sheet | ⏳ Not started | |
| Customers — list | ⏳ Not started | |
| Customer — detail tabs | ⏳ Not started | |
| Add Payment sheet | ⏳ Not started | |
| EMI — generate + mark paid | ⏳ Not started | |
| Notifications — cron + push | ⏳ Not started | |

### Phase 2 — Deferred

| Area | Status |
|------|--------|
| Buyer login + portal | ⏳ Not started |
| Agent screens + leads | ⏳ Not started |
| PDF reports / analytics | ⏳ Not started |

---

## Design Rules

- Money: Indian format `₹1,23,456` always (use `formatINR()`)
- Dates: `DD MMM YYYY` (e.g., `04 May 2026`)
- Sentence case everywhere. No emoji in UI chrome.
- Status: colored badge (never text-only)
- Loading: skeletons, not spinners
- Errors: inline below field, not popup
- Forms: scroll view + sticky save button at bottom
- FAB for primary create actions
- Cards: 12px radius, subtle shadow, 1px border
- Plot status colors: green=available, amber=reserved, orange=sold-pending, red=sold-paid, slate=blocked

## Ground Rules

- **Discuss business logic and data model before writing code.**
- Surface domain rule ambiguities as open questions first.
- RLS is the security boundary — no app-layer-only filtering.
- All monetary amounts: `numeric(15,2)` in INR.
- `uuid` primary keys everywhere.
- Edge Functions handle all computed writes (EMI rows, commissions).

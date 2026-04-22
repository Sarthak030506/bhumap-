# Land Developer Platform

## App Overview
A mobile platform for a land developer business. The admin buys raw land, divides it into numbered plots, sells plots to buyers either outright or on an EMI plan. Agents find buyers and earn commission on closed deals.

> **Current Focus:** Admin and Buyer roles. Agent role is defined but deferred.

---

## User Roles

| Role | Status | Description |
|------|--------|-------------|
| **Admin** | Active | Full control: creates projects, plots, sales, records payments, manages agents, generates reports |
| **Buyer** | Active | Sees only their own purchased plots and payment/EMI schedule |
| **Agent** | Deferred | Finds buyers for admin; sees available plots and their own leads only; sees own commission earned; cannot record payments |

> Partner role has been removed from scope.

---

## Core Entities (9)

| # | Entity | Status | Purpose |
|---|--------|--------|---------|
| 1 | **users** | Active | All 3 roles stored here with a `role` enum field |
| 2 | **projects** | Active | A land layout/project (e.g., "Green Valley Phase 1") |
| 3 | **plots** | Active | Individual numbered plots within a project; stores boundary polygon |
| 4 | **sales** | Active | A plot sale linking plot → buyer, with optional agent_id, commission fields |
| 5 | **transactions** | Active | Individual payment records against a sale (cash/transfer/cheque/UPI) |
| 6 | **emi_schedule** | Active | Auto-generated installment rows per sale (due date, amount, status) |
| 7 | **agents** | Deferred | Agent profile with commission_percent and running total_earned |
| 8 | **leads** | Deferred | Agent-submitted buyer prospects with full lifecycle tracking |

> `partners` entity removed from scope.

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| React Native + Expo | Cross-platform mobile app (iOS + Android) |
| Expo Router | File-based navigation; separate tab stacks per role |
| Supabase Auth | JWT-based auth; role stored in user metadata |
| Supabase PostgreSQL | Primary relational database |
| Supabase RLS | Row-level security — each role sees only their data |
| Supabase Storage | 7/12 documents, sale documents, plot images |
| Supabase Edge Functions | EMI generation, commission calc |
| Supabase Cron | Scheduled EMI reminder notifications |
| Zustand | Client-side state (session, current user, active project) |
| React Query | Server state, caching, background sync |
| React Native Maps | Plot polygon overlay rendering on Google Maps |
| Expo Notifications | Push notifications for EMI reminders and payment alerts |

---

## Build Order

**Phase 1 — Admin + Buyer (current focus)**
1. **Supabase setup** — schema, enums, RLS policies, storage buckets, indexes
2. **Auth** — login, role detection, session management, role-based redirect
3. **Projects** — create, list, view project detail
4. **Plots** — create with polygon boundary, status management, map view
5. **Sales** — create sale, link buyer, set payment plan
6. **EMI** — auto-generate schedule on sale creation, mark installments paid
7. **Buyers** — buyer-facing screens: my plots, my EMI schedule
8. **Reports** — admin financial overview, per-project P&L
9. **Notifications** — cron-triggered EMI reminders (7-day, day-of, overdue)

**Phase 2 — Agent (deferred)**
10. **Agents** — agent screens: available plots, leads, add lead, my earnings

---

## Build Status

> Update this section at the start and end of every coding session.

### Phase 1 — Admin + Buyer

| Area | Status | Notes |
|------|--------|-------|
| Supabase project setup | ⏳ Not started | |
| Schema + enums | ⏳ Not started | |
| RLS policies | ⏳ Not started | |
| Storage buckets | ⏳ Not started | |
| Auth — login screen | ⏳ Not started | |
| Auth — role redirect | ⏳ Not started | |
| Projects — list + create | ⏳ Not started | |
| Projects — detail view | ⏳ Not started | |
| Plots — create + map view | ⏳ Not started | |
| Sales — create flow | ⏳ Not started | |
| EMI — schedule generation | ⏳ Not started | |
| EMI — mark paid | ⏳ Not started | |
| Buyer — home + plot detail | ⏳ Not started | |
| Buyer — EMI schedule | ⏳ Not started | |
| Reports — admin overview | ⏳ Not started | |
| Notifications — cron + push | ⏳ Not started | |

### Phase 2 — Agent (deferred)

| Area | Status |
|------|--------|
| Agent screens | ⏳ Not started |
| Leads flow | ⏳ Not started |

---

## Ground Rules

- **Always discuss business logic and data model before writing any code.**
- When in doubt about a domain rule, surface it as an open question first.
- RLS is the security boundary — never rely solely on application-layer filtering.
- All monetary amounts stored as `numeric(15,2)` in INR.
- Use `uuid` primary keys everywhere.
- Edge Functions handle all computed writes (EMI rows, commission amounts, profit calc).

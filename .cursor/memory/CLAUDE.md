# Project Memory — Land Developer Platform

This directory contains structured memory for the land developer React Native app.
Load relevant files based on the topic at hand.

## Index

| File | Contents | Load when... |
|------|----------|--------------|
| `domain-model.md` | All entities with full field lists (partners removed; agents/leads deferred) | Discussing schema, data structure, or entities |
| `business-rules/plot-rules.md` | Plot status transitions, constraints, deletion rules | Working on plots or map |
| `business-rules/sale-rules.md` | Sale creation steps, cancellation, constraints | Working on sales flow |
| `business-rules/emi-rules.md` | EMI formula, due dates, payment recording, cron | Working on EMI or payments |
| `business-rules/commission-rules.md` | Commission calc, lifecycle, disputes | Working on agent commission |
| `business-rules/lead-rules.md` | Lead lifecycle, conversion, access (Phase 2) | Working on agent leads |
| `schema.md` | PostgreSQL tables with column definitions | Writing SQL, designing queries |
| `decisions.md` | Architectural decisions made (append-only log) | Understanding why something was built a certain way |
| `open-questions.md` | Unresolved business logic questions | Starting a new feature, validating domain rules |

## Quick Facts

- App: Land developer platform (plots, sales, EMI)
- Roles: Admin (active), Buyer (active), Agent (deferred Phase 2) — Partner removed
- Stack: React Native + Expo + Expo Router + Supabase + Zustand + React Query
- DB: PostgreSQL via Supabase with RLS on all tables
- Ground rule: **Discuss domain + data model before writing any code**

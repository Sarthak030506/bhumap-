# What changed: LandLedger → BhuMap

The design references in this folder were originally authored for a product called **LandLedger** with two roles (Admin + Buyer). The product is now called **BhuMap** and is a **single-login app for the developer/admin only**. This file captures the delta so you don't get confused.

## Use these references

✅ **Design tokens** — `colors_and_type.css`, `preview/*.html`, `assets/*`. Brand colors, type, spacing, radii, shadows, motion are all unchanged.

✅ **Content rules** — sentence case, Indian digit grouping, DD MMM YYYY, no emoji in chrome, Aadhaar/phone masking patterns, trust-confirm on money actions. All carry over.

✅ **Iconography** — Lucide, 1.75 stroke. Carry over.

✅ **Component primitives** — `ui_kits/mobile/LLPrimitives.jsx` (Button, TextField, StatusChip, Card, Sheet, AmountDisplay) — visual reference for how each component should look.

✅ **`formatINR` function** — port verbatim from `ui_kits/mobile/data.js`.

## Ignore these references

❌ **Two-role architecture.** BhuMap has ONE login. Drop everything related to:
- Buyer role / Buyer screens (`BuyerScreens.jsx`)
- Role switcher in the prototype
- `app/(buyer)/` route group
- Multiple tab trees

❌ **The original screen list** — `AdminScreens.jsx` shows screens (Projects → Project detail → Plot map → Plot detail → Record payment) that don't 1:1 match BhuMap's 12 screens. Use the new spec in `../README.md`.

❌ **Buyer-specific copy** — "Namaste, Rohan" greeting, buyer hero terracotta-50 card, buyer-facing EMI views. None of these exist in BhuMap.

❌ **The "Buyer" tab tree** in `index.html`. Only the admin half is conceptually relevant, and even that has a different screen list now.

## What's new in BhuMap (no reference exists yet — design from spec)

- Phone OTP login + onboarding splash with tagline "Apni Zameen, Apna Hisaab"
- Dashboard with 6 KPI cards + recent activity
- Land Acquisition flow (list + multi-section add form + 3-tab detail)
- Partners (sub-entity of Land — list + detail)
- Project Map (full-screen, all lands + plots overlaid, legend pill)
- Plot Detail bottom sheet with conditional CTAs by status
- Customers (list + multi-section add form + 3-tab detail with EMI schedule)
- Universal Add Payment screen used by farmer / partner / customer contexts

For these, build to the specs in `../README.md`. The design tokens + component primitives in this folder are how they should *feel*; the layouts are new.

## Mental model

> "Same brand, same components, new product structure."

If you find yourself looking at an old prototype and thinking "should I reproduce this screen?" — the answer is almost always no. Use the prototypes only to learn the visual vocabulary, then build the screens listed in the new README.

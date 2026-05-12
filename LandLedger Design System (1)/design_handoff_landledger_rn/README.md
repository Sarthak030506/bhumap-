# Handoff: BhuMap — React Native Mobile App

## Overview

**BhuMap** is a mobile management app for Indian land developers (Maharashtra focus). It replaces a multi-role concept with a **single-login product** for the developer/admin only. The developer manages land they buy from farmers, the partners who co-invest, the plots they create from that land, and the customers they sell those plots to.

> Tagline: *Apni Zameen, Apna Hisaab* — "Your Land, Your Account"

This handoff packages the full design system + the new BhuMap product spec and asks Claude Code to implement it as a real React Native app.

---

## About the design files in this bundle

`design_reference/` contains the **LandLedger** design-system source — colors, type, spacing, radii, shadows, content rules, iconography, and prototype HTML/JSX components. **The visual language carries over to BhuMap** (warm paper + evergreen + terracotta, Plus Jakarta Sans, Indian digit grouping, etc.).

What does NOT carry over from the original design references:
- ❌ The Buyer role and all buyer-facing screens
- ❌ The role switcher
- ❌ Multi-tab Admin/Buyer route trees

What DOES carry over:
- ✅ All design tokens (colors, type, spacing, radii, shadows, motion)
- ✅ Component primitives in `LLPrimitives.jsx` (Button, TextField, StatusChip, Card, Sheet, AmountDisplay)
- ✅ Content rules (sentence case, Indian digit grouping, DD MMM YYYY, no emoji in chrome)
- ✅ Iconography choice (Lucide, 1.75 stroke)

A delta document is at `design_reference/CHANGES.md` listing what to ignore in the original prototypes.

## Fidelity

**High fidelity for tokens & components, mid-fidelity for screens.** The screens listed below are NEW (LandLedger had a different set), so HTML mocks aren't 1:1 references. Use the design-system tokens + component primitives faithfully; lay out screens per the specs in this README.

---

## Brand recap (one screen scan)

| Token | Value | Role |
|---|---|---|
| `colors.evergreen[600]` | `#1F6F50` | Primary — buttons, links, "available" status |
| `colors.terracotta[500]` | `#C8552B` | Accent — FABs, hero highlights |
| `colors.soil[900]` | `#2A1F14` | Primary text, dark surfaces |
| `colors.paper[50]` | `#FBF7F0` | App background |
| `colors.status.available` | `#1F8A5B` | Plot available |
| `colors.status.reserved` | `#C68A18` | Reserved / EMI upcoming |
| `colors.status.sold` | `#B23A3A` | Sold-paid / EMI overdue |
| `colors.soil[500]` | `#6B553C` | Sold-pending intermediate state |
| `colors.status.hold` | `#4A5568` | Blocked plot |

Type: Plus Jakarta Sans (UI) · IBM Plex Sans Devanagari (Hindi/Marathi strings) · JetBrains Mono (amounts, EMI tables, receipt numbers).

Radii: cards 12, buttons 10, chips 6, plot status pills 999. Map polygons sharp / unrounded.

Spacing: 4px grid · default container & card padding 16.

---

## Target stack

- **React Native + Expo (managed, SDK 51+)**
- **TypeScript** (strict)
- **Expo Router** — single tree, no role groups: `app/(tabs)/...`
- **Zustand** — auth/session, selected land, sheet state
- **TanStack Query** — server state (mock API for v1)
- **`lucide-react-native`** — icons (1.75 stroke)
- **`react-native-svg`** — logo + contour pattern
- **`react-native-maps`** — land boundary + plot polygons (Google provider on Android, Apple on iOS)
- **`@gorhom/bottom-sheet`** — plot detail sheet, add-payment sheet
- **`react-native-reanimated`** + **`react-native-gesture-handler`** — animations + sheet gestures
- **`expo-document-picker`** + **`expo-image-picker`** — 7/12 / sale agreement / receipt uploads
- **`expo-font`** — Plus Jakarta Sans, IBM Plex Sans Devanagari, JetBrains Mono
- **`date-fns`** — DD MMM YYYY formatting + relative dues
- **`react-hook-form`** + **`zod`** — forms (Add Land, Add Customer, Add Payment)

Do NOT add a UI component library. Implement primitives directly.

---

## Auth / login model (NEW)

**Single role: developer/admin.** No buyer login, no agent login.

- Phone + OTP only (Indian +91 format).
- Use **Firebase Phone Auth** OR **Supabase Phone Auth** — pick one in Step 2 and stick with it. Either way, abstract behind `src/lib/auth.ts` so the wire-up is one file.
- Persist session via SecureStore (`expo-secure-store`).
- On every cold start: read SecureStore → if session valid, route to `/(tabs)/dashboard`; else route to `/login`.
- OTP screen: 6-digit input, auto-advance, paste-fills-all, 30s resend cooldown, "Change number" link.
- No email, no password, no social.

Stub the SMS provider in dev — log the OTP to console for testing. Document the flag in the README so the developer knows to flip it before staging.

---

## Project structure

```
bhumap/
├── app/                                # Expo Router
│   ├── _layout.tsx                     # Load fonts, mount providers
│   ├── index.tsx                       # Splash → routes to /onboarding or /(tabs)/dashboard
│   ├── onboarding.tsx                  # Logo + tagline + "Get started"
│   ├── login.tsx                       # Phone number + Send OTP
│   ├── login/otp.tsx                   # OTP verification (6 digits)
│   └── (tabs)/
│       ├── _layout.tsx                 # Bottom tab bar (4 tabs)
│       ├── dashboard.tsx               # Tab 1
│       ├── land/
│       │   ├── index.tsx               # Tab 2 — Land Acquisition list
│       │   ├── new.tsx                 # Add Land form
│       │   └── [id].tsx                # Land detail (3 inner tabs)
│       ├── map.tsx                     # Tab 3 — Project map
│       ├── customers/
│       │   ├── index.tsx               # Tab 4 — Customers list
│       │   ├── new.tsx                 # Add Customer form
│       │   └── [id].tsx                # Customer detail (3 inner tabs)
│       ├── partners/
│       │   └── [id].tsx                # Partner detail (linked from Land detail)
│       └── payments/
│           └── new.tsx                 # Generic Add Payment form
├── src/
│   ├── theme/
│   │   ├── tokens.ts                   # ← copy verbatim from tokens/tokens.ts
│   │   ├── typography.ts
│   │   └── shadows.ts
│   ├── components/
│   │   ├── Button.tsx
│   │   ├── FAB.tsx                     # 56px circular, terracotta-500
│   │   ├── TextField.tsx
│   │   ├── PhoneField.tsx              # +91 prefix, 10-digit
│   │   ├── OTPInput.tsx                # 6-cell auto-advance
│   │   ├── UnitField.tsx               # number + Guntha/Acre/Sqft selector
│   │   ├── DatePickerField.tsx         # DD MMM YYYY
│   │   ├── DocumentUploadField.tsx     # PDF / image, shows filename + remove
│   │   ├── StatusChip.tsx
│   │   ├── PlotPolygon.tsx             # for react-native-maps
│   │   ├── PlotTile.tsx                # grid tile
│   │   ├── AmountDisplay.tsx
│   │   ├── Card.tsx
│   │   ├── StatCard.tsx                # dashboard KPI
│   │   ├── ActivityRow.tsx             # recent activity feed
│   │   ├── EMIRow.tsx
│   │   ├── PartnerRow.tsx
│   │   ├── PaymentRow.tsx
│   │   ├── Sheet.tsx                   # @gorhom wrapper
│   │   ├── TopAppBar.tsx
│   │   ├── BottomTabBar.tsx
│   │   ├── EmptyState.tsx
│   │   ├── Skeleton.tsx                # shimmering placeholder
│   │   ├── Toast.tsx
│   │   ├── Logo.tsx
│   │   └── Icon.tsx
│   ├── lib/
│   │   ├── format.ts                   # formatINR (Indian grouping), formatArea, formatDateDDMMMYYYY
│   │   ├── auth.ts                     # Firebase OR Supabase phone OTP, sessions
│   │   ├── store.ts                    # Zustand
│   │   ├── mockApi.ts                  # in-memory API
│   │   └── validators.ts               # zod schemas (Aadhaar 12-digit, phone 10-digit, etc.)
│   └── data/
│       └── seed.ts                     # sample lands, partners, plots, customers, payments
├── assets/
│   ├── fonts/
│   └── images/                         # logo, contour pattern, illustrations
└── package.json
```

---

## Screens

### 1 · Splash / Onboarding
- **Layout:** Full paper-50 canvas. Contour pattern at 4% opacity. BhuMap logo mark centered (use the LandLedger logo as a placeholder — `design_reference/assets/logo-mark.svg`). Below logo: wordmark "BhuMap" (`fontSize.4xl`, evergreen-600, bold). Tagline below in `fontSize.md`, `colors.fgMuted`: "Apni Zameen, Apna Hisaab". Bottom 25%: primary button "Get started" → `/login`.
- **Behavior:** If session valid in SecureStore, skip to `/(tabs)/dashboard`.

### 2 · Login (phone) + 2a · OTP verify
**Login (`/login`)**
- Top: small logo + "Sign in to BhuMap" (h1).
- Helper: "We'll send a 6-digit code to your phone."
- `PhoneField`: locked `+91` prefix, 10-digit input, monospace, large.
- Disabled until 10 digits entered. Primary button "Send OTP" → submit → route to `/login/otp?phone=…`.
- Below: tiny caption "By continuing you agree to our terms and privacy policy." (no link UI for v1).

**OTP (`/login/otp`)**
- Title: "Enter the code we sent to +91 98765 43210" (mask: `+91 98XXX XX210` — show first 2 + last 3).
- `OTPInput` 6 cells, mono, auto-advance, paste-fills-all. Auto-submit on 6th digit.
- Below: "Didn't get it? Resend in 0:30" → countdown → tappable "Resend code".
- Tertiary: "Change number" → back to `/login`.
- Error state: `colors.status.sold` text under cells: "Wrong code. 2 attempts left."

### 3 · Dashboard (tab 1)
- **TopAppBar:** "Dashboard" (h1) on left, bell icon trailing.
- **KPI grid (2 columns):**
  1. Total land acquired — value in acres (large, mono), guntha sub-line
  2. Total plots — value, sub: "X sold · Y available"
  3. Money received — `₹` total, sub: from N customers
  4. Money pending — `₹` total, sub: across N customers, red if > 0
  5. Pending to farmers — `₹` total, sub: across N farmers
  6. Partner dues — `₹` total, sub: across N partners
- Each KPI is a `StatCard`: Card with label (overline), amount (`ll-amount`, mono), sub-line (caption).
- **Recent activity** — section header "Recent activity", then 5–10 `ActivityRow`s. Each: icon (lucide), one-line description, relative timestamp ("2h ago"), tap → deep link to relevant detail.
- Activity types: payment received, payment to farmer, plot sold, plot reserved, partner added, land added.

### 4 · Land Acquisition (tab 2)
- **TopAppBar:** "Land" (h1).
- List of land cards. Each card:
  - Top row: land name (h3) + status chip (`fully paid` / `partially paid`)
  - Sub: village, taluka — caption with map-pin icon
  - Owner row: avatar dot + farmer name + ` · ` + phone
  - Area: `12 Guntha · 0.30 Acre` (mono)
  - Payment bar: paid (filled) / total (track) + "₹4,50,000 of ₹12,00,000 paid" caption
- Tap card → `/(tabs)/land/[id]`.
- **FAB** (terracotta-500, bottom-right above tab bar): plus icon → `/(tabs)/land/new`.
- Empty state: contour pattern + illustration + "No land yet" + primary "Add your first land".

### 5 · Add Land (form)
Multi-section scroll view, sticky "Save land" button in safe area at bottom.

**Section 1 — Owner details**
- Owner name, owner phone (`PhoneField`), Aadhaar (12 digits, mono, masked `XXXX XXXX 1234`).
- Village, Taluka, District (autocomplete from a static MH list — load JSON in `src/data/mh-locations.json`).
- Survey number / Gat number (free text).

**Section 2 — Land details**
- Total area (`UnitField` — number + dropdown Guntha/Acre/Sqft).
- Agreed total price (`AmountField`, ₹ prefix, mono).
- Advance paid (`AmountField`, default 0).
- Acquisition date (`DatePickerField`).
- Registration status — toggle "Registered" / "Pending registration".

**Section 3 — Documents**
- 7/12 extract upload (PDF or image).
- Sale agreement upload (PDF or image).
- Each shows filename + size + remove button after upload. Skeleton while uploading.

**Section 4 — Map**
- Block-level button "Mark on map" (icon: `MapPin`). Opens map screen in draw mode (FUTURE — for v1 just stub: store nothing, show "Coming soon — map will accept polygon data here.")

**Validation (zod):**
- Owner phone: 10 digits.
- Aadhaar: 12 digits.
- Total area > 0. Agreed price > 0. Advance ≥ 0 and ≤ agreed price.

**On save:** mutation → toast "Land saved" → back to list.

### 6 · Land Detail (`/land/[id]`)
- **Top:** Map preview (read-only, 200px tall) showing land boundary polygon. Tap to open full Project Map zoomed to this land.
- Header: land name (h1), village/taluka caption.
- **Inner tabs (segmented control):** Overview · Partners · Plots

**Tab 1 — Overview**
- Owner info card (name, phone tap-to-call, Aadhaar masked).
- Land info card (area, agreed price, registered toggle, acquisition date).
- Payment summary card: paid bar + "₹4,50,000 of ₹12,00,000 to {owner}" + Add Payment button → opens `/payments/new?context=land&landId=…`.
- Payment history list (`PaymentRow` × N).

**Tab 2 — Partners**
- List of partners: name, ownership %, committed / paid / remaining.
- Add Partner button → sheet (name, phone, Aadhaar, ownership %, committed amount).
- Tap partner → `/partners/[id]`.

**Tab 3 — Plots**
- 3-column grid of `PlotTile`s. Color-coded by status:
  - 🟢 available · 🟡 reserved · 🟠 sold-pending · 🔴 sold-paid · ⬜ blocked
- Each tile: plot number, area (mono small), status dot.
- Add Plot button → opens map in draw mode (stub for v1: simple form with plot number + area + facing).
- Tap plot → opens Plot Detail sheet (same component as on the Map screen).

### 7 · Partner Detail (`/partners/[id]`)
- Card: partner info (name, phone, Aadhaar masked, ownership %).
- Investment summary card: committed / paid / remaining + progress bar.
- Payment history list. Add Payment button → `/payments/new?context=partner&partnerId=…`.

### 8 · Project Map (tab 3)
- **Full-screen map.** All land boundaries + plot polygons overlaid. Plot fill = status color at 35% opacity, stroke 1.5px at full color, sharp corners.
- **Floating legend** bottom-left: pill card with five rows (filled circle + label). Use `colors.status.*` directly.
- **FAB** bottom-right: "Draw new plot" → enters draw mode (cursor + tap-to-add-vertices). v1: stub the draw mode — show toast "Draw mode coming soon".
- **Tap polygon** → bottom sheet (Plot Detail Sheet, screen 9) slides up.

### 9 · Plot Detail Bottom Sheet
- Drag handle, paper-50 bg, radius-lg top corners, shadow-2.
- Header: plot number (h2) + status chip + facing direction caption (e.g. "East-facing").
- Area + base price metadata row (mono).
- **Conditional actions:**
  - **Available** → two buttons: "Reserve" (secondary) + "Sell" (primary).
  - **Reserved** → "Reserved by {customer}" subtitle + "Create sale" primary.
  - **Sold-pending / Sold-paid** → customer card (name + phone) + payment summary (paid/total/pending) + "Open customer" link → `/customers/[id]`.
  - **Blocked** → reason caption + "Unblock" ghost button.

### 10 · Customers (tab 4)
- List of customer cards. Each:
  - Customer name (h3) + phone caption
  - Plot purchased: "Plot A-14, Green Valley" (caption with plot icon)
  - Total deal: ₹18,00,000 (mono)
  - Paid bar + "₹4,50,000 paid · ₹13,50,000 pending" (red if pending > 0)
  - EMI status badge: "On track" / "1 EMI overdue" / "Fully paid"
- Tap card → `/customers/[id]`.
- FAB → `/customers/new`.
- Empty state same pattern as Land.

### 10a · Add Customer (form)
- Plot picker (dropdown — only available/reserved plots).
- Customer name, phone, Aadhaar.
- Sale price (auto-fills from plot base; editable).
- Registration charges (₹).
- Other charges (₹) + reason (free text).
- Down payment (₹).
- EMI plan toggle:
  - If on: count (1–60), frequency (Monthly/Quarterly), start date, interest rate (% — optional).
- On save: creates customer + sale + EMI schedule. Toast "Customer added".

### 11 · Customer Detail (`/customers/[id]`)
- Header: customer info card (name, phone tap-to-call, Aadhaar masked).
- **Inner tabs:** Plot & Sale · Payments · EMI

**Tab 1 — Plot & Sale**
- Plot info: plot number, area, location (tap → map).
- Sale breakdown: base price + registration charges + other charges = total deal value (mono table).

**Tab 2 — Payments**
- Payment summary card: paid / pending / next due date.
- Payment history list. Add Payment button → `/payments/new?context=customer&customerId=…`.

**Tab 3 — EMI Schedule**
- Header summary: "12 EMIs · 4 paid · 7 to go · 1 overdue".
- **Overdue EMIs pinned at top** (separate section with red header).
- Then upcoming EMIs in chronological order, then paid.
- Each `EMIRow`: number (mono), due date, amount (mono right-aligned), status chip (paid green / upcoming amber / overdue red).
- Overdue rows get a 1.5px red left edge — the documented exception to "no left-border accents".

### 12 · Add Payment (universal form)

**Reused for farmer / partner / customer payments via context query param.**

Form fields:
- Context banner at top: "Recording payment to Suresh Patil (farmer) for Land: Survey 142/3" — bold confirmation line.
- Amount (₹, mono, large).
- Date picker (default today, max today).
- Mode selector — `TweakRadio`-style segmented: Cash · Bank transfer · UPI · Cheque.
- **Conditional fields:**
  - Cheque → cheque number + bank name (both required).
  - UPI / Bank transfer → UTR number (12-digit, required).
  - Cash → no extra fields.
- Receipt photo upload (camera or gallery).
- Notes (multiline, optional, 200 char max).
- Sticky footer: confirmation re-summary + "Save payment" primary.

On save: mutation → toast "Payment recorded · Receipt #BM-2026-00418" with copy action → close sheet/screen.

---

## Bottom tab bar

Exactly 4 tabs, in this order:

1. **Dashboard** — `Home` icon
2. **Land** — `Mountain` (or `Trees`) icon
3. **Map** — `Map` icon
4. **Customers** — `Users` icon

Tab bar height **64px**, paper-50 bg, top hairline border `colors.line`, safe-area inset honored. Active tab: evergreen-600 icon + label. Inactive: soil-500.

---

## Interactions & motion

- Page transitions 220ms ease-out.
- Button press scale 0.98 over 90ms.
- Toasts: 180ms fade + 4px slide-up. Position above tab bar.
- Status crossfade on plot status change: 320ms.
- Bottom-sheet scrim `rgba(42,31,20,0.45)`. No frosted blur.
- **Skeletons, not spinners**, for any list / detail loading state. Use a shimmering rectangle component with `colors.bgSunken` base + `colors.paper[200]` shimmer.

---

## State management

```ts
interface AppState {
  session: { phone: string; userId: string; token: string } | null;
  selectedLandId: string | null;
  setSession(s: AppState['session']): Promise<void>; // also writes SecureStore
  signOut(): Promise<void>;
}
```

TanStack Query keys:
- `['dashboard']`
- `['lands']`, `['land', id]`
- `['partners', landId]`, `['partner', id]`
- `['plots', landId]`, `['plot', id]`
- `['customers']`, `['customer', id]`
- `['emi', customerId]`, `['payments', { context, refId }]`

Mock API in `src/lib/mockApi.ts` with 250–500ms simulated latency.

---

## Content rules (non-negotiable)

- **Indian digit grouping** for every rupee value. `₹1,50,000` not `₹150,000`. Implementation: copy `formatINR` from `design_reference/ui_kits/mobile/data.js`.
- **`₹` glyph (U+20B9)**, never "Rs." or "INR".
- **DD MMM YYYY** dates. Pair with relative for due dates: "in 3 days · 13 May 2026".
- **Sentence case** everywhere.
- **Areas**: Guntha for small parcels, Acre for large, sq.ft. for plots. Always show both when conversion adds clarity (e.g. "12 Guntha · 0.30 Acre").
- **Aadhaar masking**: always show as `XXXX XXXX 1234` in lists. Full value only in detail view, behind a "Show" toggle.
- **Phone masking** on OTP screen: `+91 98XXX XX210`.
- **Trust-confirm** on every payment: amount + counterparty + reference object in one sentence above Save.
- **No emoji** in chrome. Status is shape + color, always.

---

## Iconography

Lucide, stroke 1.75. Wrap in `src/components/Icon.tsx`.

Used: `Home`, `Mountain` (or `Trees`), `Map`, `Users`, `Bell`, `Plus`, `MapPin`, `Phone`, `Calendar`, `Upload`, `Camera`, `FileText`, `ChevronRight`, `ChevronLeft`, `MoreVertical`, `Copy`, `Check`, `X`, `AlertCircle`, `CheckCircle2`, `Clock`, `Lock`, `PauseCircle`, `Receipt`, `IndianRupee` (prefer `₹` text glyph).

---

## Design tokens

- `tokens/tokens.ts` — TypeScript, drop into `src/theme/tokens.ts` verbatim.
- `tokens/tokens.json` — raw JSON for build pipelines.

Highlights are in the "Brand recap" table at the top.

---

## Acceptance criteria

1. `npx expo start` runs the app on iOS sim + Android emulator without warnings.
2. Cold start with no session → splash → onboarding → login → OTP → dashboard. Cold start with valid session → splash → dashboard.
3. All 12 screens reachable per the navigation in this README.
4. Bottom tab bar has exactly 4 tabs in the order: Dashboard · Land · Map · Customers.
5. INR formatter passes unit tests for: `1500`, `150000`, `1500000`, `12500000` → `₹1,500`, `₹1,50,000`, `₹15,00,000`, `₹1,25,00,000`.
6. Plus Jakarta Sans, IBM Plex Sans Devanagari, JetBrains Mono all render. Verify the tagline "Apni Zameen, Apna Hisaab" on the splash uses Devanagari for the Hindi spelling if you choose Devanagari script (Latin transliteration is also acceptable — match what the developer prefers).
7. Add Payment is one screen reused by farmer / partner / customer contexts.
8. EMI Schedule pins overdue rows at the top with the red left-edge accent.
9. Phone OTP login: stub provider in dev mode, real provider in `EXPO_PUBLIC_AUTH_PROVIDER` flag.
10. Skeletons (not spinners) on every loading state.
11. No buyer login, no agent screens, no PDF reports, no analytics — these are Phase 2 and explicitly excluded.

---

## Out of scope (do not build)

- Buyer-facing portal / login
- Agent management
- PDF report generation
- Analytics screens / charts
- Real Google Maps polygon drawing tools (stub the FAB)
- Hindi full localization (only the tagline + "Namaste" greeting if used)
- Push notifications

---

## Files

- `README.md` — this file
- `CLAUDE_CODE_PROMPT.md` — the prompt to paste into Claude Code
- `tokens/tokens.ts` · `tokens/tokens.json` — design tokens
- `design_reference/CHANGES.md` — what changed vs the original LandLedger references
- `design_reference/README.md` — design system rationale (read for brand voice)
- `design_reference/colors_and_type.css` — token ground truth
- `design_reference/ui_kits/mobile/LLPrimitives.jsx` — component reference (Button, TextField, StatusChip, Card, Sheet, AmountDisplay)
- `design_reference/ui_kits/mobile/data.js` — `formatINR` to port
- `design_reference/assets/` — logos, contour pattern, illustrations
- `design_reference/preview/*.html` — per-token preview cards

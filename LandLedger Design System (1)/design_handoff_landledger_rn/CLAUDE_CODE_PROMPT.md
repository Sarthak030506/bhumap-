# Prompt to paste into Claude Code

Copy everything between the `---` markers below into your Claude Code terminal session, after `cd`'ing into this folder (`design_handoff_landledger_rn/`) so Claude Code has filesystem access to it.

---

I'm handing you a complete design-system handoff for a React Native app called **BhuMap** — a mobile management app for Indian land developers (Maharashtra focus). It's a **single-login app** for one role only: the developer/admin. No buyer login, no agent login.

Tagline: *Apni Zameen, Apna Hisaab* — "Your Land, Your Account".

**Your job:** build a real, runnable React Native app from the materials in this folder.

## Step 1 — Read the brief (do not skim)

Read these files in order:

1. `./README.md` — the full BhuMap implementation brief. Target stack, project structure, all 12 screens with detailed layouts, auth model, content rules, acceptance criteria.
2. `./design_reference/CHANGES.md` — what changed from the original LandLedger references. Read this BEFORE looking at any prototype file so you don't get confused.
3. `./design_reference/README.md` — the design-system rationale. Brand voice, palette philosophy, content rules.
4. `./design_reference/colors_and_type.css` — ground truth for every token.
5. `./tokens/tokens.ts` — TypeScript tokens. Copy verbatim into `src/theme/tokens.ts`.
6. `./design_reference/ui_kits/mobile/LLPrimitives.jsx` — component visual reference (Button, TextField, StatusChip, Card, Sheet, AmountDisplay).
7. `./design_reference/ui_kits/mobile/data.js` — port `formatINR` verbatim.

After reading, summarize back to me in 6 bullets:
1. The product (one sentence).
2. The single-role auth model.
3. The 4 tabs in order.
4. The 12 screens grouped by tab.
5. The non-negotiable content rules.
6. Anything in `CHANGES.md` you'll deliberately ignore from the prototypes.

Wait for me to confirm before writing any code.

## Step 2 — Scaffold

Once I confirm:

1. `npx create-expo-app@latest bhumap -t expo-template-blank-typescript`
2. Add Expo Router + every dependency listed in the README "Target stack" section.
3. **Choose Firebase OR Supabase** for phone OTP — tell me which, and why, before installing.
4. Build the folder structure exactly per the README's "Project structure" tree.
5. Drop `tokens/tokens.ts` into `src/theme/tokens.ts`.
6. Wire `expo-font` for Plus Jakarta Sans, IBM Plex Sans Devanagari, JetBrains Mono. Fetch font files into `assets/fonts/`.

Stop. Show me `npx expo start` running on iOS sim with the default screen + the file tree. Wait for confirmation.

## Step 3 — Theme + primitives

Build, screenshot after each:

1. `src/theme/typography.ts`, `src/theme/shadows.ts`.
2. `src/components/Icon.tsx` (lucide wrapper, stroke 1.75 locked).
3. `src/components/Logo.tsx` (BhuMap wordmark — use the SVG mark from `design_reference/assets/logo-mark.svg` as the symbol, render "BhuMap" wordmark in evergreen-600 Plus Jakarta Sans bold).
4. `src/components/Button.tsx`, `FAB.tsx` — primary scale-press animation (0.98 over 90ms).
5. `src/components/TextField.tsx`, `PhoneField.tsx`, `OTPInput.tsx`, `UnitField.tsx`, `DatePickerField.tsx`, `DocumentUploadField.tsx`.
6. `src/components/StatusChip.tsx`, `AmountDisplay.tsx`, `Card.tsx`, `StatCard.tsx`, `EMIRow.tsx`, `PartnerRow.tsx`, `PaymentRow.tsx`, `ActivityRow.tsx`, `PlotTile.tsx`.
7. `src/components/TopAppBar.tsx`, `BottomTabBar.tsx`, `Sheet.tsx`, `EmptyState.tsx`, `Skeleton.tsx`, `Toast.tsx`.

## Step 4 — Auth + navigation skeleton

1. `src/lib/auth.ts` — phone OTP wrapper. Dev mode: log OTP to console. Prod mode: real provider behind `EXPO_PUBLIC_AUTH_PROVIDER` flag.
2. `src/lib/store.ts` — Zustand session store with SecureStore persistence.
3. `app/index.tsx` — splash that decides onboarding vs dashboard.
4. `app/onboarding.tsx`, `app/login.tsx`, `app/login/otp.tsx` — full auth flow.
5. `app/(tabs)/_layout.tsx` — 4-tab bottom bar (Dashboard · Land · Map · Customers).

Screenshot the cold-start flow end-to-end. Wait for confirmation.

## Step 5 — Build screens in this order

Screenshot after each pair:

1. Dashboard (with mock data → 6 KPIs + activity feed).
2. Land list + Add Land form.
3. Land detail (3 inner tabs: Overview / Partners / Plots).
4. Partner detail.
5. Project Map (use mock polygon data; FAB stub OK).
6. Plot Detail bottom sheet with all 5 status variants.
7. Customers list + Add Customer form.
8. Customer detail (3 inner tabs: Plot & Sale / Payments / EMI).
9. Universal Add Payment screen — verify all 3 contexts (farmer/partner/customer).

## Step 6 — Acceptance pass

Run the README acceptance criteria checklist. Add the `formatINR` unit test (`1500 / 150000 / 1500000 / 12500000` → `₹1,500 / ₹1,50,000 / ₹15,00,000 / ₹1,25,00,000`). Verify no buyer/agent/report/analytics code exists.

## Rules

- **Do not invent colors.** Ask if something seems to need a new one.
- **Do not add a UI component library.** Implement primitives directly.
- **No emoji in chrome.** Status = shape + color.
- **Indian digit grouping** for every rupee value.
- **DD MMM YYYY** for every date.
- **Sentence case** for every label, button, title.
- **Aadhaar masking** in lists; full value behind a Show toggle in detail.
- **Skeletons, not spinners**, for loading states.
- **Mobile first at 390×844.** Test on iPhone 14 sim and Pixel 6 emulator.
- **Phase 2 is excluded** — do not build buyer login, agent screens, PDF reports, or analytics. If you think you need them to make something work, ask first.
- Ask before deviating from the spec or adding new dependencies.

When you're ready, start with **Step 1**.

---

## Tips

- Run `cd design_handoff_landledger_rn && claude` so Claude Code has direct read access.
- Or, if Claude Code is already running: `/add-dir /absolute/path/to/design_handoff_landledger_rn`.
- The Step 1 read list is the part you should NEVER cut. Skipping `CHANGES.md` will cause confusion since the prototypes show a different product.
- If Claude Code asks to skip steps to go faster, say no. The acceptance criteria depend on the full sequence.

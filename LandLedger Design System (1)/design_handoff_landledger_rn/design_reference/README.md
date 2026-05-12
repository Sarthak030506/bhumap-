# LandLedger Design System

LandLedger is a mobile platform for small-to-mid Indian land developers — the business that buys raw land, divides it into numbered plots, and sells them to buyers either outright or on EMI. This design system defines the brand, visual foundations, and component vocabulary for that app.

The product has two roles in scope:

- **Admin** — power user. Creates projects, draws plot boundaries on a map, records sales, sets up EMI schedules, logs payments, generates reports.
- **Buyer** — consumer. Sees only their own plots, their EMI schedule, upcoming dues, and payment history.

The brand must do two jobs at once: feel **professional enough to be trusted with lakhs of rupees**, and feel **approachable enough for a first-time plot buyer in Tier-2/3 India**. Admin tooling leans dense and utilitarian; buyer surfaces lean calm and reassuring. Both share the same tokens.

---

## Source material

This design system was built against:

- **GitHub:** `Sarthak030506/bhumap-` — `CLAUDE.md` (build plan, entities, tech stack) and `workspace.md` (domain skills scaffolding). The repo is **pre-code**: no screens, no components, no colors defined. All visual decisions in this system were authored from the product brief, not extracted from existing UI.
- **Brief:** "Professional but approachable. Indian real estate market. Mobile-first. Trust-building design — buyers hand over large sums. Clean data-heavy screens (EMI tables, payment history). Status colors critical: green = available, red = sold, amber = reserved. INR currency format (₹1,50,000). Admin power-user; buyer needs simplicity."

Because there's no existing product to mirror, this system is **opinionated, not reverse-engineered**. Treat every choice as a v1 proposal open to edits.

---

## Index

Root files:
- `README.md` — this file (context, content fundamentals, visual foundations, iconography)
- `colors_and_type.css` — all color + type + spacing + radius + shadow tokens
- `SKILL.md` — agent-skill manifest for Claude Code / design agent use
- `fonts/` — web font files (Plus Jakarta Sans for UI, IBM Plex Sans Devanagari for Hindi, JetBrains Mono for numerics)
- `assets/` — logos, status icons, illustrations
- `preview/` — registered cards that populate the Design System tab (colors, type, spacing, components)
- `ui_kits/mobile/` — React Native-flavored mobile UI kit: Admin and Buyer screens, components, interactive click-through

---

## Content fundamentals

**Voice.** Clear, specific, respectful. We're handling someone's life savings — never chirpy, never jargon-heavy. Confident without swagger. A senior bank officer who also runs the village panchayat meeting: formal enough to trust, warm enough to ask a second question.

**Pronouns.** Address the user as **you**. Refer to the admin's own business as **your project / your plots**. Buyer-facing copy refers to the developer company by name, never as "we" abstractly — "Green Valley Developers" not "we". This keeps accountability concrete.

**Casing.** Sentence case everywhere. Buttons: "Record payment", not "Record Payment" or "RECORD PAYMENT". Section titles: "Upcoming installments". Proper nouns stay capitalised (plot numbers, project names, city names).

**Numbers.** This is non-negotiable: **Indian digit grouping** — `₹1,50,000` not `₹150,000`. Use the `₹` symbol, never "Rs." or "INR" in UI chrome (INR only appears in exported documents). Lakh/crore words are OK in copy ("₹12.5 Lakh") but the numeric token itself always uses digit grouping. Area uses sq.ft. for plots < 1 acre, **guntha / acre** for larger holdings (common in Maharashtra/Karnataka land deals).

**Dates.** DD MMM YYYY. "04 May 2026", not "5/4/26". Due-date badges show relative ("in 3 days", "overdue 12 days") alongside the absolute date.

**Tone examples.**

| Don't | Do |
|---|---|
| "Oops! Payment failed 😬" | "Payment couldn't be recorded. Check the amount and try again." |
| "Awesome! Plot sold 🎉" | "Plot A-14 marked as sold to Rohan Patil." |
| "Your EMI is due!" | "₹12,500 due on 10 May · Plot C-07" |
| "Hey buyer, welcome back!" | "Namaste, Rohan" (on-brand greeting, used once, at home) |
| "Click here to pay" | "Pay ₹12,500" (amount on the button) |

**Emoji.** No. Status is conveyed by color, shape, and iconography — not emoji. The one exception is a single 🙏 or 🌱 permitted in welcome-only illustrations, never in UI chrome.

**Hindi / regional.** English is primary. A single Hindi/Marathi greeting ("Namaste") is allowed as a warmth signal on home screens. Full Hindi localization is a future concern; this system includes a Devanagari type pairing ready for it.

**Trust signals in copy.** Every money-moving action states the amount, the party, and the plot in one line before confirmation. Receipt numbers are always shown and copyable. "This cannot be undone" warnings appear before destructive admin actions (canceling a sale, deleting a plot).

---

## Visual foundations

**Palette philosophy.** Grounded in land itself. A deep **soil brown** and **evergreen trust green** as anchors; warm **terracotta** for attention; **document off-white** as the dominant canvas. Status colors (green / amber / red) are saturated and high-contrast because an admin may scan 40 plots on a site in sunlight. Admin chrome is denser and leans toward neutrals; buyer chrome gets more of the warm paper + green accents.

**Primary brand colors.**

| Token | Hex | Role |
|---|---|---|
| `--ll-evergreen-600` | `#1F6F50` | Primary brand, confirming actions, "available" status |
| `--ll-soil-900` | `#2A1F14` | Deepest text / dark surfaces |
| `--ll-terracotta-500` | `#C8552B` | Accent, highlights, buyer hero |
| `--ll-paper-50` | `#FBF7F0` | App background (warm off-white) |

**Status colors.** Green `#1F8A5B` (available), Amber `#C68A18` (reserved / due soon), Red `#B23A3A` (sold / overdue), Slate `#4A5568` (on hold). These are used on plot maps, EMI tables, and status chips — always with a matching iconographic shape for color-blind accessibility.

**Type.** 
- **Plus Jakarta Sans** — UI typeface. Humanist geometric; works well in both Latin and data-dense tables. Weights 400 / 500 / 600 / 700.
- **IBM Plex Sans Devanagari** — matched Devanagari companion for Hindi strings.
- **JetBrains Mono** — numeric column alignment in EMI tables and transaction ledgers. Used tabularly, never for body.

All font files are local in `fonts/`. `Plus Jakarta Sans` is the Google-Fonts substitute that was selected up front — flag: if the brand commits to a custom display face later, swap here.

**Spacing.** 4px base grid. Tokens: 4, 8, 12, 16, 20, 24, 32, 40, 56, 72. Mobile container padding is 16; card internal padding is 16; list-row vertical padding is 12. Density mode for admin tables drops row padding to 8.

**Radii.** Cards and surfaces `12px`. Buttons `10px`. Chips / badges `6px` (pill radius only for status badges on plot tiles — they get `999px`). Map-plot polygons: sharp, unrounded — they represent real legal boundaries.

**Elevation.** Two-layer shadow system:
- `--ll-shadow-1` subtle card lift: `0 1px 2px rgba(42,31,20,.06), 0 1px 1px rgba(42,31,20,.04)`
- `--ll-shadow-2` floating surface (sheets, menus): `0 8px 24px -8px rgba(42,31,20,.18), 0 2px 4px rgba(42,31,20,.06)`
Shadows tint toward `soil-900` rather than pure black — keeps the warm paper feel.

**Borders.** `1px solid --ll-line` (#E9E1D3 — warm line on paper) for dividers and card outlines. On dark surfaces, borders become `rgba(255,255,255,.08)`. Table rules: single 1px line at 60% opacity, never bold rules.

**Backgrounds.** Default is warm paper (`--ll-paper-50`). A very subtle repeating land-contour SVG pattern (see `assets/pattern-contour.svg`) may be used at 4% opacity on empty states and splash screens to hint at surveying/land without being literal. Hero gradients are forbidden — they cheapen a trust-first brand. Full-bleed photography is reserved for marketing contexts, never inside the app.

**Animation.** Restrained.
- Page transitions: 220ms ease-out.
- Button press: 90ms scale to 0.98, instant return.
- Toasts/snackbars: 180ms fade + 4px slide-up.
- Status changes on a plot (pending → sold): 320ms color crossfade, no bounce.
- No parallax, no page-wide animations, no confetti. Buyers do not want novelty on a screen that shows ₹18L of debt.

**Hover / press states.**
- Hover (tablet / web fallback): background darkens by ~6% (use `color-mix` against surface).
- Press: scale 0.98 + 8% darken.
- Disabled: 40% opacity, no background, no shadow, cursor not-allowed.
- Focus (keyboard): 2px `--ll-evergreen-600` outline at 2px offset — never removed.

**Transparency & blur.** Used sparingly. Bottom sheet scrim: `rgba(42,31,20,0.45)`. No frosted-glass nav bars (visually noisy on map overlays). Map-plot labels use a 90% opaque paper chip rather than a blur — legible in sun.

**Cards.** Warm paper background, 1px warm line, `12px` radius, shadow-1, internal padding 16. No colored left-border accent stripes (overused fintech trope — avoided). Status is conveyed via a top-right badge or a numeric hero, not a sidebar stripe.

**Layout rules.**
- Mobile first — designs authored at 390×844.
- Fixed bottom tab bar (64px) on both Admin and Buyer.
- Sticky page header (56px) with back nav + action.
- Safe-area inset respected on both iOS and Android.
- Primary action always in the bottom 25% of the viewport (thumb zone). Secondary in the header.

**Imagery.** Warm tone, natural light, non-stock. Prefer site/earth/survey imagery over perfect renders. B&W for document previews (7/12 extracts). Avoid: drone-shot real-estate billboard aesthetics, Western suburban lawns, overly glossy renders.

---

## Iconography

**System:** Lucide icons (outline, 1.75px stroke, 24px grid). Chosen because (a) it's MIT-licensed, (b) it has a neutral, documentary feel matching land-records aesthetic, and (c) wide platform coverage for React Native via `lucide-react-native`.

**Flag — substitution:** The brief references no existing icon set, so **Lucide is this system's chosen default**. If the brand later adopts a custom icon family, swap by changing `ui_kits/mobile/Icon.jsx`. Lucide is linked via CDN in preview HTML; production should use the `lucide-react-native` package.

**Status icons** (color-blind safe): available = filled circle + check; reserved = filled circle + clock; sold = filled circle + lock; on-hold = filled circle + pause. Shape + color, always. See `preview/status-icons.html`.

**Logo:** `assets/logo-wordmark.svg`, `assets/logo-mark.svg`. The mark is a stylized plot boundary — four corners with one marked — evoking a surveyed parcel. Evergreen on paper; white on dark.

**Imagery & illustration:** `assets/pattern-contour.svg` (subtle topo pattern for empty states), `assets/illustration-no-sales.svg` (empty-state for reports), `assets/illustration-welcome.svg` (buyer home greeting).

**Emoji / unicode:** Avoided in chrome. Unicode `•` (bullet) and `·` (middle dot) are used in compact metadata rows ("Plot A-14 · 2,400 sq.ft · ₹18,00,000"). Rupee glyph `₹` (U+20B9) used everywhere for currency.

---

## UI kits

- `ui_kits/mobile/` — the mobile app. Includes a shared component layer (buttons, fields, status chips, EMI row, plot card, stat card, bottom tab bar, top app bar, sheet) plus interactive flows for both **Admin** (project list → plot map → sale flow → EMI table) and **Buyer** (home → my plots → EMI schedule → payment history). Open `ui_kits/mobile/index.html` for the click-through.

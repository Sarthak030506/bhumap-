# LandLedger Mobile UI Kit

Recreated UI components for the LandLedger mobile app. Covers both **Admin** and **Buyer** role shells as a click-through prototype inside an iOS device frame.

- `index.html` — interactive prototype. Role switcher in the status bar flips between Admin and Buyer. Admin flows: projects → plot map → plot detail → record payment. Buyer flows: home → my plot → EMI schedule → payment history.
- `ios-frame.jsx` — device bezel (starter component).
- `LLPrimitives.jsx` — shared tokens-as-JS, Icon, Button, Field, StatusChip, AmountDisplay, Card, Sheet.
- `LLAppShell.jsx` — top app bar + bottom tab bar.
- `AdminScreens.jsx` — Admin home, Project detail, Plot map, Plot detail, Record payment sheet.
- `BuyerScreens.jsx` — Buyer home, My plot, EMI schedule, Payment history.
- `data.js` — fake seed data (projects, plots, sales, EMI rows, transactions).

Components are cosmetic recreations — no Supabase, no real maps. Designed at 390×844 iPhone frame, scales to fit viewport.

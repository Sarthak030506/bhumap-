---
name: ui-patterns
description: Consistent React Native UI decisions for the land developer app — component library, navigation patterns, map behavior, empty states, error handling
trigger_phrases: component library, NativeWind, Tamagui, StyleSheet, bottom sheet, modal, empty state, error toast, loading state, UI pattern, screen transition, map tap, gesture handler
---

# UI Patterns — Land Developer App

## Component Library Decision
**Choice: Plain StyleSheet + custom components**
- No NativeWind (Tailwind for RN) — adds build complexity
- No Tamagui — overkill for this app size
- No NativeBase / RN Paper — too opinionated, hard to match custom design
- Use: `StyleSheet.create()` with a central `theme.ts` (colors, spacing, typography)

## Theme File (`src/theme.ts`)
```ts
colors: {
  primary: '#1A3C5E',       // deep navy — trust, professional
  accent: '#E8A020',        // amber — CTAs, highlights
  success: '#22C55E',       // available plots, paid EMIs
  warning: '#F59E0B',       // reserved plots, due-today EMIs
  danger: '#EF4444',        // sold plots, overdue EMIs
  surface: '#F8FAFC',       // card backgrounds
  border: '#E2E8F0',
  text: { primary, secondary, disabled }
}
spacing: 4px base unit (4, 8, 12, 16, 24, 32, 48)
radius: { sm: 6, md: 12, lg: 20, full: 9999 }
```

## Navigation Patterns

| Scenario | Pattern | Reason |
|----------|---------|--------|
| List → detail (plots, sales) | Push screen (Expo Router `router.push`) | Back nav feels natural |
| Quick info (plot tap on map) | Bottom sheet (80% height) | Stay in map context |
| Form flows (create sale) | Stack of screens with progress indicator | Multi-step clarity |
| Confirmations (delete, cancel sale) | Modal alert | Destructive actions need friction |
| Filters / sort | Bottom sheet | Lightweight, dismissable |

## Map Tap Behavior
- Tap plot polygon → open bottom sheet (not navigate away)
- Bottom sheet shows: plot number, area, price, status badge
- Admin bottom sheet CTAs: "View Detail" | "Create Sale" (if available)
- Buyer bottom sheet: read-only info only
- Long press on polygon: no special behavior in v1

## Bottom Sheet Rules
- Use `@gorhom/bottom-sheet` library
- Always use `snapPoints={['50%', '85%']}` — two snap positions
- Dismiss on backdrop tap = true
- Never nest a ScrollView inside a bottom sheet that itself scrolls

## Empty State Patterns
```
[Icon — 48px, color: text.disabled]
[Primary message — 16px semibold]
[Secondary message — 14px text.secondary]
[CTA button — if admin and can create]
```
Examples:
- No plots: "No plots yet" / "Add the first plot to this project" / [+ Add Plot]
- No sales: "No sales recorded" / "Create a sale to get started" / [+ New Sale]
- Buyer no plots: "No plots purchased yet" / "Contact admin for assistance"

## Loading States
- List screens: use skeleton cards (3–4 placeholder rows) — NOT spinners
- Detail screens: show header skeleton + content shimmer
- Mutations (save, record payment): disable button + show activity indicator inside button
- Never show a full-screen spinner for data that was previously cached

## Error Handling
- Network errors: inline banner at top of screen (red, dismissable)
- Validation errors: inline below each field (red text, 12px)
- Mutation failures: bottom toast (3 second auto-dismiss)
- Critical failures (auth lost, DB unreachable): full-screen error with retry button

## Toast / Notification Pattern
- Library: `react-native-toast-message`
- Success: green, bottom, 2.5s
- Error: red, bottom, 4s
- Info: blue, bottom, 3s
- Never stack more than one toast

## Form Patterns
- All forms: `react-hook-form` + `zod` for validation
- Submit button: always at bottom, full width
- Required fields: no asterisk — just validate on submit with inline message
- Currency inputs: numeric keyboard, formatted display (e.g., ₹1,50,000)
- Date pickers: use `@react-native-community/datetimepicker`

## Status Badge Component
```tsx
<StatusBadge status="available" />  // green
<StatusBadge status="reserved" />   // amber
<StatusBadge status="sold" />       // red
<StatusBadge status="pending" />    // gray
<StatusBadge status="overdue" />    // red with warning icon
<StatusBadge status="paid" />       // green with check icon
```
Pill shape, 12px text, 4px vertical padding, 10px horizontal padding.

## Card Pattern
- Elevation: `shadowColor: #000, shadowOffset: {0,2}, shadowOpacity: 0.06, elevation: 2`
- Border: none (use shadow only)
- Border radius: `radius.md` (12px)
- Padding: 16px
- Tap: `TouchableOpacity` with `activeOpacity={0.85}`

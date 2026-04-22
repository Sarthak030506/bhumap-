# Role Access Matrix

Legend: ✅ Allowed | ❌ Blocked | 👁 Read-only | ⚠️ Partial/Conditional

---

## Entity Access by Role

### Projects

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| Create project | ✅ | ❌ | ❌ | ❌ |
| View all projects | ✅ | ❌ | ❌ | ❌ |
| View own projects | — | 👁 (bought plot) | 👁 (co-invested) | 👁 (active only) |
| Edit project | ✅ | ❌ | ❌ | ❌ |
| Delete/cancel project | ✅ | ❌ | ❌ | ❌ |

### Plots

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| Create plot | ✅ | ❌ | ❌ | ❌ |
| View all plots in project | ✅ | ❌ | 👁 (own projects) | ❌ |
| View available plots | ✅ | ❌ | 👁 | ✅ (only available) |
| View own plot | — | 👁 | — | — |
| Edit plot (boundary, price) | ✅ | ❌ | ❌ | ❌ |
| Change plot status manually | ✅ | ❌ | ❌ | ❌ |

### Sales

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| Create sale | ✅ | ❌ | ❌ | ❌ |
| View all sales | ✅ | ❌ | ❌ | ❌ |
| View own sale details | — | 👁 (no commission data) | ❌ | ⚠️ (commission fields only, no sale_price) |
| Edit sale | ✅ | ❌ | ❌ | ❌ |
| Cancel sale | ✅ | ❌ | ❌ | ❌ |
| View sale_price | ✅ | 👁 (own) | ❌ | ❌ |

### Transactions

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| Record transaction | ✅ | ❌ | ❌ | ❌ |
| View all transactions | ✅ | ❌ | ❌ | ❌ |
| View own sale transactions | — | 👁 | ❌ | ❌ |
| Edit/delete transaction | ❌ (append-only) | ❌ | ❌ | ❌ |

### EMI Schedule

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| View full EMI schedule | ✅ | ❌ | ❌ | ❌ |
| View own EMI schedule | — | 👁 | ❌ | ❌ |
| Mark EMI paid | ✅ | ❌ | ❌ | ❌ |
| Generate EMI (Edge Fn) | ✅ | ❌ | ❌ | ❌ |

### Partners

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| Add partner to project | ✅ | ❌ | ❌ | ❌ |
| View all partners | ✅ | ❌ | ❌ | ❌ |
| View own partner records | — | ❌ | 👁 (own % and profit share) | ❌ |
| Edit partner percentage | ✅ | ❌ | ❌ | ❌ |
| See other partners' % | ✅ | ❌ | ❌ | ❌ |

### Agents

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| Create agent profile | ✅ | ❌ | ❌ | ❌ |
| View all agents | ✅ | ❌ | ❌ | ❌ |
| View own agent profile | — | ❌ | ❌ | 👁 (commission_percent, total_earned) |
| Edit commission_percent | ✅ | ❌ | ❌ | ❌ |
| Mark commission paid | ✅ | ❌ | ❌ | ❌ |

### Leads

| Action | Admin | Buyer | Partner | Agent |
|--------|-------|-------|---------|-------|
| View all leads | ✅ | ❌ | ❌ | ❌ |
| View own leads | — | ❌ | ❌ | 👁 |
| Create lead | ✅ | ❌ | ❌ | ✅ (own agent_id only) |
| Update lead status | ✅ | ❌ | ❌ | ✅ (own leads only) |
| See other agents' leads | ✅ | ❌ | ❌ | ❌ |
| Convert lead to sale | ✅ (creates sale) | ❌ | ❌ | ❌ |

---

## Agent-Specific Rules (Critical)

| Data | Agent Access |
|------|-------------|
| Available plots | ✅ See — used to pitch buyers |
| Plot boundary/location | ✅ See — needed for site visits |
| Sale price (amount) | ❌ Never |
| Buyer financial details | ❌ Never |
| Partner info | ❌ Never |
| Other agents' leads | ❌ Never |
| Transaction records | ❌ Never |
| EMI schedules | ❌ Never |
| Own commission amount | ✅ See — from agent_sales_view |
| Own commission paid status | ✅ See |
| Own total_earned | ✅ See — from agents table |

---

## Navigation Guards (UX Layer — in addition to RLS)

Expo Router route groups should prevent wrong-role screen access:
- `/(admin)/` — redirect non-admins to their role group
- `/(buyer)/` — redirect non-buyers
- `/(partner)/` — redirect non-partners
- `/(agent)/` — redirect non-agents

Route guard logic in root `_layout.tsx`:
```tsx
const { role } = useSession();
if (role === 'admin') return <Redirect href="/(admin)/" />;
if (role === 'buyer') return <Redirect href="/(buyer)/" />;
if (role === 'partner') return <Redirect href="/(partner)/" />;
if (role === 'agent') return <Redirect href="/(agent)/" />;
```

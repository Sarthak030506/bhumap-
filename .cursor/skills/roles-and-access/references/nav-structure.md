# Expo Router Navigation Structure

---

## Full File Structure

```
app/
├── _layout.tsx                          ← Root layout: auth gate + role-based redirect
├── +not-found.tsx
│
├── (auth)/
│   ├── _layout.tsx                      ← Unauthenticated stack
│   └── login.tsx                        ← Login screen (email/password)
│
├── (admin)/
│   ├── _layout.tsx                      ← Admin tab bar
│   ├── index.tsx                        ← Dashboard (summary stats)
│   ├── projects/
│   │   ├── index.tsx                    ← Project list
│   │   ├── new.tsx                      ← Create project
│   │   └── [id]/
│   │       ├── index.tsx                ← Project detail + plot map
│   │       ├── edit.tsx
│   │       ├── plots/
│   │       │   ├── new.tsx              ← Create plot (with map polygon draw)
│   │       │   └── [plotId]/
│   │       │       ├── index.tsx        ← Plot detail
│   │       │       └── edit.tsx
│   │       └── partners/
│   │           ├── index.tsx            ← Partner list for project
│   │           └── new.tsx
│   ├── sales/
│   │   ├── index.tsx                    ← All sales list
│   │   ├── new.tsx                      ← Create sale
│   │   └── [id]/
│   │       ├── index.tsx                ← Sale detail (transactions + EMI)
│   │       └── record-payment.tsx       ← Record transaction
│   ├── buyers/
│   │   ├── index.tsx                    ← All buyers
│   │   └── [id].tsx                     ← Buyer profile + their sales
│   ├── agents/
│   │   ├── index.tsx                    ← All agents + their earnings
│   │   ├── new.tsx                      ← Create agent profile
│   │   └── [id].tsx                     ← Agent detail + leads + commission
│   └── reports/
│       └── index.tsx                    ← Project P&L, partner profit shares
│
├── (buyer)/
│   ├── _layout.tsx                      ← Buyer tab bar: My Plots | My Payments
│   ├── index.tsx                        ← My Plots (list)
│   ├── plots/
│   │   └── [id].tsx                     ← Plot detail + map
│   └── payments/
│       ├── index.tsx                    ← Payment history + upcoming EMIs
│       └── [saleId].tsx                 ← EMI schedule for a specific sale
│
├── (partner)/
│   ├── _layout.tsx                      ← Partner tab bar: My Projects | My Returns
│   ├── index.tsx                        ← My Projects (co-invested)
│   └── projects/
│       └── [id].tsx                     ← Project detail: plot map + profit share
│
└── (agent)/
    ├── _layout.tsx                      ← Agent tab bar: Available Plots | My Leads | Add Lead | My Earnings
    ├── index.tsx                        ← Available Plots (map + list)
    ├── plots/
    │   └── [id].tsx                     ← Plot detail (no price shown)
    ├── leads/
    │   ├── index.tsx                    ← My Leads (list, filtered by status)
    │   └── [id].tsx                     ← Lead detail + status update
    ├── add-lead.tsx                     ← Add new lead form
    └── earnings.tsx                     ← My Earnings: commission history + total_earned
```

---

## Tab Bar Definitions

### Admin Tab Bar
| Tab | Icon | Route |
|-----|------|-------|
| Dashboard | home | `/(admin)/` |
| Projects | map | `/(admin)/projects/` |
| Sales | file-text | `/(admin)/sales/` |
| Agents | users | `/(admin)/agents/` |
| Reports | bar-chart | `/(admin)/reports/` |

### Buyer Tab Bar
| Tab | Icon | Route |
|-----|------|-------|
| My Plots | map-pin | `/(buyer)/` |
| My Payments | credit-card | `/(buyer)/payments/` |

### Partner Tab Bar
| Tab | Icon | Route |
|-----|------|-------|
| My Projects | briefcase | `/(partner)/` |
| My Returns | trending-up | `/(partner)/` (second tab) |

### Agent Tab Bar
| Tab | Icon | Route |
|-----|------|-------|
| Available Plots | map | `/(agent)/` |
| My Leads | users | `/(agent)/leads/` |
| Add Lead | user-plus | `/(agent)/add-lead` |
| My Earnings | dollar-sign | `/(agent)/earnings` |

---

## Root Layout — Auth Gate + Role Redirect

```tsx
// app/_layout.tsx
import { Redirect, Stack } from 'expo-router';
import { useSession } from '@/hooks/useSession';

export default function RootLayout() {
  const { session, role, isLoading } = useSession();

  if (isLoading) return <SplashScreen />;

  if (!session) return <Redirect href="/(auth)/login" />;

  // Role-based redirect on first load
  switch (role) {
    case 'admin':   return <Redirect href="/(admin)/" />;
    case 'buyer':   return <Redirect href="/(buyer)/" />;
    case 'partner': return <Redirect href="/(partner)/" />;
    case 'agent':   return <Redirect href="/(agent)/" />;
    default:        return <Redirect href="/(auth)/login" />;
  }
}
```

---

## Zustand Session Store

```typescript
// stores/sessionStore.ts
interface SessionStore {
  session: Session | null;
  user: User | null;          // from public.users table
  role: UserRole | null;
  agentId: string | null;     // set if role = 'agent'
  setSession: (s: Session | null) => void;
  setUser: (u: User | null) => void;
  clear: () => void;
}
```

---

## Deep Linking Considerations

- Agent tapping a push notification for a lead → deep link to `/(agent)/leads/[id]`
- Buyer tapping EMI reminder → deep link to `/(buyer)/payments/[saleId]`
- Expo Router handles deep links via the file-based URL structure automatically

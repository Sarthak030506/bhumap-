---
name: roles-and-access
description: >
  Trigger phrases: role, permission, admin, buyer, partner, agent, navigation, RLS, access control,
  who can see, who can do, route guard, protected screen, tab structure, role-based redirect,
  agent access, buyer access, partner access
---

# Roles and Access Skill

## When to activate
Load this skill when the conversation involves:
- What a specific role can or cannot see/do
- Navigation structure and tab layout per role
- Expo Router route guards and role-based redirects
- RLS policy intent (the access rules; the SQL is in supabase-architecture)
- Agent-specific access restrictions (no financials, no partner data, no other leads)
- Designing screens that must respect role boundaries

## Reference files
| File | Contents |
|------|---------|
| `references/role-matrix.md` | Full grid: what each role can view/create/edit/delete per entity |
| `references/nav-structure.md` | Expo Router file structure per role; Agent tabs: Available Plots, My Leads, Add Lead, My Earnings |

## Critical access rules (summary)
| Rule | Detail |
|------|--------|
| Agent sees plots | Available plots only (`status = 'available'`) |
| Agent sees sales | Own rows only (where `sales.agent_id` = their agent record), commission fields only |
| Agent sees leads | Own rows only (`leads.agent_id` = their agent record) |
| Agent never sees | Sale price totals, partner info, other agents' leads, transactions, EMI schedules |
| Buyer sees | Own plots and own EMI/payment records only |
| Partner sees | Only projects they co-invested in; never buyer or agent data |
| Admin sees | Everything |

## Design principle
Role enforcement happens at two layers:
1. **Supabase RLS** — database-level, cannot be bypassed from client
2. **Expo Router route guards** — hide screens that a role shouldn't reach (UX, not security)
Both layers must agree. RLS is the real security; navigation guards are UX convenience.

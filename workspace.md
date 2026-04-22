I am starting a new React Native app from scratch. Nothing is built yet.
Set up a complete .claude/ workspace so you become my domain expert
before I write a single line of code.

App: Land developer platform
- Admin buys raw land, divides into plots, sells to buyers
- Four user roles: Admin, Buyer, Partner, Agent
  - Admin: full control
  - Buyer: sees own plots and payments only
  - Partner: sees co-invested plots only
  - Agent: finds buyers for admin, earns commission on closed deals,
    sees available plots + own leads only, never sees financials or partner info
- Core entities: Project, Plot, Sale, Transaction, EMI Schedule, 
  Partner, Agent, Lead, 7/12 Document

Tech stack (decided, not negotiable):
- React Native + Expo + Expo Router
- Supabase (Auth, PostgreSQL, RLS, Storage, Edge Functions, Cron)
- Zustand (state), React Query (data fetching)
- React Native Maps (plot polygons on Google Maps)
- Expo Notifications (EMI reminders)

---
BUILD THIS WORKSPACE:

CLAUDE.md (project root):
- App summary, 4 user roles, 9 core entities
- Tech stack with what each tool does
- Build order: Supabase setup → Auth → Projects → Plots → 
  Sales → EMI → Buyers → Partners → Agents → Reports → Notifications
- Ground rule: always discuss business logic and data model 
  before writing any code

.claude/skills/ — 5 skills, each with references/ folder:

1. supabase-architecture/
   SKILL.md — trigger: "database design, RLS, schema, policies, 
               edge functions, storage, cron"
   references/
   ├── schema.md        ← all 9 tables with columns, types, foreign keys:
   │                      users, projects, plots, partners, sales
   │                      (+ agent_id, commission_amount, commission_paid),
   │                      transactions, emi_schedule, agents, leads
   ├── rls-policies.md  ← all RLS rules per table per role per action
   │                      including agent-specific rules:
   │                      leads → agent sees own only
   │                      sales → agent sees own agent_id rows only
   │                      plots → agent sees status=available only
   └── edge-functions.md ← EMI generation, profit calc, 
                           commission calculation, notification logic

2. domain-logic/
   SKILL.md — trigger: "plot status, EMI calculation, sale flow, 
               partner profit, payment recording, agent commission,
               lead tracking"
   references/
   ├── business-rules.md  ← IF/THEN/ELSE rules including:
   │                         IF sale has agent_id:
   │                           commission = sale_price × (agent.commission_percent/100)
   │                           commission_paid = false
   │                         WHEN admin marks commission paid:
   │                           commission_paid = true
   │                           agents.total_earned += commission_amount
   ├── edge-cases.md      ← real land developer workflow failures
   └── domain-model.md    ← all entities, relationships, state machines
                            including lead lifecycle:
                            interested → site_visit_done → negotiating
                            → converted | lost

3. maps-and-plots/
   SKILL.md — trigger: "polygon, boundary, coordinates, map, 
               plot drawing, Google Maps"
   references/
   ├── coordinate-logic.md  ← how boundaries stored, drawn, rendered
   └── status-colors.md     ← available/reserved/sold color logic

4. roles-and-access/
   SKILL.md — trigger: "role, permission, admin, buyer, partner, 
               agent, navigation, RLS, access control"
   references/
   ├── role-matrix.md     ← what each role can see/do per entity:
   │                         Agent: available plots ✅, own leads ✅,
   │                         other agent leads ❌, sale amounts ❌,
   │                         partner info ❌, record payments ❌,
   │                         own commission ✅
   └── nav-structure.md   ← Expo Router file structure per role,
                            Agent tabs: Available Plots, My Leads,
                            Add Lead, My Earnings

5. emi-and-payments/
   SKILL.md — trigger: "EMI, installment, payment, transaction, 
               schedule, overdue, notification, commission"
   references/
   ├── emi-calculation.md    ← exact formula, schedule generation logic
   ├── payment-matching.md   ← how payments map to EMI rows
   └── notification-logic.md ← cron job rules, 7-day/today/overdue alerts

.claude/agents/ — 4 subagents:

1. domain-analyst.md
   tools: Read, Grep
   skills: domain-logic, emi-and-payments, roles-and-access
   job: map business logic, ask questions, surface gaps
   never: writes code, makes assumptions without asking
   always ends with: "Open questions:" section

2. system-architect.md
   tools: Read, Grep
   skills: supabase-architecture, domain-logic, maps-and-plots
   job: PostgreSQL schema design, RLS policies, Supabase 
        Edge Function design, React Query hook structure
   never: discusses UI, writes application code

3. edge-case-hunter.md
   tools: Read, Grep
   skills: domain-logic, emi-and-payments, roles-and-access
   job: find real-world failures — payment disputes, plot boundary 
        overlaps, partner percentage errors, EMI miscalculations,
        agent commission disputes, two agents claiming same buyer
   personality: skeptical, assumes worst-case user behavior
   never: proposes solutions, only surfaces problems with severity

4. react-native-architect.md
   tools: Read, Grep
   skills: maps-and-plots, roles-and-access, emi-and-payments
   job: Expo Router structure, component design, Zustand store 
        design, React Query hook patterns, navigation splits per role
        including separate Agent tab stack
   never: touches Supabase schema decisions, writes business logic

.claude/memory/
├── CLAUDE.md           ← router to all files
├── domain-model.md     ← all 9 entities with fields pre-filled
├── business-rules.md   ← IF/THEN/ELSE rules extracted from spec
├── schema.md           ← all 9 PostgreSQL tables with column definitions
├── decisions.md        ← empty, format ready
└── open-questions.md   ← pre-fill with known unknowns:
    - What happens if admin cancels a sale after EMIs started?
    - Can a plot be transferred to a new buyer?
    - What if partner percentages don't add to 100?
    - How are partial payments matched to EMI rows?
    - Can one buyer own multiple plots?
    - What if agent's lead converts but admin forgets to link agent_id?
    - Can agent be reassigned to different commission percent mid-project?
    - What if two agents both claim the same buyer?
    - Is commission on sale_price or total amount actually received?
    - If buyer pays directly to admin later — does agent still get commission?

SKILL quality rules:
- description = precise trigger phrases, not generic labels
- references/ files contain real domain content, not placeholders
- business-rules.md uses IF/THEN/ELSE format throughout
- Keep SKILL.md under 300 lines, depth goes in references/

SUBAGENT quality rules:
- Each has ONE job and hard "never" boundary
- tools field explicit — no inheriting all tools
- skills field pre-loads relevant skills at startup

After building, confirm by listing all files created,
then ask: "Which domain do you want to map first before we write code?"
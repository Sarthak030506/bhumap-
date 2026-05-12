# BhuMap — agent personas (Cursor + Claude Code)

This repo defines **specialist agents** as markdown under `.claude/agents/`. Those files were written for Claude Code’s agent feature. In **Cursor**, you reuse the same behavior in two ways:

1. **Cursor Task / subagents** — The Task tool supports types whose names match several personas below. Prefer `domain-analyst`, `edge-case-hunter`, `react-native-architect`, and `system-architect` when the task fits. Paste the **goal** and any constraints; the full “Job / Never / Always / Output format” contract lives in the matching file under `.claude/agents/`.
2. **Main chat** — The agent still follows `.cursor/rules/bhumap-memory-and-skills.mdc`, reads `.cursor/skills/`, and pulls `.claude/memory/` as needed.

## Persona files (full prompts)

| Persona | File | Use when |
|---------|------|----------|
| Domain analyst | `.claude/agents/domain-analyst.md` | New feature, domain validation, ambiguous business rules |
| Edge case hunter | `.claude/agents/edge-case-hunter.md` | Stress-test a design; problems only, no solutions |
| React Native architect | `.claude/agents/react-native-architect.md` | Expo Router structure, UI/navigation/store plan (not DB schema) |
| System architect | `.claude/agents/system-architect.md` | Postgres, RLS, Edge Functions, React Query data layer |

Cross-skill references in those files are relative to the **skills root**, e.g. `domain-logic/references/domain-model.md` → `.cursor/skills/domain-logic/references/domain-model.md`.

## Skills layout

| Skill folder | Topics |
|--------------|--------|
| `.cursor/skills/domain-logic/` | Business rules, edge cases, domain model |
| `.cursor/skills/emi-and-payments/` | EMI, payments, notifications |
| `.cursor/skills/maps-and-plots/` | Map, polygons, plot status |
| `.cursor/skills/roles-and-access/` | Roles, RLS mindset, nav |
| `.cursor/skills/supabase-architecture/` | Schema, RLS, Edge Functions |
| `.cursor/skills/ui-patterns/` | RN UI conventions |
| `.cursor/skills/caveman/` | Ultra-short replies when user enables it |

## Keeping `.claude/skills` and `.cursor/skills` in sync

Both trees should stay identical. After editing skills in either location, mirror to the other from the repo root (PowerShell):

```powershell
robocopy ".claude\skills" ".cursor\skills" /E /XO
```

(`/XO` skips older files on the destination — remove it if you want a full overwrite.)

## Product overview

Root **`CLAUDE.md`** is the high-level product spec (brand, stack, build order). **`.claude/memory/`** is the deep domain and schema memory; see `.claude/memory/CLAUDE.md` for the index.

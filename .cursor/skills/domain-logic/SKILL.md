---
name: domain-logic
description: >
  Trigger phrases: plot status, EMI calculation, sale flow, partner profit, payment recording,
  agent commission, lead tracking, business rules, domain rules, sale cancellation,
  plot transfer, commission dispute, partial payment
---

# Domain Logic Skill

## When to activate
Load this skill when the conversation involves:
- Plot lifecycle and status transitions (available → reserved → sold)
- Sale creation flow and what happens on each step
- EMI schedule generation rules and edge cases
- Partner profit calculation and when it triggers
- Payment recording and how it maps to EMI rows
- Agent commission rules: when it's created, when it's paid out
- Lead lifecycle and conversion to sale
- Any "what should happen if..." business question

## Reference files
| File | Contents |
|------|---------|
| `references/business-rules.md` | All IF/THEN/ELSE rules: plot status, EMI, commission, payments, cancellation |
| `references/edge-cases.md` | Real land developer workflow failures with severity ratings |
| `references/domain-model.md` | All 9 entities, their relationships, and state machines |

## Key domain rules (summary — full detail in references)
- A plot status moves: `available → sold` (direct); never skip to sold from nothing
- EMI schedule generated once at sale creation; rows are immutable except for `status` and `paid_date`
- Agent commission computed at sale creation; only admin can mark `commission_paid = true`
- A lead's `converted` state requires a matching `sales.agent_id` pointing to that agent
- Partners see project-level profit only — never individual sale or buyer data

## Principle
Always ask: **what is the admin trying to record in the real world?** Then model that as the source of truth, not the app state.

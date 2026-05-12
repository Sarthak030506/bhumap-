# Lead Rules

> **Status: Deferred to Phase 2** — Agent role and leads are not being built in Phase 1.
> These rules are documented here for future reference only.

## Lead Lifecycle
```
interested → site_visit_done → negotiating → converted | lost
```
- Any status can transition directly to `lost`
- `converted` and `lost` are terminal states — no transitions out
- Only the owning agent can update their own leads

## Conversion Rule
- Lead marked `converted` only when a sale is created with:
  - agent_id matching lead.agent_id
  - buyer_phone matching the lead's buyer_phone
- Conversion is triggered automatically by the `create-sale` Edge Function
- lead.converted_sale_id is set at conversion time

## Deduplication
- buyer_phone is the dedup key for leads
- If an agent tries to add a lead with a phone already in their leads → warn, don't block
- If phone matches another agent's lead → no warning (agents can't see each other's leads)

## Access Rules
- Agent sees only their own leads (RLS: lead.agent_id = auth.uid() via agents table)
- Admin sees all leads
- Buyer and other agents see nothing

## Lead Expiry (open question)
- No auto-expiry in v1
- Admin can manually mark leads as lost

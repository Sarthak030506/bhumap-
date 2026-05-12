# Open Questions — Unresolved Business Logic

Answer these before building the relevant module.

---

## Sale & Cancellation

1. **What happens if admin cancels a sale after EMIs have already started?**
   - Are partially-paid EMI rows preserved as a payment history, or deleted?
   - Does the buyer get a refund? How is that recorded?
   - Can a plot be re-sold to a new buyer after cancellation?

2. **Can a plot be transferred to a new buyer mid-sale?**
   - e.g., original buyer can no longer pay; admin wants to transfer to a different buyer
   - Does the EMI schedule reset, or does the new buyer inherit it?
   - What happens to paid transactions under the original buyer?

---

## Partner

3. **What if partner percentages don't add to 100%?**
   - Is the remainder implicitly the admin's profit?
   - Should the system enforce that they sum to exactly 100%, or allow under-allocation?
   - What if a partner exits mid-project? Is there a partner exit workflow?

---

## Payments

4. **How are partial payments matched to EMI rows?**
   - If buyer pays ₹15,000 against an EMI of ₹20,000, which EMI row is marked as what status?
   - Should we add a `partial` status and `amount_received` field to emi_schedule?
   - Or does admin just record the transaction without linking to an EMI row?

---

## Buyers

5. **Can one buyer own multiple plots?**
   - Can the same user.id (buyer) have active sales on two different plots?
   - Can they be in different projects, or the same project?
   - How does the buyer's "My Plots" screen handle this?

---

## Agents & Commission

6. **What if agent's lead converts but admin forgets to link agent_id on the sale?**
   - Commission is never generated; agent has no recourse in the app
   - Can admin retroactively add agent_id to an existing sale?
   - If yes, does the commission get calculated at that point?

7. **Can an agent's commission_percent be changed mid-project?**
   - If admin updates agent.commission_percent from 2% to 3%, do old unsettled commissions recalculate?
   - Answer: they should NOT — commission_amount is stored at sale creation (denormalized)
   - But: confirm this is the desired behavior

8. **What if two agents both claim the same buyer?**
   - Both have a lead for buyer with phone X
   - Admin creates the sale — which agent_id gets set?
   - Is there an in-app mechanism to surface duplicate leads (same buyer_phone)?
   - What happens to the losing agent's lead?

9. **Is commission calculated on sale_price or on the total amount actually received?**
   - Current assumption: commission = sale_price × percent (at sale creation, regardless of payments)
   - Alternative: commission = (total transactions collected) × percent — more complex, paid over time
   - Which model does the admin want?

10. **If buyer pays directly to admin later (outside EMI) — does agent still get commission?**
    - Current model: commission is set at sale creation; it's fixed regardless of payment method changes
    - Confirm this is intentional

---

## Technical

11. **Should EMI rows be deleted or soft-deleted on sale cancellation?**
    - Deleting: clean database; but loses payment history if any EMIs were paid
    - Soft-delete (add cancelled status or deleted_at): preserves history
    - Recommendation: add `cancelled` status to emi_status enum; set on cancellation

12. **Do we need a `reserved` plot status for v1?**
    - Use case: admin wants to hold a plot for a likely buyer before paperwork is complete
    - Current plan: go directly available → sold on sale creation
    - If reservation is needed, it adds a state to the plot state machine

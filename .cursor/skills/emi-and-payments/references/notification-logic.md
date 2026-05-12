# Notification Logic — EMI Reminders

---

## Notification Types

| Type | Trigger | Audience | Timing |
|------|---------|----------|--------|
| Upcoming EMI | Due in 7 days | Buyer | 7 days before due_date |
| Due Today | Due today | Buyer | Morning of due_date |
| Overdue | Past due_date, unpaid | Buyer | Daily until paid |

---

## Supabase Cron Schedule

```sql
-- Daily at 08:00 IST = 02:30 UTC
SELECT cron.schedule(
  'emi-reminder-daily',
  '30 2 * * *',
  $$
  SELECT net.http_post(
    url := 'https://<project>.functions.supabase.co/send-emi-notifications',
    headers := '{"Authorization": "Bearer <service-role-key>"}'::jsonb,
    body := '{}'::jsonb
  );
  $$
);
```

---

## Edge Function Logic (send-emi-notifications)

```typescript
const today = new Date();
today.setHours(0, 0, 0, 0);
const sevenDaysOut = addDays(today, 7);

// Step 1: Auto-mark overdue
await supabase
  .from('emi_schedule')
  .update({ status: 'overdue' })
  .eq('status', 'pending')
  .lt('due_date', today.toISOString().split('T')[0]);

// Step 2: Fetch EMIs to notify
const { data: upcoming } = await supabase
  .from('emi_schedule')
  .select(`
    id, due_date, amount, installment_number,
    sale:sales(
      plot:plots(plot_number, project:projects(name)),
      buyer:users!sales_buyer_id_fkey(id, full_name)
    )
  `)
  .eq('status', 'pending')
  .eq('due_date', sevenDaysOut.toISOString().split('T')[0]);

const { data: dueToday } = await supabase
  .from('emi_schedule')
  .select('...')
  .eq('status', 'pending')
  .eq('due_date', today.toISOString().split('T')[0]);

const { data: overdue } = await supabase
  .from('emi_schedule')
  .select('...')
  .eq('status', 'overdue');

// Step 3: Send Expo push notifications
for (const emi of upcoming)  await sendNotification(emi, 'upcoming');
for (const emi of dueToday)  await sendNotification(emi, 'due_today');
for (const emi of overdue)   await sendNotification(emi, 'overdue');
```

---

## Notification Message Templates

```typescript
const MESSAGES = {
  upcoming: (emi: EmiWithContext) => ({
    title: 'EMI Reminder',
    body: `EMI #${emi.installment_number} of ₹${formatCurrency(emi.amount)} for Plot ${emi.sale.plot.plot_number} (${emi.sale.plot.project.name}) is due on ${formatDate(emi.due_date)}.`,
    data: { type: 'emi_upcoming', saleId: emi.sale.id, emiId: emi.id },
  }),
  due_today: (emi: EmiWithContext) => ({
    title: 'EMI Due Today',
    body: `Your EMI of ₹${formatCurrency(emi.amount)} for Plot ${emi.sale.plot.plot_number} is due today. Please make the payment.`,
    data: { type: 'emi_due_today', saleId: emi.sale.id, emiId: emi.id },
  }),
  overdue: (emi: EmiWithContext) => ({
    title: '⚠️ Overdue EMI',
    body: `Your EMI of ₹${formatCurrency(emi.amount)} for Plot ${emi.sale.plot.plot_number} was due on ${formatDate(emi.due_date)} and is overdue. Please contact us.`,
    data: { type: 'emi_overdue', saleId: emi.sale.id, emiId: emi.id },
  }),
};
```

---

## Expo Push Token Storage

Admin registers device tokens during login:

```typescript
// On login/app start:
const token = await registerForPushNotificationsAsync();
// Store in users table or a separate push_tokens table:
await supabase
  .from('push_tokens')
  .upsert({ user_id: session.user.id, token, platform: Platform.OS });
```

Push token table (add to schema if needed):
```sql
CREATE TABLE push_tokens (
  user_id   uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token     text NOT NULL,
  platform  text NOT NULL,  -- 'ios' | 'android'
  updated_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, token)
);
```

---

## Deep Link on Notification Tap

```typescript
// In notification handler (app startup):
Notifications.addNotificationResponseReceivedListener(response => {
  const { type, saleId } = response.notification.request.content.data;
  if (type.startsWith('emi_')) {
    router.push(`/(buyer)/payments/${saleId}`);
  }
});
```

---

## Rules Summary

| Rule | Detail |
|------|--------|
| Only send to buyer | Notifications go to the buyer of the sale, not admin |
| One notification per EMI per day | Overdue EMIs get one notification per cron run (daily) |
| Stop sending when paid | Cron only queries `status = 'overdue'` — once paid, status changes and it stops |
| Respect notification permissions | Expo checks token validity; failed sends are silently skipped |
| Admin notifications | Out of scope for v1 — admin sees overdue in the Reports screen |

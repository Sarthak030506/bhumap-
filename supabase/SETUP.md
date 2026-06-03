# Supabase CLI Setup — BhuMap

## One-time link (do once per machine)

```bash
supabase login                                          # opens browser
supabase link --project-ref izpypzoxojgiclosgtms       # link to remote
supabase db pull                                        # pull remote schema
```

---

## Dev: phone OTP without real SMS

### Step 1 — Enable Phone in dashboard (one-time)
1. https://supabase.com/dashboard/project/izpypzoxojgiclosgtms/auth/providers
2. **Phone** → toggle **Enable** ON
3. Leave SMS provider fields empty (not needed for test numbers)
4. Scroll to **"Test phone numbers"** → add:
   - `+919999999999` → `123456`
   - `+919000000000` → `123456`
   - `+919561857265` → `123456`
5. Save

Test numbers skip real SMS. All other numbers get "SMS not configured" error.

### Step 2 — .env for real OTP flow (no stub)
```
EXPO_PUBLIC_AUTH_STUB=false
EXPO_PUBLIC_SKIP_AUTH=false
```
Use `+91 99999 99999` in the app → OTP is always `123456`.

### Bypass auth entirely (current default)
```
EXPO_PUBLIC_SKIP_AUTH=true   # goes straight to dashboard, skips all auth screens
```

---

## Production SMS — India (MSG91 via SMS Hook)

Supabase native providers (Twilio, Vonage, TextLocal) work but are expensive/unreliable for India.
Use **SMS Hook + MSG91** instead.

### Setup
1. Get MSG91 account → authkey + DLT-registered OTP template
2. Dashboard → Auth → Hooks → Add Hook → "Send SMS" → HTTPS → your Edge Function URL
3. Deploy `supabase/functions/sms-hook/index.ts` (handles Supabase webhook → MSG91 API)
4. Set secrets: `supabase secrets set MSG91_AUTH_KEY=xxx SMS_HOOK_SECRET=xxx`

### DLT registration (mandatory for India before production)
- Register at dlt.bsnl.co.in or any TRAI DLT portal
- Get Principal Entity ID + register SMS template
- Without DLT: Indian carriers block all transactional SMS

---

## Push config changes to remote

```bash
echo Y | supabase config push
```

## Run migrations

```bash
supabase db push          # push local migrations to remote
supabase migration list   # check status
```

/** Dev-only: explicit “no auth screens” flag (never ship true in prod). */
export const SKIP_AUTH = process.env.EXPO_PUBLIC_SKIP_AUTH === 'true';

/** OTP SMS not sent; uses stub path in login. */
export const AUTH_STUB = process.env.EXPO_PUBLIC_AUTH_STUB === 'true';

/** Cold start goes to tabs (no real Supabase session). AUTH_STUB or SKIP_AUTH. */
export const BYPASS_AUTH_ENTRY = SKIP_AUTH || AUTH_STUB;

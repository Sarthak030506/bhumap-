import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
} from 'react-native';
import { useRouter } from 'expo-router';
import { supabase } from '@/lib/supabase';
import { Colors, FontSize, FontWeight, Spacing, Radius } from '@/constants/tokens';
import { SKIP_AUTH } from '@/constants/featureFlags';

export default function LoginScreen() {
  const router = useRouter();
  const [phone, setPhone] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isStub = process.env.EXPO_PUBLIC_AUTH_STUB === 'true';

  function validatePhone(raw: string): string | null {
    const digits = raw.replace(/\D/g, '');
    if (digits.length !== 10) return 'Enter a valid 10-digit mobile number';
    return null;
  }

  async function handleSendOtp() {
    setError('');
    if (SKIP_AUTH) {
      router.replace('/(tabs)/dashboard');
      return;
    }
    const validationError = validatePhone(phone);
    if (validationError) {
      setError(validationError);
      return;
    }

    const e164 = `+91${phone.replace(/\D/g, '')}`;
    setLoading(true);

    try {
      if (isStub) {
        console.log('[AUTH STUB] skip OTP → dashboard', e164);
        router.replace('/(tabs)/dashboard');
        return;
      }

      const { error: supaError } = await supabase.auth.signInWithOtp({
        phone: e164,
        options: {
          channel: 'sms',
          shouldCreateUser: true,
        },
      });

      if (supaError) {
        setError(supaError.message);
        return;
      }

      router.push({ pathname: '/(auth)/otp', params: { phone: e164 } });
    } catch (e: any) {
      setError(e?.message ?? 'Something went wrong. Try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.inner}>
        <View style={styles.brand}>
          <Text style={styles.brandName}>BhuMap</Text>
          <Text style={styles.tagline}>Apni Zameen, Apna Hisaab</Text>
        </View>

        <View style={styles.form}>
          <Text style={styles.label}>Mobile number</Text>

          <View style={[styles.phoneRow, error ? styles.inputError : null]}>
            <View style={styles.prefix}>
              <Text style={styles.prefixText}>+91</Text>
            </View>
            <TextInput
              style={styles.input}
              placeholder="98765 43210"
              placeholderTextColor={Colors.textMuted}
              keyboardType="phone-pad"
              maxLength={10}
              value={phone}
              onChangeText={(t) => {
                setPhone(t.replace(/\D/g, ''));
                if (error) setError('');
              }}
              returnKeyType="done"
              onSubmitEditing={handleSendOtp}
              autoFocus
            />
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <TouchableOpacity
            style={[styles.btn, loading && styles.btnDisabled]}
            onPress={handleSendOtp}
            disabled={loading}
            activeOpacity={0.85}
          >
            {loading ? (
              <ActivityIndicator color={Colors.textInverse} />
            ) : (
              <Text style={styles.btnText}>Send OTP</Text>
            )}
          </TouchableOpacity>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: Colors.bg,
  },
  inner: {
    flex: 1,
    paddingHorizontal: Spacing['6'],
    justifyContent: 'center',
  },
  brand: {
    marginBottom: Spacing['10'],
    alignItems: 'center',
  },
  brandName: {
    fontSize: FontSize['3xl'],
    fontWeight: FontWeight.bold,
    color: Colors.primary,
    letterSpacing: -0.5,
  },
  tagline: {
    fontSize: FontSize.base,
    color: Colors.textSecondary,
    marginTop: Spacing['1'],
  },
  form: {
    gap: Spacing['2'],
  },
  label: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    color: Colors.textSecondary,
    marginBottom: Spacing['1'],
  },
  phoneRow: {
    flexDirection: 'row',
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: Radius.md,
    backgroundColor: Colors.bgCard,
    overflow: 'hidden',
  },
  inputError: {
    borderColor: Colors.error,
  },
  prefix: {
    paddingHorizontal: Spacing['3'],
    justifyContent: 'center',
    borderRightWidth: 1,
    borderRightColor: Colors.border,
    backgroundColor: Colors.bgMuted,
  },
  prefixText: {
    fontSize: FontSize.base,
    color: Colors.text,
    fontWeight: FontWeight.medium,
  },
  input: {
    flex: 1,
    paddingHorizontal: Spacing['3'],
    paddingVertical: Spacing['3'],
    fontSize: FontSize.md,
    color: Colors.text,
    letterSpacing: 1,
  },
  errorText: {
    fontSize: FontSize.sm,
    color: Colors.error,
    marginTop: Spacing['1'],
  },
  btn: {
    backgroundColor: Colors.primary,
    borderRadius: Radius.md,
    paddingVertical: Spacing['4'],
    alignItems: 'center',
    marginTop: Spacing['4'],
  },
  btnDisabled: {
    opacity: 0.6,
  },
  btnText: {
    color: Colors.textInverse,
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },
});

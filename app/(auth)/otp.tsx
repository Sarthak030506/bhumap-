import React, { useState, useRef, useEffect } from 'react';
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
import { useRouter, useLocalSearchParams } from 'expo-router';
import { supabase } from '@/lib/supabase';
import { Colors, FontSize, FontWeight, Spacing, Radius } from '@/constants/tokens';
import { AUTH_STUB, BYPASS_AUTH_ENTRY } from '@/constants/featureFlags';

const OTP_LENGTH = 6;
const RESEND_COOLDOWN = 30;

export default function OtpScreen() {
  const router = useRouter();
  const { phone } = useLocalSearchParams<{ phone: string }>();
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [resendCountdown, setResendCountdown] = useState(RESEND_COOLDOWN);
  const inputRef = useRef<TextInput>(null);

  const maskedPhone = phone?.replace(/(\+91)(\d{5})(\d{5})/, '$1 $2 $3');

  useEffect(() => {
    if (BYPASS_AUTH_ENTRY) {
      router.replace('/(tabs)/dashboard');
      return;
    }
    if (!phone) {
      router.replace('/(auth)/login');
      return;
    }
    inputRef.current?.focus();
    const timer = setInterval(() => {
      setResendCountdown((c) => (c > 0 ? c - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [phone]);

  async function handleVerify() {
    if (loading) return;
    if (otp.length !== OTP_LENGTH) {
      setError('Enter the 6-digit OTP');
      return;
    }
    if (!phone) {
      setError('Phone number missing. Go back and retry.');
      return;
    }

    setError('');
    setInfo('');
    setLoading(true);

    try {
      if (AUTH_STUB) {
        console.log('[AUTH STUB] OTP verified:', otp);
        router.replace('/(tabs)/dashboard');
        return;
      }

      const { error: supaError } = await supabase.auth.verifyOtp({
        phone,
        token: otp,
        type: 'sms',
      });

      if (supaError) {
        setError(supaError.message);
        setOtp('');
        return;
      }

      router.replace('/(tabs)/dashboard');
    } catch (e: any) {
      setError(e?.message ?? 'Verification failed. Try again.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (otp.length === OTP_LENGTH && !loading && !AUTH_STUB) {
      handleVerify();
    }
  }, [otp]);

  async function handleResend() {
    if (resending || resendCountdown > 0 || !phone) return;
    setError('');
    setInfo('');
    setOtp('');
    setResending(true);

    try {
      if (AUTH_STUB) {
        console.log('[AUTH STUB] OTP resent to', phone);
        setResendCountdown(RESEND_COOLDOWN);
        setInfo('OTP resent.');
        return;
      }
      const { error: supaError } = await supabase.auth.signInWithOtp({
        phone,
        options: { channel: 'sms', shouldCreateUser: true },
      });
      if (supaError) {
        setError(supaError.message);
        return;
      }
      setResendCountdown(RESEND_COOLDOWN);
      setInfo('OTP resent.');
    } catch (e: any) {
      setError(e?.message ?? 'Resend failed. Try again.');
    } finally {
      setResending(false);
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.inner}>
        <TouchableOpacity style={styles.backBtn} onPress={() => router.back()}>
          <Text style={styles.backText}>← Back</Text>
        </TouchableOpacity>

        <Text style={styles.title}>Enter OTP</Text>
        <Text style={styles.subtitle}>
          Sent to <Text style={styles.phoneHighlight}>{maskedPhone}</Text>
        </Text>

        <TouchableOpacity activeOpacity={1} onPress={() => inputRef.current?.focus()}>
          <View style={styles.dotsRow}>
            {Array.from({ length: OTP_LENGTH }).map((_, i) => (
              <View
                key={i}
                style={[
                  styles.dot,
                  otp.length === i && styles.dotActive,
                  otp[i] && styles.dotFilled,
                  error && styles.dotError,
                ]}
              >
                <Text style={styles.dotText}>{otp[i] ?? ''}</Text>
              </View>
            ))}
          </View>
        </TouchableOpacity>

        <TextInput
          ref={inputRef}
          style={styles.hiddenInput}
          keyboardType="number-pad"
          maxLength={OTP_LENGTH}
          value={otp}
          onChangeText={(t) => {
            setOtp(t.replace(/\D/g, ''));
            if (error) setError('');
          }}
          onSubmitEditing={handleVerify}
          caretHidden
        />

        {error ? <Text style={styles.errorText}>{error}</Text> : null}
        {!error && info ? <Text style={styles.infoText}>{info}</Text> : null}

        <TouchableOpacity
          style={[styles.btn, (loading || otp.length !== OTP_LENGTH) && styles.btnDisabled]}
          onPress={handleVerify}
          disabled={loading || otp.length !== OTP_LENGTH}
          activeOpacity={0.85}
        >
          {loading ? (
            <ActivityIndicator color={Colors.textInverse} />
          ) : (
            <Text style={styles.btnText}>Verify & Sign in</Text>
          )}
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.resendBtn}
          onPress={handleResend}
          disabled={resendCountdown > 0 || resending}
        >
          <Text
            style={[
              styles.resendText,
              (resendCountdown > 0 || resending) && styles.resendDisabled,
            ]}
          >
            {resending
              ? 'Resending…'
              : resendCountdown > 0
                ? `Resend OTP in ${resendCountdown}s`
                : 'Resend OTP'}
          </Text>
        </TouchableOpacity>
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
    paddingTop: Spacing['12'],
  },
  backBtn: {
    marginBottom: Spacing['8'],
  },
  backText: {
    fontSize: FontSize.base,
    color: Colors.primary,
    fontWeight: FontWeight.medium,
  },
  title: {
    fontSize: FontSize['2xl'],
    fontWeight: FontWeight.bold,
    color: Colors.text,
    marginBottom: Spacing['2'],
  },
  subtitle: {
    fontSize: FontSize.base,
    color: Colors.textSecondary,
    marginBottom: Spacing['8'],
  },
  phoneHighlight: {
    color: Colors.text,
    fontWeight: FontWeight.medium,
  },
  dotsRow: {
    flexDirection: 'row',
    gap: Spacing['3'],
    marginBottom: Spacing['3'],
  },
  dot: {
    flex: 1,
    aspectRatio: 1,
    maxWidth: 52,
    borderWidth: 1.5,
    borderColor: Colors.border,
    borderRadius: Radius.md,
    backgroundColor: Colors.bgCard,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dotActive: {
    borderColor: Colors.primary,
  },
  dotFilled: {
    borderColor: Colors.primary,
    backgroundColor: Colors.bgCard,
  },
  dotError: {
    borderColor: Colors.error,
  },
  dotText: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.semibold,
    color: Colors.text,
  },
  hiddenInput: {
    position: 'absolute',
    opacity: 0,
    height: 0,
    width: 0,
  },
  errorText: {
    fontSize: FontSize.sm,
    color: Colors.error,
    marginBottom: Spacing['3'],
  },
  infoText: {
    fontSize: FontSize.sm,
    color: Colors.primary,
    marginBottom: Spacing['3'],
  },
  btn: {
    backgroundColor: Colors.primary,
    borderRadius: Radius.md,
    paddingVertical: Spacing['4'],
    alignItems: 'center',
    marginTop: Spacing['4'],
  },
  btnDisabled: {
    opacity: 0.4,
  },
  btnText: {
    color: Colors.textInverse,
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },
  resendBtn: {
    alignItems: 'center',
    marginTop: Spacing['5'],
    paddingVertical: Spacing['2'],
  },
  resendText: {
    fontSize: FontSize.sm,
    color: Colors.primary,
    fontWeight: FontWeight.medium,
  },
  resendDisabled: {
    color: Colors.textMuted,
  },
});

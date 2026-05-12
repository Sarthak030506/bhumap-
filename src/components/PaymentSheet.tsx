import React, { forwardRef, useImperativeHandle, useRef, useState } from 'react';
import { Platform, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import DateTimePicker, { DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type BottomSheet from '@gorhom/bottom-sheet';

import { Sheet, SheetRef } from './Sheet';
import { Field } from './Field';
import { Button } from './Button';
import { PaymentInput, PaymentSchema } from '@/lib/validators';
import { Colors, FontSize, FontWeight, Radius, Spacing } from '@/constants/tokens';
import { formatDate } from '@/utils/format';

export interface PaymentSheetRef {
  open: () => void;
  close: () => void;
}

interface PaymentSheetProps {
  title?: string;
  submitLabel?: string;
  onSubmit: (input: PaymentInput) => Promise<void>;
}

const METHODS: { value: PaymentInput['method']; label: string }[] = [
  { value: 'cash', label: 'Cash' },
  { value: 'upi', label: 'UPI' },
  { value: 'bank_transfer', label: 'Bank' },
  { value: 'cheque', label: 'Cheque' },
];

export const PaymentSheet = forwardRef<PaymentSheetRef, PaymentSheetProps>(
  function PaymentSheet({ title = 'Add payment', submitLabel = 'Save payment', onSubmit }, ref) {
    const sheetRef = useRef<SheetRef>(null);
    const [showDatePicker, setShowDatePicker] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [serverError, setServerError] = useState('');

    const {
      control,
      handleSubmit,
      reset,
      watch,
      setValue,
      formState: { errors },
    } = useForm<PaymentInput>({
      resolver: zodResolver(PaymentSchema) as any,
      defaultValues: {
        amount: undefined as any,
        payment_date: new Date().toISOString().slice(0, 10),
        method: 'cash',
        cheque_number: '',
        utr_number: '',
        notes: '',
      },
    });

    const method = watch('method');
    const dateStr = watch('payment_date');

    useImperativeHandle(ref, () => ({
      open: () => {
        reset();
        setServerError('');
        (sheetRef.current as BottomSheet | null)?.expand();
      },
      close: () => (sheetRef.current as BottomSheet | null)?.close(),
    }));

    const onDateChange = (_e: DateTimePickerEvent, picked?: Date) => {
      if (Platform.OS === 'android') setShowDatePicker(false);
      if (picked) setValue('payment_date', picked.toISOString().slice(0, 10));
    };

    const submit = handleSubmit(async (input) => {
      try {
        setSubmitting(true);
        setServerError('');
        await onSubmit(input);
        (sheetRef.current as BottomSheet | null)?.close();
      } catch (e: any) {
        setServerError(e?.message ?? 'Save failed');
      } finally {
        setSubmitting(false);
      }
    });

    return (
      <Sheet ref={sheetRef} snapPoints={['65%', '90%']} scrollable>
        <Text style={styles.title}>{title}</Text>

        <Controller
          control={control}
          name="amount"
          render={({ field: { value, onChange, onBlur } }) => (
            <Field
              label="Amount (₹)"
              keyboardType="numeric"
              placeholder="0"
              value={value !== undefined ? String(value) : ''}
              onBlur={onBlur}
              onChangeText={onChange}
              error={errors.amount?.message}
            />
          )}
        />

        <View style={styles.fieldBlock}>
          <Text style={styles.label}>Payment date</Text>
          <TouchableOpacity
            style={styles.dateBtn}
            onPress={() => setShowDatePicker(true)}
            activeOpacity={0.7}
          >
            <Text style={styles.dateText}>{formatDate(dateStr)}</Text>
          </TouchableOpacity>
          {errors.payment_date && (
            <Text style={styles.errorText}>{errors.payment_date.message}</Text>
          )}
        </View>

        {showDatePicker && (
          <DateTimePicker
            value={new Date(dateStr)}
            mode="date"
            display={Platform.OS === 'ios' ? 'spinner' : 'default'}
            maximumDate={new Date()}
            onChange={onDateChange}
          />
        )}

        <View style={styles.fieldBlock}>
          <Text style={styles.label}>Method</Text>
          <View style={styles.methodRow}>
            {METHODS.map((m) => {
              const active = method === m.value;
              return (
                <TouchableOpacity
                  key={m.value}
                  style={[styles.methodBtn, active && styles.methodBtnActive]}
                  onPress={() => setValue('method', m.value)}
                  activeOpacity={0.8}
                >
                  <Text
                    style={[styles.methodLabel, active && styles.methodLabelActive]}
                  >
                    {m.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
        </View>

        {method === 'cheque' && (
          <Controller
            control={control}
            name="cheque_number"
            render={({ field: { value, onChange, onBlur } }) => (
              <Field
                label="Cheque number"
                placeholder="123456"
                keyboardType="numeric"
                value={value ?? ''}
                onBlur={onBlur}
                onChangeText={onChange}
                error={errors.cheque_number?.message}
              />
            )}
          />
        )}

        {(method === 'upi' || method === 'bank_transfer') && (
          <Controller
            control={control}
            name="utr_number"
            render={({ field: { value, onChange, onBlur } }) => (
              <Field
                label={method === 'upi' ? 'UPI reference' : 'UTR / reference'}
                placeholder="Enter reference"
                value={value ?? ''}
                onBlur={onBlur}
                onChangeText={onChange}
                error={errors.utr_number?.message}
              />
            )}
          />
        )}

        <Controller
          control={control}
          name="notes"
          render={({ field: { value, onChange, onBlur } }) => (
            <Field
              label="Notes (optional)"
              placeholder="Any details"
              multiline
              value={value ?? ''}
              onBlur={onBlur}
              onChangeText={onChange}
            />
          )}
        />

        {serverError ? <Text style={styles.errorText}>{serverError}</Text> : null}

        <View style={styles.footer}>
          <Button label={submitLabel} loading={submitting} onPress={submit} fullWidth />
        </View>
      </Sheet>
    );
  },
);

const styles = StyleSheet.create({
  title: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    color: Colors.text,
    marginBottom: Spacing['4'],
  },
  fieldBlock: {
    marginBottom: Spacing['4'],
  },
  label: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    color: Colors.textSecondary,
    marginBottom: Spacing['1'],
  },
  dateBtn: {
    height: 48,
    borderRadius: Radius.md,
    borderWidth: 1,
    borderColor: Colors.border,
    backgroundColor: Colors.bgCard,
    paddingHorizontal: Spacing['3'],
    justifyContent: 'center',
  },
  dateText: {
    fontSize: FontSize.base,
    color: Colors.text,
  },
  methodRow: {
    flexDirection: 'row',
    gap: Spacing['2'],
  },
  methodBtn: {
    flex: 1,
    height: 44,
    borderRadius: Radius.md,
    borderWidth: 1,
    borderColor: Colors.border,
    backgroundColor: Colors.bgCard,
    alignItems: 'center',
    justifyContent: 'center',
  },
  methodBtnActive: {
    borderColor: Colors.primary,
    backgroundColor: Colors.primary,
  },
  methodLabel: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    color: Colors.textSecondary,
  },
  methodLabelActive: {
    color: Colors.textInverse,
  },
  errorText: {
    fontSize: FontSize.sm,
    color: Colors.error,
    marginBottom: Spacing['2'],
  },
  footer: {
    marginTop: Spacing['4'],
  },
});

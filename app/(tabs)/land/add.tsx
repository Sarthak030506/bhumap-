import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import DateTimePicker, { DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { Colors, FontSize, FontWeight, Spacing, Radius } from '@/constants/tokens';
import { Field } from '@/components/Field';
import { Button } from '@/components/Button';
import { LandSchema, LandInput } from '@/lib/validators';
import { useCreateLand, AreaUnit, RegistrationStatus } from '@/hooks/useLands';
import { formatDate } from '@/utils/format';

const AREA_UNITS: { value: AreaUnit; label: string }[] = [
  { value: 'guntha', label: 'Guntha' },
  { value: 'acre', label: 'Acre' },
  { value: 'sqft', label: 'Sqft' },
];

export default function AddLandScreen() {
  const router = useRouter();
  const createLand = useCreateLand();
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [serverError, setServerError] = useState('');

  const {
    control,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<LandInput>({
    resolver: zodResolver(LandSchema) as any,
    defaultValues: {
      name: '',
      owner_name: '',
      owner_phone: '',
      gat_number: '',
      village: '',
      taluka: '',
      district: 'Maharashtra',
      total_area: undefined as any,
      area_unit: 'guntha',
      agreed_price: undefined as any,
      advance_paid: undefined as any,
      acquisition_date: new Date().toISOString().slice(0, 10),
      registration_status: 'pending',
      notes: '',
    },
  });

  const dateStr = watch('acquisition_date');
  const areaUnit = watch('area_unit');
  const regStatus = watch('registration_status');

  const onDateChange = (_e: DateTimePickerEvent, picked?: Date) => {
    if (Platform.OS === 'android') setShowDatePicker(false);
    if (picked) setValue('acquisition_date', picked.toISOString().slice(0, 10));
  };

  const onSubmit = handleSubmit(async (input) => {
    try {
      setServerError('');
      const land = await createLand.mutateAsync(input);
      router.replace(`/(tabs)/land/${land.id}`);
    } catch (e: any) {
      setServerError(e?.message ?? 'Could not save. Try again.');
    }
  });

  return (
    <SafeAreaView style={styles.root}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()}>
          <Text style={styles.backText}>← Back</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Add land</Text>
      </View>

      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <ScrollView
          style={{ flex: 1 }}
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled"
        >
          <Section title="Owner / farmer">
            <Controller
              control={control}
              name="owner_name"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Owner name"
                  placeholder="Full name"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.owner_name?.message}
                />
              )}
            />
            <Controller
              control={control}
              name="owner_phone"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Owner phone"
                  placeholder="98765 43210"
                  keyboardType="phone-pad"
                  maxLength={10}
                  value={value}
                  onBlur={onBlur}
                  onChangeText={(t) => onChange(t.replace(/\D/g, ''))}
                  error={errors.owner_phone?.message}
                />
              )}
            />
          </Section>

          <Section title="Land details">
            <Controller
              control={control}
              name="name"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Land name / nickname"
                  placeholder="e.g. Wagholi 5 acres"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.name?.message}
                />
              )}
            />
            <Controller
              control={control}
              name="gat_number"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Gat / survey number (optional)"
                  placeholder="e.g. 142/2"
                  value={value ?? ''}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.gat_number?.message}
                />
              )}
            />
            <Controller
              control={control}
              name="village"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Village"
                  placeholder="Wagholi"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.village?.message}
                />
              )}
            />
            <Controller
              control={control}
              name="taluka"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Taluka"
                  placeholder="Haveli"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.taluka?.message}
                />
              )}
            />
            <Controller
              control={control}
              name="district"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="District"
                  placeholder="Pune"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.district?.message}
                />
              )}
            />

            <View style={styles.row}>
              <View style={{ flex: 2 }}>
                <Controller
                  control={control}
                  name="total_area"
                  render={({ field: { value, onChange, onBlur } }) => (
                    <Field
                      label="Total area"
                      placeholder="0"
                      keyboardType="numeric"
                      value={value !== undefined ? String(value) : ''}
                      onBlur={onBlur}
                      onChangeText={onChange}
                      error={errors.total_area?.message}
                    />
                  )}
                />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={styles.fieldLabel}>Unit</Text>
                <View style={styles.unitRow}>
                  {AREA_UNITS.map((u) => {
                    const active = areaUnit === u.value;
                    return (
                      <TouchableOpacity
                        key={u.value}
                        style={[styles.unitBtn, active && styles.unitBtnActive]}
                        onPress={() => setValue('area_unit', u.value)}
                      >
                        <Text
                          style={[
                            styles.unitLabel,
                            active && styles.unitLabelActive,
                          ]}
                        >
                          {u.label}
                        </Text>
                      </TouchableOpacity>
                    );
                  })}
                </View>
              </View>
            </View>

            <Controller
              control={control}
              name="agreed_price"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Agreed price (₹)"
                  placeholder="0"
                  keyboardType="numeric"
                  value={value !== undefined ? String(value) : ''}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.agreed_price?.message}
                />
              )}
            />
            <Controller
              control={control}
              name="advance_paid"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Advance paid at acquisition (optional)"
                  placeholder="0"
                  keyboardType="numeric"
                  value={value !== undefined ? String(value) : ''}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={errors.advance_paid?.message}
                />
              )}
            />

            <View style={styles.fieldBlock}>
              <Text style={styles.fieldLabel}>Acquisition date</Text>
              <TouchableOpacity
                style={styles.dateBtn}
                onPress={() => setShowDatePicker(true)}
                activeOpacity={0.7}
              >
                <Text style={styles.dateText}>{formatDate(dateStr)}</Text>
              </TouchableOpacity>
              {errors.acquisition_date && (
                <Text style={styles.errorText}>{errors.acquisition_date.message}</Text>
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
              <Text style={styles.fieldLabel}>Registration status</Text>
              <View style={styles.toggleRow}>
                {(['pending', 'registered'] as RegistrationStatus[]).map((opt) => {
                  const active = regStatus === opt;
                  return (
                    <TouchableOpacity
                      key={opt}
                      style={[styles.toggleBtn, active && styles.toggleBtnActive]}
                      onPress={() => setValue('registration_status', opt)}
                    >
                      <Text
                        style={[
                          styles.toggleLabel,
                          active && styles.toggleLabelActive,
                        ]}
                      >
                        {opt === 'pending' ? 'Pending' : 'Registered'}
                      </Text>
                    </TouchableOpacity>
                  );
                })}
              </View>
            </View>
          </Section>

          <Section title="Documents (optional)">
            <TouchableOpacity style={styles.placeholderBtn} disabled>
              <Text style={styles.placeholderBtnText}>Upload 7/12 / agreement</Text>
              <Text style={styles.placeholderBtnSub}>Coming soon</Text>
            </TouchableOpacity>
          </Section>

          <Section title="Map boundary (optional)">
            <TouchableOpacity style={styles.placeholderBtn} disabled>
              <Text style={styles.placeholderBtnText}>Mark on map</Text>
              <Text style={styles.placeholderBtnSub}>Set boundary later from Map tab</Text>
            </TouchableOpacity>
          </Section>

          <Section title="Notes">
            <Controller
              control={control}
              name="notes"
              render={({ field: { value, onChange, onBlur } }) => (
                <Field
                  label="Any extra details (optional)"
                  placeholder="Multiple sellers, conditions, etc."
                  multiline
                  value={value ?? ''}
                  onBlur={onBlur}
                  onChangeText={onChange}
                />
              )}
            />
          </Section>

          {serverError ? <Text style={styles.serverError}>{serverError}</Text> : null}
        </ScrollView>

        <View style={styles.footer}>
          <Button
            label="Save land"
            onPress={onSubmit}
            loading={createLand.isPending}
            fullWidth
          />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title}</Text>
      <View>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: Colors.bg },
  header: {
    paddingHorizontal: Spacing['5'],
    paddingVertical: Spacing['4'],
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
    gap: Spacing['2'],
  },
  backText: { fontSize: FontSize.base, color: Colors.primary, fontWeight: FontWeight.medium },
  title: { fontSize: FontSize.xl, fontWeight: FontWeight.bold, color: Colors.text },
  scroll: {
    paddingHorizontal: Spacing['5'],
    paddingVertical: Spacing['4'],
    paddingBottom: Spacing['8'],
  },
  section: {
    marginBottom: Spacing['5'],
  },
  sectionTitle: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
    color: Colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: Spacing['3'],
  },
  row: {
    flexDirection: 'row',
    gap: Spacing['3'],
  },
  fieldBlock: {
    marginBottom: Spacing['4'],
  },
  fieldLabel: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    color: Colors.textSecondary,
    marginBottom: Spacing['1'],
  },
  unitRow: {
    flexDirection: 'row',
    gap: 4,
    height: 48,
  },
  unitBtn: {
    flex: 1,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: Radius.sm,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.bgCard,
  },
  unitBtnActive: {
    borderColor: Colors.primary,
    backgroundColor: Colors.primary,
  },
  unitLabel: {
    fontSize: FontSize.xs,
    color: Colors.textSecondary,
    fontWeight: FontWeight.medium,
  },
  unitLabelActive: {
    color: Colors.textInverse,
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
  toggleRow: {
    flexDirection: 'row',
    gap: Spacing['2'],
  },
  toggleBtn: {
    flex: 1,
    height: 44,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: Radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.bgCard,
  },
  toggleBtnActive: {
    borderColor: Colors.primary,
    backgroundColor: Colors.primary,
  },
  toggleLabel: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    color: Colors.textSecondary,
  },
  toggleLabelActive: { color: Colors.textInverse },
  placeholderBtn: {
    height: 56,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: Radius.md,
    backgroundColor: Colors.bgMuted,
    paddingHorizontal: Spacing['3'],
    justifyContent: 'center',
    opacity: 0.7,
  },
  placeholderBtnText: {
    fontSize: FontSize.base,
    color: Colors.textSecondary,
    fontWeight: FontWeight.medium,
  },
  placeholderBtnSub: {
    fontSize: FontSize.xs,
    color: Colors.textMuted,
    marginTop: 2,
  },
  errorText: {
    fontSize: FontSize.xs,
    color: Colors.error,
    marginTop: Spacing['1'],
  },
  serverError: {
    fontSize: FontSize.sm,
    color: Colors.error,
    marginTop: Spacing['2'],
    marginBottom: Spacing['2'],
  },
  footer: {
    paddingHorizontal: Spacing['5'],
    paddingVertical: Spacing['3'],
    borderTopWidth: 1,
    borderTopColor: Colors.border,
    backgroundColor: Colors.bgCard,
  },
});

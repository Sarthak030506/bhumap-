import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Card } from './Card';
import { Amount } from './Amount';
import { Colors, FontSize, FontWeight, Radius, Spacing } from '@/constants/tokens';
import { formatArea } from '@/utils/format';
import type { Land } from '@/hooks/useLands';

interface LandCardProps {
  land: Land;
  onPress: () => void;
}

export function LandCard({ land, onPress }: LandCardProps) {
  const settled = Number(land.remaining_to_owner) <= 0;
  const pending = !settled;
  const regPending = land.registration_status === 'pending';

  const locationParts = [land.village, land.taluka].filter(Boolean);
  const location = locationParts.length ? locationParts.join(', ') : land.district;

  return (
    <Card onPress={onPress} style={styles.card}>
      <View style={styles.headerRow}>
        <Text style={styles.name} numberOfLines={1}>
          {land.name}
        </Text>
        <View
          style={[
            styles.chip,
            pending ? styles.chipPending : styles.chipSettled,
          ]}
        >
          <Text
            style={[
              styles.chipText,
              pending ? styles.chipTextPending : styles.chipTextSettled,
            ]}
          >
            {pending ? 'Payment pending' : 'Settled'}
          </Text>
        </View>
      </View>

      <Text style={styles.location} numberOfLines={1}>
        {location}
        {land.gat_number ? ` • Gat ${land.gat_number}` : ''}
      </Text>

      {land.owner_name ? (
        <Text style={styles.owner}>Owner: {land.owner_name}</Text>
      ) : null}

      <View style={styles.metaRow}>
        <Text style={styles.metaText}>
          {formatArea(Number(land.total_area_sqft), land.area_unit)}
        </Text>
        <View style={styles.dot} />
        <Text style={styles.metaText}>Acquired {formatShortDate(land.acquisition_date)}</Text>
      </View>

      <View style={styles.divider} />

      <View style={styles.financeRow}>
        <View style={styles.financeCell}>
          <Text style={styles.financeLabel}>Paid</Text>
          <Amount value={Number(land.total_paid)} size="sm" color={Colors.success} />
        </View>
        <View style={styles.financeCell}>
          <Text style={styles.financeLabel}>Remaining</Text>
          <Amount
            value={Number(land.remaining_to_owner)}
            size="sm"
            color={pending ? Colors.accent : Colors.success}
          />
        </View>
        <View style={styles.financeCell}>
          <Text style={styles.financeLabel}>Agreed</Text>
          <Amount value={Number(land.agreed_price)} size="sm" />
        </View>
      </View>

      {regPending ? (
        <View style={styles.regBanner}>
          <Text style={styles.regText}>Registration pending</Text>
        </View>
      ) : null}
    </Card>
  );
}

function formatShortDate(d: string): string {
  const date = new Date(d);
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: '2-digit' });
}

const styles = StyleSheet.create({
  card: {
    marginBottom: Spacing['3'],
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing['1'],
  },
  name: {
    flex: 1,
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
    color: Colors.text,
    marginRight: Spacing['2'],
  },
  chip: {
    paddingHorizontal: Spacing['2'],
    paddingVertical: 4,
    borderRadius: Radius.sm,
  },
  chipPending: {
    backgroundColor: Colors.accent + '22',
  },
  chipSettled: {
    backgroundColor: Colors.success + '22',
  },
  chipText: {
    fontSize: FontSize.xs,
    fontWeight: FontWeight.semibold,
  },
  chipTextPending: { color: Colors.accent },
  chipTextSettled: { color: Colors.success },
  location: {
    fontSize: FontSize.sm,
    color: Colors.textSecondary,
    marginBottom: Spacing['1'],
  },
  owner: {
    fontSize: FontSize.sm,
    color: Colors.textSecondary,
    marginBottom: Spacing['2'],
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing['2'],
  },
  metaText: {
    fontSize: FontSize.xs,
    color: Colors.textMuted,
  },
  dot: {
    width: 3,
    height: 3,
    borderRadius: 999,
    backgroundColor: Colors.textMuted,
  },
  divider: {
    height: 1,
    backgroundColor: Colors.border,
    marginVertical: Spacing['3'],
  },
  financeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  financeCell: {
    flex: 1,
  },
  financeLabel: {
    fontSize: FontSize.xs,
    color: Colors.textMuted,
    marginBottom: 2,
  },
  regBanner: {
    marginTop: Spacing['3'],
    paddingHorizontal: Spacing['2'],
    paddingVertical: 6,
    backgroundColor: Colors.bgMuted,
    borderRadius: Radius.sm,
    alignSelf: 'flex-start',
  },
  regText: {
    fontSize: FontSize.xs,
    color: Colors.textSecondary,
    fontWeight: FontWeight.medium,
  },
});

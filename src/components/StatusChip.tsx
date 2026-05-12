import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Colors, FontSize, FontWeight, Radius, Spacing } from '../constants/tokens';

export type PlotDisplayStatus = 'available' | 'reserved' | 'sold_pending' | 'sold_paid' | 'blocked';

const LABEL: Record<PlotDisplayStatus, string> = {
  available: 'Available',
  reserved: 'Reserved',
  sold_pending: 'Sold · Pending',
  sold_paid: 'Sold · Paid',
  blocked: 'Blocked',
};

// plotColors from tokens
const FG: Record<PlotDisplayStatus, string> = {
  available: Colors.statusAvailable,
  reserved: Colors.statusReserved,
  sold_pending: Colors.statusSoldPending,
  sold_paid: Colors.statusSoldPaid,
  blocked: Colors.statusBlocked,
};

interface StatusChipProps {
  status: PlotDisplayStatus;
}

export function StatusChip({ status }: StatusChipProps) {
  const fg = FG[status];
  // 20% opacity hex suffix approximation
  const bg = fg + '26';

  return (
    <View style={[styles.chip, { backgroundColor: bg }]}>
      <View style={[styles.dot, { backgroundColor: fg }]} />
      <Text style={[styles.label, { color: fg }]}>{LABEL[status]}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing['2'] + 2,
    paddingVertical: Spacing['1'],
    borderRadius: Radius.full,
    alignSelf: 'flex-start',
    gap: Spacing['1'],
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  label: {
    fontSize: FontSize.xs,
    fontWeight: FontWeight.semibold,
  },
});

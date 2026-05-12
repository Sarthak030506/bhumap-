import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Colors, FontSize, Spacing } from '../constants/tokens';

interface ListRowProps {
  left: React.ReactNode;
  right?: React.ReactNode;
  chevron?: boolean;
  onPress?: () => void;
}

export function ListRow({ left, right, chevron, onPress }: ListRowProps) {
  const Container = onPress ? TouchableOpacity : View;
  const containerProps = onPress ? { activeOpacity: 0.7, onPress } : {};

  return (
    <Container style={styles.row} {...(containerProps as any)}>
      <View style={styles.left}>{left}</View>
      {(right || chevron) && (
        <View style={styles.right}>
          {right}
          {chevron && (
            <Text style={styles.chevron}>›</Text>
          )}
        </View>
      )}
    </Container>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing['3'],
    paddingHorizontal: Spacing['4'],
    minHeight: 52,
  },
  left: {
    flex: 1,
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing['2'],
    marginLeft: Spacing['3'],
  },
  chevron: {
    fontSize: FontSize.lg,
    color: Colors.textMuted,
    lineHeight: FontSize.lg * 1.2,
  },
});

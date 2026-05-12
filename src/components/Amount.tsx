import React from 'react';
import { StyleSheet, Text, TextStyle } from 'react-native';
import { Colors, FontFamily, FontSize, FontWeight } from '../constants/tokens';
import { formatINR } from '../utils/format';

type SizeKey = keyof typeof FontSize;

interface AmountProps {
  value: number | string;
  size?: SizeKey;
  color?: string;
  bold?: boolean;
  style?: TextStyle;
}

export function Amount({
  value,
  size = 'base',
  color = Colors.text,
  bold,
  style,
}: AmountProps) {
  return (
    <Text
      style={[
        styles.base,
        { fontSize: FontSize[size], color },
        bold && styles.bold,
        style,
      ]}
    >
      {formatINR(value)}
    </Text>
  );
}

const styles = StyleSheet.create({
  base: {
    fontFamily: FontFamily.mono,
    fontWeight: FontWeight.medium,
  },
  bold: {
    fontWeight: FontWeight.bold,
  },
});

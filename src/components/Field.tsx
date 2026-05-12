import React from 'react';
import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';
import { Colors, FontSize, FontWeight, Radius, Spacing } from '../constants/tokens';

interface FieldProps extends TextInputProps {
  label: string;
  error?: string;
}

export function Field({ label, error, ...rest }: FieldProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        style={[styles.input, error ? styles.inputError : styles.inputNormal]}
        placeholderTextColor={Colors.textMuted}
        {...rest}
      />
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: Spacing['4'],
  },
  label: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    color: Colors.textSecondary,
    marginBottom: Spacing['1'],
  },
  input: {
    height: 48,
    borderRadius: Radius.md,
    borderWidth: 1,
    paddingHorizontal: Spacing['3'],
    fontSize: FontSize.base,
    color: Colors.text,
    backgroundColor: Colors.bgCard,
  },
  inputNormal: {
    borderColor: Colors.border,
  },
  inputError: {
    borderColor: Colors.error,
  },
  error: {
    fontSize: FontSize.xs,
    color: Colors.error,
    marginTop: Spacing['1'],
  },
});

import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Colors, FontSize, FontWeight } from '@/constants/tokens';

export default function MapScreen() {
  return (
    <View style={styles.root}>
      <Text style={styles.text}>Map — full-screen polygon overlay coming soon</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: Colors.bgMuted, alignItems: 'center', justifyContent: 'center' },
  text: { fontSize: FontSize.base, color: Colors.textSecondary, fontWeight: FontWeight.medium },
});

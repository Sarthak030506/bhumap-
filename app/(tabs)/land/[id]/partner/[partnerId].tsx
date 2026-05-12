import React from 'react';
import { View, Text, StyleSheet, SafeAreaView, TouchableOpacity } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { Colors, FontSize, FontWeight, Spacing } from '@/constants/tokens';

export default function PartnerDetailScreen() {
  const router = useRouter();
  const { id, partnerId } = useLocalSearchParams<{ id: string; partnerId: string }>();

  return (
    <SafeAreaView style={styles.root}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()}>
          <Text style={styles.backText}>← Back</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Partner Detail</Text>
        <Text style={styles.id}>Land: {id} · Partner: {partnerId}</Text>
      </View>
      <View style={styles.placeholder}>
        <Text style={styles.placeholderText}>Partner committed / paid / remaining — coming soon</Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: Colors.bg },
  header: {
    paddingHorizontal: Spacing['5'],
    paddingVertical: Spacing['4'],
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
    gap: Spacing['1'],
  },
  backText: { fontSize: FontSize.base, color: Colors.primary, fontWeight: FontWeight.medium },
  title: { fontSize: FontSize.xl, fontWeight: FontWeight.bold, color: Colors.text },
  id: { fontSize: FontSize.sm, color: Colors.textMuted },
  placeholder: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  placeholderText: { fontSize: FontSize.base, color: Colors.textMuted },
});

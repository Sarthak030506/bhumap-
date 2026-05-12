import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  TouchableOpacity,
  FlatList,
  RefreshControl,
} from 'react-native';
import { useRouter } from 'expo-router';
import { Colors, FontSize, FontWeight, Spacing, Radius } from '@/constants/tokens';
import { useLands } from '@/hooks/useLands';
import { LandCard } from '@/components/LandCard';
import { Button } from '@/components/Button';

export default function LandListScreen() {
  const router = useRouter();
  const { data: lands, isLoading, isRefetching, refetch, error } = useLands();

  return (
    <SafeAreaView style={styles.root}>
      <View style={styles.header}>
        <Text style={styles.title}>Land</Text>
        {lands && lands.length > 0 ? (
          <Text style={styles.subtitle}>{lands.length} acquired</Text>
        ) : null}
      </View>

      {isLoading ? (
        <SkeletonList />
      ) : error ? (
        <ErrorState onRetry={() => refetch()} />
      ) : !lands || lands.length === 0 ? (
        <EmptyState onAdd={() => router.push('/(tabs)/land/add')} />
      ) : (
        <FlatList
          data={lands}
          keyExtractor={(l) => l.id}
          contentContainerStyle={styles.listContent}
          renderItem={({ item }) => (
            <LandCard
              land={item}
              onPress={() => router.push(`/(tabs)/land/${item.id}`)}
            />
          )}
          refreshControl={
            <RefreshControl
              refreshing={isRefetching}
              onRefresh={refetch}
              tintColor={Colors.primary}
            />
          }
        />
      )}

      <TouchableOpacity
        style={styles.fab}
        onPress={() => router.push('/(tabs)/land/add')}
        activeOpacity={0.85}
      >
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

function SkeletonList() {
  return (
    <View style={styles.listContent}>
      {[0, 1, 2].map((i) => (
        <View key={i} style={styles.skeletonCard}>
          <View style={[styles.skeletonLine, { width: '60%' }]} />
          <View style={[styles.skeletonLine, { width: '40%' }]} />
          <View style={[styles.skeletonLine, { width: '80%' }]} />
        </View>
      ))}
    </View>
  );
}

function EmptyState({ onAdd }: { onAdd: () => void }) {
  return (
    <View style={styles.emptyWrap}>
      <View style={styles.emptyIcon}>
        <Text style={styles.emptyIconText}>🌾</Text>
      </View>
      <Text style={styles.emptyTitle}>No lands yet</Text>
      <Text style={styles.emptyMsg}>
        Acquire your first land to start tracking partners, plots, and payments.
      </Text>
      <View style={styles.emptyAction}>
        <Button label="+ Add land" onPress={onAdd} />
      </View>
    </View>
  );
}

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <View style={styles.emptyWrap}>
      <Text style={styles.emptyTitle}>Could not load lands</Text>
      <Text style={styles.emptyMsg}>Check your connection and try again.</Text>
      <View style={styles.emptyAction}>
        <Button label="Retry" variant="secondary" onPress={onRetry} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: Colors.bg },
  header: {
    paddingHorizontal: Spacing['5'],
    paddingTop: Spacing['5'],
    paddingBottom: Spacing['3'],
  },
  title: { fontSize: FontSize['2xl'], fontWeight: FontWeight.bold, color: Colors.text },
  subtitle: {
    fontSize: FontSize.sm,
    color: Colors.textSecondary,
    marginTop: 2,
  },
  listContent: {
    paddingHorizontal: Spacing['5'],
    paddingTop: Spacing['2'],
    paddingBottom: 100,
  },
  skeletonCard: {
    backgroundColor: Colors.bgCard,
    borderRadius: Radius.lg,
    padding: Spacing['4'],
    marginBottom: Spacing['3'],
    gap: Spacing['2'],
  },
  skeletonLine: {
    height: 12,
    backgroundColor: Colors.bgMuted,
    borderRadius: 4,
  },
  emptyWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: Spacing['8'],
  },
  emptyIcon: {
    width: 72,
    height: 72,
    borderRadius: 999,
    backgroundColor: Colors.bgMuted,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing['4'],
  },
  emptyIconText: { fontSize: 32 },
  emptyTitle: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
    color: Colors.text,
    marginBottom: Spacing['2'],
  },
  emptyMsg: {
    fontSize: FontSize.sm,
    color: Colors.textSecondary,
    textAlign: 'center',
    marginBottom: Spacing['6'],
  },
  emptyAction: { width: '60%' },
  fab: {
    position: 'absolute',
    right: Spacing['5'],
    bottom: Spacing['6'],
    width: 56,
    height: 56,
    borderRadius: Radius.full,
    backgroundColor: Colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 4,
    shadowColor: Colors.primary,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
  },
  fabText: { fontSize: 28, color: Colors.textInverse, lineHeight: 32 },
});

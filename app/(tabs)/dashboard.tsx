import { useRouter } from 'expo-router';
import React, { useEffect, useRef } from 'react';
import {
  Animated,
  DimensionValue,
  ScrollView,
  StyleProp,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  ViewStyle,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Amount } from '@/components/Amount';
import { Card } from '@/components/Card';
import { Colors, FontSize, FontWeight, Radius, Spacing } from '@/constants/tokens';
import { useDashboardStats, useRecentActivity, type ActivityItem } from '@/hooks/useDashboard';
import { formatDate } from '@/utils/format';

// ─── Skeleton ───────────────────────────────────────────────

function SkeletonBox({ width, height }: { width?: DimensionValue; height: number }) {
  const opacity = useRef(new Animated.Value(0.4)).current;

  useEffect(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(opacity, { toValue: 0.9, duration: 700, useNativeDriver: true }),
        Animated.timing(opacity, { toValue: 0.4, duration: 700, useNativeDriver: true }),
      ])
    ).start();
  }, [opacity]);

  return (
    <Animated.View
      style={{
        width: width ?? '100%',
        height,
        backgroundColor: Colors.bgMuted,
        borderRadius: Radius.sm,
        opacity,
      }}
    />
  );
}

function StatsSkeleton() {
  return (
    <View style={styles.grid}>
      <Card style={styles.gridCell}>
        <SkeletonBox height={14} width={60} />
        <View style={{ marginTop: Spacing['2'] }}>
          <SkeletonBox height={28} width={40} />
        </View>
      </Card>
      <Card style={styles.gridCell}>
        <SkeletonBox height={14} width={60} />
        <View style={{ marginTop: Spacing['2'] }}>
          <SkeletonBox height={28} width={40} />
        </View>
      </Card>
      <Card style={[styles.gridCell, styles.fullWidth]}>
        <SkeletonBox height={14} width={80} />
        <View style={{ marginTop: Spacing['2'], flexDirection: 'row', gap: Spacing['6'] }}>
          <SkeletonBox height={24} width={60} />
          <SkeletonBox height={24} width={60} />
        </View>
      </Card>
      <Card style={styles.gridCell}>
        <SkeletonBox height={14} width={70} />
        <View style={{ marginTop: Spacing['2'] }}>
          <SkeletonBox height={24} width={90} />
        </View>
      </Card>
      <Card style={styles.gridCell}>
        <SkeletonBox height={14} width={70} />
        <View style={{ marginTop: Spacing['2'] }}>
          <SkeletonBox height={24} width={90} />
        </View>
      </Card>
      <Card style={[styles.gridCell, styles.fullWidth]}>
        <SkeletonBox height={14} width={100} />
        <View style={{ marginTop: Spacing['2'] }}>
          <SkeletonBox height={24} width={120} />
        </View>
      </Card>
    </View>
  );
}

function ActivitySkeleton() {
  return (
    <View style={{ gap: 1 }}>
      {[0, 1, 2, 3, 4].map((i) => (
        <View key={i} style={styles.activityRow}>
          <SkeletonBox width={36} height={36} />
          <View style={{ flex: 1, gap: Spacing['1'] }}>
            <SkeletonBox height={13} width="80%" />
            <SkeletonBox height={11} width="40%" />
          </View>
        </View>
      ))}
    </View>
  );
}

// ─── Stat Cards ─────────────────────────────────────────────

function StatCard({
  label,
  children,
  style,
}: {
  label: string;
  children: React.ReactNode;
  style?: StyleProp<ViewStyle>;
}) {
  return (
    <Card style={[styles.gridCell, style]}>
      <Text style={styles.statLabel}>{label}</Text>
      <View style={styles.statValueRow}>{children}</View>
    </Card>
  );
}

// ─── Activity Row ────────────────────────────────────────────

const ACTIVITY_ICON: Record<ActivityItem['type'], string> = {
  land_added: '⊡',
  plot_sold: '◈',
  payment_received: '₹',
  emi_overdue: '!',
};

const ACTIVITY_COLOR: Record<ActivityItem['type'], string> = {
  land_added: Colors.primary,
  plot_sold: Colors.success,
  payment_received: Colors.success,
  emi_overdue: Colors.error,
};

function ActivityRow({ item }: { item: ActivityItem }) {
  const router = useRouter();
  const color = ACTIVITY_COLOR[item.type];
  const icon = ACTIVITY_ICON[item.type];

  return (
    <TouchableOpacity
      style={styles.activityRow}
      activeOpacity={0.7}
      onPress={() => item.href && router.push(item.href as any)}
    >
      <View style={[styles.activityIcon, { backgroundColor: color + '18' }]}>
        <Text style={[styles.activityIconText, { color }]}>{icon}</Text>
      </View>
      <View style={styles.activityBody}>
        <Text style={styles.activityDesc} numberOfLines={2}>
          {item.description}
        </Text>
        <Text style={styles.activityDate}>{formatDate(item.date)}</Text>
      </View>
      <Text style={styles.chevron}>›</Text>
    </TouchableOpacity>
  );
}

// ─── Screen ─────────────────────────────────────────────────

export default function DashboardScreen() {
  const insets = useSafeAreaInsets();
  const stats = useDashboardStats();
  const activity = useRecentActivity();

  const today = formatDate(new Date());

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>BhuMap</Text>
          <Text style={styles.subtitle}>Apni Zameen, Apna Hisaab</Text>
        </View>
        <Text style={styles.dateText}>{today}</Text>
      </View>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* ── Stats ── */}
        <Text style={styles.sectionTitle}>Overview</Text>

        {stats.isError && (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>Could not load stats. Pull to retry.</Text>
          </View>
        )}

        {stats.isLoading ? (
          <StatsSkeleton />
        ) : stats.data ? (
          <View style={styles.grid}>
            {/* Row 1: Lands + Plots */}
            <StatCard label="Lands acquired" style={styles.gridCell}>
              <Text style={styles.statCount}>{stats.data.totalLands}</Text>
            </StatCard>

            <StatCard label="Total plots" style={styles.gridCell}>
              <Text style={styles.statCount}>{stats.data.totalPlots}</Text>
            </StatCard>

            {/* Row 2: Sold vs Available — full width */}
            <Card style={[styles.gridCell, styles.fullWidth]}>
              <Text style={styles.statLabel}>Plots status</Text>
              <View style={styles.splitRow}>
                <View style={styles.splitItem}>
                  <Text style={[styles.statCount, { color: Colors.statusSoldPending }]}>
                    {stats.data.soldPlots}
                  </Text>
                  <Text style={styles.splitLabel}>Sold</Text>
                </View>
                <View style={styles.splitDivider} />
                <View style={styles.splitItem}>
                  <Text style={[styles.statCount, { color: Colors.statusAvailable }]}>
                    {stats.data.availablePlots}
                  </Text>
                  <Text style={styles.splitLabel}>Available</Text>
                </View>
              </View>
            </Card>

            {/* Row 3: Received + Pending customers */}
            <StatCard label="Received from customers" style={styles.gridCell}>
              <Amount value={stats.data.totalReceived} size="md" color={Colors.success} bold />
            </StatCard>

            <StatCard label="Pending from customers" style={styles.gridCell}>
              <Amount value={stats.data.totalPending} size="md" color={Colors.accent} bold />
            </StatCard>

            {/* Row 4: Pending to farmers — full width */}
            <Card style={[styles.gridCell, styles.fullWidth]}>
              <Text style={styles.statLabel}>Pending to farmers</Text>
              <View style={styles.statValueRow}>
                <Amount value={stats.data.pendingToFarmers} size="lg" color={Colors.accent} bold />
              </View>
            </Card>
          </View>
        ) : null}

        {/* ── Activity ── */}
        <Text style={[styles.sectionTitle, { marginTop: Spacing['6'] }]}>Recent activity</Text>

        {activity.isError && (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>Could not load activity.</Text>
          </View>
        )}

        {activity.isLoading ? (
          <ActivitySkeleton />
        ) : activity.data && activity.data.length > 0 ? (
          <Card style={styles.activityCard}>
            {activity.data.map((item, idx) => (
              <View key={item.id}>
                <ActivityRow item={item} />
                {idx < activity.data!.length - 1 && <View style={styles.separator} />}
              </View>
            ))}
          </Card>
        ) : (
          <View style={styles.emptyState}>
            <Text style={styles.emptyIcon}>⊙</Text>
            <Text style={styles.emptyTitle}>No activity yet</Text>
            <Text style={styles.emptyDesc}>Add your first land to get started</Text>
          </View>
        )}

        <View style={{ height: Spacing['8'] }} />
      </ScrollView>
    </View>
  );
}

// ─── Styles ─────────────────────────────────────────────────

const GRID_GAP = Spacing['3'];

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: Colors.bg,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing['5'],
    paddingVertical: Spacing['4'],
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
    backgroundColor: Colors.bgCard,
  },
  title: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
    color: Colors.primary,
  },
  subtitle: {
    fontSize: FontSize.xs,
    color: Colors.textSecondary,
    marginTop: 1,
  },
  dateText: {
    fontSize: FontSize.sm,
    color: Colors.textMuted,
  },
  scroll: {
    flex: 1,
  },
  scrollContent: {
    padding: Spacing['4'],
  },
  sectionTitle: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
    color: Colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.6,
    marginBottom: Spacing['3'],
  },
  errorBanner: {
    backgroundColor: Colors.error + '15',
    borderRadius: Radius.sm,
    padding: Spacing['3'],
    marginBottom: Spacing['3'],
  },
  errorText: {
    fontSize: FontSize.sm,
    color: Colors.error,
  },

  // Grid
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: GRID_GAP,
  },
  gridCell: {
    flexBasis: '48%' as const,
    flexGrow: 1,
    minWidth: 140,
  },
  fullWidth: {
    flexBasis: '100%' as const,
    flexGrow: 0,
  },
  statLabel: {
    fontSize: FontSize.xs,
    color: Colors.textSecondary,
    fontWeight: FontWeight.medium,
    marginBottom: Spacing['1'],
  },
  statValueRow: {
    marginTop: Spacing['1'],
  },
  statCount: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    color: Colors.text,
  },
  splitRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing['4'],
    marginTop: Spacing['2'],
  },
  splitItem: {
    alignItems: 'center',
    flex: 1,
  },
  splitLabel: {
    fontSize: FontSize.xs,
    color: Colors.textSecondary,
    marginTop: 2,
  },
  splitDivider: {
    width: 1,
    height: 36,
    backgroundColor: Colors.border,
  },

  // Activity
  activityCard: {
    padding: 0,
    overflow: 'hidden',
  },
  activityRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing['4'],
    paddingVertical: Spacing['3'],
    gap: Spacing['3'],
  },
  activityIcon: {
    width: 36,
    height: 36,
    borderRadius: Radius.sm,
    alignItems: 'center',
    justifyContent: 'center',
  },
  activityIconText: {
    fontSize: FontSize.md,
    fontWeight: FontWeight.bold,
  },
  activityBody: {
    flex: 1,
    gap: 2,
  },
  activityDesc: {
    fontSize: FontSize.sm,
    color: Colors.text,
    fontWeight: FontWeight.medium,
  },
  activityDate: {
    fontSize: FontSize.xs,
    color: Colors.textMuted,
  },
  chevron: {
    fontSize: 20,
    color: Colors.textMuted,
    lineHeight: 24,
  },
  separator: {
    height: 1,
    backgroundColor: Colors.border,
    marginLeft: Spacing['4'] + 36 + Spacing['3'],
  },

  // Empty state
  emptyState: {
    alignItems: 'center',
    paddingVertical: Spacing['10'],
    gap: Spacing['2'],
  },
  emptyIcon: {
    fontSize: 48,
    color: Colors.textMuted,
  },
  emptyTitle: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
    color: Colors.textSecondary,
  },
  emptyDesc: {
    fontSize: FontSize.sm,
    color: Colors.textMuted,
    textAlign: 'center',
  },
});

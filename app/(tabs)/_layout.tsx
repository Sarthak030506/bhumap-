import { Tabs } from 'expo-router';
import { View, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Colors, FontSize } from '@/constants/tokens';

function TabIcon({ focused, color, size }: {
  focused: boolean;
  color: string;
  size: number;
  children?: string;
}) {
  return (
    <View style={[styles.iconWrap, focused && styles.iconActive]}>
      {/* placeholder until icon library wired */}
      <View style={{ width: size, height: size, backgroundColor: color, borderRadius: 4, opacity: 0.8 }} />
    </View>
  );
}

export default function TabLayout() {
  const insets = useSafeAreaInsets();
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: Colors.primary,
        tabBarInactiveTintColor: Colors.textMuted,
        tabBarStyle: [
          styles.bar,
          { height: 64 + insets.bottom, paddingBottom: 8 + insets.bottom },
        ],
        tabBarLabelStyle: styles.label,
        tabBarItemStyle: styles.item,
      }}
    >
      <Tabs.Screen
        name="dashboard"
        options={{
          title: 'Dashboard',
          tabBarIcon: ({ focused, color, size }) => (
            <TabIcon focused={focused} color={color} size={size}>dashboard</TabIcon>
          ),
        }}
      />
      <Tabs.Screen
        name="land"
        options={{
          title: 'Land',
          tabBarIcon: ({ focused, color, size }) => (
            <TabIcon focused={focused} color={color} size={size}>land</TabIcon>
          ),
        }}
      />
      <Tabs.Screen
        name="map"
        options={{
          title: 'Map',
          tabBarIcon: ({ focused, color, size }) => (
            <TabIcon focused={focused} color={color} size={size}>map</TabIcon>
          ),
        }}
      />
      <Tabs.Screen
        name="customers"
        options={{
          title: 'Customers',
          tabBarIcon: ({ focused, color, size }) => (
            <TabIcon focused={focused} color={color} size={size}>customers</TabIcon>
          ),
        }}
      />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  bar: {
    backgroundColor: Colors.bgCard,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
  },
  label: {
    fontSize: FontSize.xs,
    fontWeight: '500',
  },
  item: {
    paddingTop: 4,
  },
  iconWrap: {
    padding: 4,
    borderRadius: 8,
  },
  iconActive: {
    backgroundColor: Colors.bgMuted,
  },
});

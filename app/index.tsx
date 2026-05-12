import { Redirect } from 'expo-router';
import { View, ActivityIndicator, StyleSheet } from 'react-native';
import { useSessionStore } from '@/store/session';
import { Colors } from '@/constants/tokens';
import { BYPASS_AUTH_ENTRY } from '@/constants/featureFlags';

export default function RootIndex() {
  const { session, isLoading } = useSessionStore();

  if (BYPASS_AUTH_ENTRY) {
    return <Redirect href="/(tabs)/dashboard" />;
  }

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  if (session) {
    return <Redirect href="/(tabs)/dashboard" />;
  }

  return <Redirect href="/(auth)/login" />;
}

const styles = StyleSheet.create({
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.bg,
  },
});

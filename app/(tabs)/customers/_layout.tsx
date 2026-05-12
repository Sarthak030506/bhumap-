import { Stack } from 'expo-router';
import { Colors } from '@/constants/tokens';

export default function CustomersLayout() {
  return (
    <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: Colors.bg } }} />
  );
}

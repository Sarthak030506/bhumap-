import { Stack } from 'expo-router';
import { Colors } from '@/constants/tokens';

export default function LandLayout() {
  return (
    <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: Colors.bg } }} />
  );
}

export const Colors = {
  primary: '#1F6F50',
  primaryLight: '#2E9E74',
  primaryDark: '#165239',

  accent: '#C8552B',
  accentLight: '#E06843',

  bg: '#FBF7F0',
  bgCard: '#FFFFFF',
  bgMuted: '#F0EBE3',

  text: '#2A1F14',
  textSecondary: '#6B5C4E',
  textMuted: '#9E8E82',
  textInverse: '#FFFFFF',

  border: '#E5DDD5',
  borderStrong: '#C9BFB5',

  // plot status
  statusAvailable: '#16A34A',
  statusReserved: '#D97706',
  statusSoldPending: '#EA580C',
  statusSoldPaid: '#DC2626',
  statusBlocked: '#64748B',

  // semantic
  success: '#16A34A',
  warning: '#D97706',
  error: '#DC2626',
  info: '#2563EB',
} as const;

export const FontFamily = {
  sans: 'PlusJakartaSans',
  sansDevanagari: 'IBMPlexSansDevanagari',
  mono: 'JetBrainsMono',
} as const;

export const FontSize = {
  xs: 11,
  sm: 13,
  base: 15,
  md: 17,
  lg: 20,
  xl: 24,
  '2xl': 30,
  '3xl': 36,
} as const;

export const FontWeight = {
  regular: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
} as const;

export const Spacing = {
  '0': 0,
  '1': 4,
  '2': 8,
  '3': 12,
  '4': 16,
  '5': 20,
  '6': 24,
  '8': 32,
  '10': 40,
  '12': 48,
  '16': 64,
} as const;

export const Radius = {
  sm: 6,
  md: 8,
  lg: 12,
  xl: 16,
  full: 9999,
} as const;

export const Shadow = {
  card: {
    shadowColor: '#2A1F14',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.06,
    shadowRadius: 4,
    elevation: 2,
  },
  sheet: {
    shadowColor: '#2A1F14',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 8,
  },
} as const;

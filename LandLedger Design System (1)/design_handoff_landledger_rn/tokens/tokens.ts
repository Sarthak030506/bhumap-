/**
 * LandLedger Design Tokens — TypeScript
 * Drop this file into `src/theme/tokens.ts` of your React Native app.
 * All numeric values are in unitless pixels (RN convention).
 * All colors are hex strings.
 */

export const colors = {
  paper: {
    50:  '#FBF7F0',
    100: '#F5EFE3',
    200: '#EDE4D1',
  },
  line: '#E9E1D3',

  soil: {
    100: '#E7DDCB',
    300: '#B49F7F',
    500: '#6B553C',
    700: '#3F2F1E',
    900: '#2A1F14',
  },

  evergreen: {
    50:  '#E8F1EC',
    100: '#C9E0D1',
    300: '#6FB28F',
    500: '#2B8A61',
    600: '#1F6F50', // PRIMARY
    700: '#185A40',
    900: '#0D3A29',
  },

  terracotta: {
    50:  '#FBEFE7',
    100: '#F5D9C6',
    300: '#E19570',
    500: '#C8552B',
    600: '#A9441F',
    700: '#853618',
  },

  ink: {
    400: '#4E6E86',
    600: '#2E4A5E',
    900: '#162936',
  },

  status: {
    available:    '#1F8A5B',
    availableBg:  '#E3F2E9',
    availableFg:  '#0F5A3A',

    reserved:     '#C68A18',
    reservedBg:   '#FBEFD3',
    reservedFg:   '#7A5309',

    sold:         '#B23A3A',
    soldBg:       '#F7DCD9',
    soldFg:       '#7A2020',

    hold:         '#4A5568',
    holdBg:       '#E4E7EB',
    holdFg:       '#2E3744',

    overdue:      '#B23A3A',
    paid:         '#1F8A5B',
    due:          '#C68A18',
  },

  // Semantic shortcuts
  bg:           '#FBF7F0',     // paper.50
  bgSunken:     '#F5EFE3',     // paper.100
  surface:      '#FFFFFF',
  surfaceDark:  '#2A1F14',     // soil.900

  fg:           '#2A1F14',     // primary text
  fgMuted:      '#6B553C',     // secondary text
  fgSubtle:     '#B49F7F',     // captions
  fgOnDark:     '#F5EFE3',
  fgOnPrimary:  '#FFFFFF',

  primary:        '#1F6F50',
  primaryHover:   '#185A40',
  primaryPress:   '#0D3A29',
  primarySoft:    '#E8F1EC',

  accent:         '#C8552B',
  accentSoft:     '#FBEFE7',

  border:         '#E9E1D3',
  borderStrong:   '#D6C9AF',
  borderDark:     'rgba(255,255,255,0.08)',

  focusRing:      '#1F6F50',
} as const;

/**
 * Font families. The exact strings here MUST match the names you pass
 * to `expo-font`'s loadAsync — verify on first run.
 */
export const fonts = {
  sans:  'PlusJakartaSans',
  hindi: 'IBMPlexSansDevanagari',
  mono:  'JetBrainsMono',
} as const;

/**
 * Type ramp — px values (rem-equivalent at 16px root).
 */
export const fontSize = {
  xs:   12,
  sm:   13,
  base: 15,
  md:   16,
  lg:   18,
  xl:   22,
  '2xl': 28,
  '3xl': 36,
  '4xl': 44,
} as const;

export const lineHeight = {
  tight: 1.15,
  snug:  1.30,
  base:  1.45,
  relax: 1.60,
} as const;

export const fontWeight = {
  regular:  '400',
  medium:   '500',
  semibold: '600',
  bold:     '700',
} as const;

export const tracking = {
  tight:  -0.15, // ~-0.01em at 15px
  normal: 0,
  caps:   0.9,   // ~0.06em at 15px
} as const;

/** 4px grid spacing — keys match the CSS token suffixes. */
export const space = {
  0:  0,
  1:  4,
  2:  8,
  3:  12,
  4:  16,
  5:  20,
  6:  24,
  8:  32,
  10: 40,
  14: 56,
  18: 72,
} as const;

export const radius = {
  xs:   4,
  sm:   6,    // chips
  md:   10,   // buttons, inputs
  lg:   12,   // cards, sheets
  xl:   16,   // big hero cards
  pill: 999,
} as const;

/**
 * Cross-platform shadow definitions.
 * Apply with `...shadow.s1` on iOS; the `elevation` is honored on Android.
 * Shadow color is soil-900 to keep the warm-paper feel.
 */
export const shadow = {
  s0: {},
  s1: {
    shadowColor:   '#2A1F14',
    shadowOffset:  { width: 0, height: 1 },
    shadowOpacity: 0.06,
    shadowRadius:  2,
    elevation:     1,
  },
  s2: {
    shadowColor:   '#2A1F14',
    shadowOffset:  { width: 0, height: 8 },
    shadowOpacity: 0.18,
    shadowRadius:  16,
    elevation:     6,
  },
  s3: {
    shadowColor:   '#2A1F14',
    shadowOffset:  { width: 0, height: 20 },
    shadowOpacity: 0.28,
    shadowRadius:  32,
    elevation:     12,
  },
} as const;

export const motion = {
  fast: 90,
  base: 180,
  slow: 320,
  // Bezier control points — pass to Easing.bezier(...).
  easeOut:      [0.22, 1, 0.36, 1] as const,
  easeIn:       [0.42, 0, 1, 1] as const,
  easeStandard: [0.20, 0, 0, 1] as const,
} as const;

export const layout = {
  headerHeight: 56,
  tabBarHeight: 64,
  designWidth:  390, // iPhone 14 / Pro
} as const;

export const tokens = {
  colors,
  fonts,
  fontSize,
  lineHeight,
  fontWeight,
  tracking,
  space,
  radius,
  shadow,
  motion,
  layout,
};

export type Tokens = typeof tokens;
export default tokens;

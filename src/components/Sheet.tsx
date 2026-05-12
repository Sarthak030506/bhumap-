import BottomSheet, {
  BottomSheetBackdrop,
  BottomSheetBackdropProps,
  BottomSheetScrollView,
} from '@gorhom/bottom-sheet';
import React, { forwardRef, useCallback, useMemo } from 'react';
import { StyleSheet, View } from 'react-native';
import { Colors, Radius, Shadow, Spacing } from '../constants/tokens';

interface SheetProps {
  children: React.ReactNode;
  snapPoints?: (string | number)[];
  onClose?: () => void;
  scrollable?: boolean;
}

export type SheetRef = BottomSheet;

export const Sheet = forwardRef<SheetRef, SheetProps>(function Sheet(
  { children, snapPoints: snapPointsProp, onClose, scrollable = false },
  ref
) {
  const snapPoints = useMemo(() => snapPointsProp ?? ['50%', '85%'], [snapPointsProp]);

  const renderBackdrop = useCallback(
    (props: BottomSheetBackdropProps) => (
      <BottomSheetBackdrop {...props} disappearsOnIndex={-1} appearsOnIndex={0} />
    ),
    []
  );

  const Content = scrollable ? BottomSheetScrollView : View;

  return (
    <BottomSheet
      ref={ref}
      index={-1}
      snapPoints={snapPoints}
      enablePanDownToClose
      onClose={onClose}
      backdropComponent={renderBackdrop}
      backgroundStyle={styles.background}
      handleIndicatorStyle={styles.handle}
    >
      <Content style={scrollable ? styles.scrollContent : styles.content}>
        {children}
      </Content>
    </BottomSheet>
  );
});

const styles = StyleSheet.create({
  background: {
    backgroundColor: Colors.bgCard,
    borderTopLeftRadius: Radius.xl,
    borderTopRightRadius: Radius.xl,
    ...Shadow.sheet,
  },
  handle: {
    backgroundColor: Colors.border,
    width: 36,
    height: 4,
  },
  content: {
    flex: 1,
    paddingHorizontal: Spacing['4'],
    paddingBottom: Spacing['8'],
  },
  scrollContent: {
    paddingHorizontal: Spacing['4'],
    paddingBottom: Spacing['8'],
  },
});

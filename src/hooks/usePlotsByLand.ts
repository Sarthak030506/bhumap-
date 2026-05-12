import { useQuery } from '@tanstack/react-query';
import { supabase } from '@/lib/supabase';

export type PlotDisplayStatus =
  | 'available'
  | 'reserved'
  | 'sold_pending'
  | 'sold_paid'
  | 'blocked';

export interface PlotRow {
  id: string;
  land_id: string;
  plot_number: string;
  area_sqft: number;
  area_unit: 'sqft' | 'guntha' | 'acre';
  base_price: number | null;
  status: 'available' | 'reserved' | 'sold' | 'blocked';
  display_status: PlotDisplayStatus;
  status_color: string;
  boundary_coordinates: { lat: number; lng: number }[] | null;
}

export const plotKeys = {
  byLand: (landId: string) => ['lands', landId, 'plots'] as const,
};

export function usePlotsByLand(landId: string | undefined) {
  return useQuery<PlotRow[]>({
    queryKey: landId ? plotKeys.byLand(landId) : ['lands', 'unknown', 'plots'],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('plots_view')
        .select('*')
        .eq('land_id', landId!)
        .order('plot_number', { ascending: true });
      if (error) throw error;
      return data as PlotRow[];
    },
    enabled: !!landId,
  });
}

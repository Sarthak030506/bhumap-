import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { supabase } from '@/lib/supabase';
import { PartnerInput } from '@/lib/validators';
import { landKeys } from './useLands';

export interface Partner {
  id: string;
  land_id: string;
  name: string;
  phone: string | null;
  email: string | null;
  ownership_percent: number;
  committed_amount: number;
  total_paid: number;
  remaining: number;
  notes: string | null;
  created_at: string;
  updated_at: string;
}

export const partnerKeys = {
  byLand: (landId: string) => ['lands', landId, 'partners'] as const,
  byId: (partnerId: string) => ['partners', partnerId] as const,
};

export function usePartnersByLand(landId: string | undefined) {
  return useQuery<Partner[]>({
    queryKey: landId ? partnerKeys.byLand(landId) : ['lands', 'unknown', 'partners'],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('partners')
        .select('*')
        .eq('land_id', landId!)
        .order('created_at', { ascending: true });
      if (error) throw error;
      return data as Partner[];
    },
    enabled: !!landId,
  });
}

export function usePartner(partnerId: string | undefined) {
  return useQuery<Partner>({
    queryKey: partnerId ? partnerKeys.byId(partnerId) : ['partners', 'unknown'],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('partners')
        .select('*')
        .eq('id', partnerId!)
        .single();
      if (error) throw error;
      return data as Partner;
    },
    enabled: !!partnerId,
  });
}

export function useCreatePartner(landId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: PartnerInput) => {
      const { data, error } = await supabase
        .from('partners')
        .insert({
          land_id: landId,
          name: input.name,
          phone: input.phone || null,
          ownership_percent: input.ownership_percent,
          committed_amount: input.committed_amount,
        })
        .select()
        .single();
      if (error) throw error;
      return data as Partner;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: partnerKeys.byLand(landId) });
      qc.invalidateQueries({ queryKey: landKeys.byId(landId) });
    },
  });
}

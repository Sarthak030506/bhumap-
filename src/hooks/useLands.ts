import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { supabase } from '@/lib/supabase';
import { LandInput } from '@/lib/validators';
import { toSqft } from '@/utils/format';

export type AreaUnit = 'sqft' | 'guntha' | 'acre';
export type RegistrationStatus = 'pending' | 'registered';

export interface Land {
  id: string;
  admin_id: string;
  name: string;
  gat_number: string | null;
  village: string | null;
  taluka: string | null;
  district: string;
  total_area_sqft: number;
  area_unit: AreaUnit;
  owner_name: string | null;
  owner_phone: string | null;
  agreed_price: number;
  total_paid: number;
  remaining_to_owner: number;
  boundary_coordinates: { lat: number; lng: number }[] | null;
  document_urls: { type: string; url: string; name: string }[];
  acquisition_date: string;
  registration_status: RegistrationStatus;
  notes: string | null;
  created_at: string;
  updated_at: string;
}

export const landKeys = {
  all: ['lands'] as const,
  list: () => [...landKeys.all, 'list'] as const,
  byId: (id: string) => [...landKeys.all, 'detail', id] as const,
};

export function useLands() {
  return useQuery<Land[]>({
    queryKey: landKeys.list(),
    queryFn: async () => {
      const { data, error } = await supabase
        .from('lands')
        .select('*')
        .order('created_at', { ascending: false });
      if (error) throw error;
      return data as Land[];
    },
  });
}

export function useLand(id: string | undefined) {
  return useQuery<Land>({
    queryKey: id ? landKeys.byId(id) : ['lands', 'detail', 'unknown'],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('lands')
        .select('*')
        .eq('id', id!)
        .single();
      if (error) throw error;
      return data as Land;
    },
    enabled: !!id,
  });
}

export function useCreateLand() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: LandInput) => {
      const { data: userRes, error: authError } = await supabase.auth.getUser();
      if (authError) throw authError;
      const adminId = userRes.user?.id;
      if (!adminId) throw new Error('Not signed in');

      const totalAreaSqft = toSqft(input.total_area, input.area_unit);
      const advance = input.advance_paid ?? 0;

      const { data: land, error } = await supabase
        .from('lands')
        .insert({
          admin_id: adminId,
          name: input.name,
          gat_number: input.gat_number || null,
          village: input.village,
          taluka: input.taluka,
          district: input.district,
          total_area_sqft: totalAreaSqft,
          area_unit: input.area_unit,
          owner_name: input.owner_name,
          owner_phone: input.owner_phone,
          agreed_price: input.agreed_price,
          acquisition_date: input.acquisition_date,
          registration_status: input.registration_status,
          boundary_coordinates: input.boundary_coordinates ?? null,
          notes: input.notes || null,
        })
        .select()
        .single();
      if (error) throw error;

      if (advance > 0) {
        const { error: payErr } = await supabase.from('land_payments').insert({
          land_id: land.id,
          amount: advance,
          payment_date: input.acquisition_date,
          payment_method: 'cash',
          notes: 'Advance paid at acquisition',
        });
        if (payErr) throw payErr;
      }

      return land as Land;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: landKeys.all });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

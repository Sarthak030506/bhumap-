import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { supabase } from '@/lib/supabase';
import { PaymentInput } from '@/lib/validators';
import { landKeys } from './useLands';

export type PaymentMethod = 'cash' | 'upi' | 'cheque' | 'bank_transfer';

export interface LandPayment {
  id: string;
  land_id: string;
  amount: number;
  payment_date: string;
  payment_method: PaymentMethod;
  notes: string | null;
  created_at: string;
}

export const landPaymentKeys = {
  byLand: (landId: string) => ['lands', landId, 'payments'] as const,
};

export function useLandPayments(landId: string | undefined) {
  return useQuery<LandPayment[]>({
    queryKey: landId ? landPaymentKeys.byLand(landId) : ['lands', 'unknown', 'payments'],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('land_payments')
        .select('*')
        .eq('land_id', landId!)
        .order('payment_date', { ascending: false });
      if (error) throw error;
      return data as LandPayment[];
    },
    enabled: !!landId,
  });
}

export function useCreateLandPayment(landId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: PaymentInput) => {
      const noteParts: string[] = [];
      if (input.method === 'cheque' && input.cheque_number) {
        noteParts.push(`Cheque #${input.cheque_number}`);
      }
      if ((input.method === 'upi' || input.method === 'bank_transfer') && input.utr_number) {
        noteParts.push(`UTR ${input.utr_number}`);
      }
      if (input.notes) noteParts.push(input.notes);

      const { data, error } = await supabase
        .from('land_payments')
        .insert({
          land_id: landId,
          amount: input.amount,
          payment_date: input.payment_date,
          payment_method: input.method,
          notes: noteParts.join(' • ') || null,
        })
        .select()
        .single();
      if (error) throw error;
      return data as LandPayment;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: landPaymentKeys.byLand(landId) });
      qc.invalidateQueries({ queryKey: landKeys.byId(landId) });
      qc.invalidateQueries({ queryKey: landKeys.list() });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

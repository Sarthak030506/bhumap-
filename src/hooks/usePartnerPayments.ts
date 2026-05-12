import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { supabase } from '@/lib/supabase';
import { PaymentInput } from '@/lib/validators';
import { PaymentMethod } from './useLandPayments';
import { partnerKeys } from './usePartners';

export interface PartnerPayment {
  id: string;
  partner_id: string;
  amount: number;
  payment_date: string;
  payment_method: PaymentMethod;
  notes: string | null;
  created_at: string;
}

export const partnerPaymentKeys = {
  byPartner: (partnerId: string) => ['partners', partnerId, 'payments'] as const,
};

export function usePartnerPayments(partnerId: string | undefined) {
  return useQuery<PartnerPayment[]>({
    queryKey: partnerId
      ? partnerPaymentKeys.byPartner(partnerId)
      : ['partners', 'unknown', 'payments'],
    queryFn: async () => {
      const { data, error } = await supabase
        .from('partner_payments')
        .select('*')
        .eq('partner_id', partnerId!)
        .order('payment_date', { ascending: false });
      if (error) throw error;
      return data as PartnerPayment[];
    },
    enabled: !!partnerId,
  });
}

export function useCreatePartnerPayment(partnerId: string, landId?: string) {
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
        .from('partner_payments')
        .insert({
          partner_id: partnerId,
          amount: input.amount,
          payment_date: input.payment_date,
          payment_method: input.method,
          notes: noteParts.join(' • ') || null,
        })
        .select()
        .single();
      if (error) throw error;
      return data as PartnerPayment;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: partnerPaymentKeys.byPartner(partnerId) });
      qc.invalidateQueries({ queryKey: partnerKeys.byId(partnerId) });
      if (landId) qc.invalidateQueries({ queryKey: partnerKeys.byLand(landId) });
    },
  });
}

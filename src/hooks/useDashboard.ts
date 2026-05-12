import { useQuery } from '@tanstack/react-query';
import { formatINR } from '@/utils/format';
import { supabase } from '@/lib/supabase';

export interface DashboardStats {
  totalLands: number;
  totalPlots: number;
  soldPlots: number;
  availablePlots: number;
  totalReceived: number;
  totalPending: number;
  pendingToFarmers: number;
}

export interface ActivityItem {
  id: string;
  type: 'land_added' | 'plot_sold' | 'payment_received' | 'emi_overdue';
  description: string;
  date: string;
  href: string;
}

export function useDashboardStats() {
  return useQuery<DashboardStats>({
    queryKey: ['dashboard', 'stats'],
    queryFn: async () => {
      const [landsRes, plotsRes, salesRes, landsFinRes] = await Promise.all([
        supabase.from('lands').select('*', { count: 'exact', head: true }),
        supabase.from('plots').select('status'),
        supabase.from('sales').select('total_paid, pending_amount').eq('status', 'active'),
        supabase.from('lands').select('remaining_to_owner'),
      ]);

      if (landsRes.error) throw landsRes.error;
      if (plotsRes.error) throw plotsRes.error;
      if (salesRes.error) throw salesRes.error;
      if (landsFinRes.error) throw landsFinRes.error;

      const plots = plotsRes.data ?? [];
      const sales = salesRes.data ?? [];
      const lands = landsFinRes.data ?? [];

      return {
        totalLands: landsRes.count ?? 0,
        totalPlots: plots.length,
        soldPlots: plots.filter((p) => p.status === 'sold').length,
        availablePlots: plots.filter((p) => p.status === 'available').length,
        totalReceived: sales.reduce((sum, s) => sum + Number(s.total_paid), 0),
        totalPending: sales.reduce((sum, s) => sum + Number(s.pending_amount), 0),
        pendingToFarmers: lands.reduce((sum, l) => sum + Number(l.remaining_to_owner), 0),
      };
    },
  });
}

export function useRecentActivity() {
  return useQuery<ActivityItem[]>({
    queryKey: ['dashboard', 'activity'],
    queryFn: async () => {
      const [landsRes, salesRes, paymentsRes, emiRes] = await Promise.all([
        supabase
          .from('lands')
          .select('id, name, created_at')
          .order('created_at', { ascending: false })
          .limit(5),
        supabase
          .from('sales')
          .select('id, created_at, customer_id, customers(name), plots(plot_number)')
          .order('created_at', { ascending: false })
          .limit(5),
        supabase
          .from('sale_payments')
          .select('id, created_at, amount, sales(customer_id, customers(name))')
          .order('created_at', { ascending: false })
          .limit(5),
        supabase
          .from('emi_schedule')
          .select('id, due_date, sales(id, customer_id, customers(name))')
          .eq('status', 'overdue')
          .order('due_date', { ascending: false })
          .limit(5),
      ]);

      const items: ActivityItem[] = [];

      for (const l of landsRes.data ?? []) {
        items.push({
          id: `land_${l.id}`,
          type: 'land_added',
          description: `Land added: ${l.name}`,
          date: l.created_at,
          href: `/(tabs)/land/${l.id}`,
        });
      }

      for (const s of salesRes.data ?? []) {
        const customer = (s.customers as any)?.name ?? 'Unknown';
        const plot = (s.plots as any)?.plot_number ?? '';
        items.push({
          id: `sale_${s.id}`,
          type: 'plot_sold',
          description: `Plot ${plot} sold to ${customer}`,
          date: s.created_at,
          href: `/(tabs)/customers/${s.customer_id}`,
        });
      }

      for (const p of paymentsRes.data ?? []) {
        const sale = p.sales as any;
        const customer = sale?.customers?.name ?? 'Unknown';
        items.push({
          id: `pay_${p.id}`,
          type: 'payment_received',
          description: `${formatINR(p.amount)} received from ${customer}`,
          date: p.created_at,
          href: `/(tabs)/customers/${sale?.customer_id ?? ''}`,
        });
      }

      for (const e of emiRes.data ?? []) {
        const sale = e.sales as any;
        const customer = sale?.customers?.name ?? 'Unknown';
        items.push({
          id: `emi_${e.id}`,
          type: 'emi_overdue',
          description: `EMI overdue for ${customer}`,
          date: e.due_date,
          href: `/(tabs)/customers/${sale?.customer_id ?? ''}`,
        });
      }

      return items
        .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
        .slice(0, 10);
    },
  });
}

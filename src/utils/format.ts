import { format } from 'date-fns';

export function formatINR(amount: number | string): string {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(num)) return '₹0';
  return (
    '₹' +
    num.toLocaleString('en-IN', {
      maximumFractionDigits: 0,
      minimumFractionDigits: 0,
    })
  );
}

export function formatDate(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return format(d, 'dd MMM yyyy');
}

export type AreaUnit = 'sqft' | 'guntha' | 'acre';

const SQFT_PER_UNIT: Record<AreaUnit, number> = {
  sqft: 1,
  guntha: 1089,
  acre: 43560,
};

export function toSqft(value: number, unit: AreaUnit): number {
  return value * SQFT_PER_UNIT[unit];
}

export function fromSqft(sqft: number, unit: AreaUnit): number {
  return sqft / SQFT_PER_UNIT[unit];
}

export function formatArea(sqft: number, unit: AreaUnit): string {
  const v = fromSqft(sqft, unit);
  return `${v.toLocaleString('en-IN', { maximumFractionDigits: 2 })} ${unit}`;
}

export function formatPhone(phone: string): string {
  const digits = phone.replace(/\D/g, '');
  if (digits.length === 10) return `+91 ${digits.slice(0, 5)} ${digits.slice(5)}`;
  return phone;
}

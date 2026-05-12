import { z } from 'zod';

const phoneRegex = /^[6-9]\d{9}$/;

const moneyString = z
  .string()
  .min(1, 'Required')
  .refine((v) => !isNaN(Number(v.replace(/,/g, ''))), 'Must be a number')
  .transform((v) => Number(v.replace(/,/g, '')))
  .pipe(z.number().nonnegative('Must be ≥ 0'));

const positiveNumberString = z
  .string()
  .min(1, 'Required')
  .refine((v) => !isNaN(Number(v.replace(/,/g, ''))), 'Must be a number')
  .transform((v) => Number(v.replace(/,/g, '')))
  .pipe(z.number().positive('Must be > 0'));

export const LandSchema = z
  .object({
    name: z.string().min(2, 'Land name required'),
    owner_name: z.string().min(2, 'Owner name required'),
    owner_phone: z.string().regex(phoneRegex, 'Enter a valid 10-digit mobile'),
    gat_number: z.string().optional().or(z.literal('')),
    village: z.string().min(1, 'Village required'),
    taluka: z.string().min(1, 'Taluka required'),
    district: z.string().min(1, 'District required'),
    total_area: positiveNumberString,
    area_unit: z.enum(['sqft', 'guntha', 'acre']),
    agreed_price: moneyString,
    advance_paid: moneyString.optional(),
    acquisition_date: z.string().min(1, 'Date required'),
    registration_status: z.enum(['pending', 'registered']),
    boundary_coordinates: z.array(z.object({ lat: z.number(), lng: z.number() })).optional(),
    notes: z.string().optional(),
  })
  .refine(
    (d) => (d.advance_paid ?? 0) <= d.agreed_price,
    { message: 'Advance cannot exceed agreed price', path: ['advance_paid'] }
  );

export type LandInput = z.infer<typeof LandSchema>;

export const PartnerSchema = z.object({
  name: z.string().min(2, 'Name required'),
  phone: z.string().regex(phoneRegex, 'Enter a valid 10-digit mobile').optional().or(z.literal('')),
  ownership_percent: positiveNumberString.pipe(
    z.number().max(100, 'Cannot exceed 100%'),
  ),
  committed_amount: moneyString,
});

export type PartnerInput = z.infer<typeof PartnerSchema>;

export const PaymentSchema = z
  .object({
    amount: positiveNumberString,
    payment_date: z.string().min(1, 'Date required'),
    method: z.enum(['cash', 'upi', 'cheque', 'bank_transfer']),
    cheque_number: z.string().optional(),
    utr_number: z.string().optional(),
    notes: z.string().optional(),
  })
  .superRefine((d, ctx) => {
    if (d.method === 'cheque' && !d.cheque_number?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['cheque_number'],
        message: 'Cheque number required',
      });
    }
    if ((d.method === 'upi' || d.method === 'bank_transfer') && !d.utr_number?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['utr_number'],
        message: 'UTR / reference required',
      });
    }
  });

export type PaymentInput = z.infer<typeof PaymentSchema>;

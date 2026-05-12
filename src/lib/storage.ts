import { supabase } from './supabase';

const MAX_FILE_BYTES = 10 * 1024 * 1024; // 10 MB

export type UploadAsset = {
  uri: string;
  name: string;
  mimeType?: string;
  size?: number;
};

export type UploadResult = { path: string };

async function uriToBlob(uri: string): Promise<Blob> {
  const res = await fetch(uri);
  return await res.blob();
}

function assertSize(asset: UploadAsset) {
  if (asset.size && asset.size > MAX_FILE_BYTES) {
    throw new Error('File too large (max 10 MB)');
  }
}

function safeName(name: string) {
  return name.replace(/[^a-zA-Z0-9._-]/g, '_');
}

/**
 * Upload a 7/12 or sale-agreement document for a land.
 * Path shape: <land_id>/docs/<timestamp>-<filename>
 */
export async function uploadLandDocument(
  landId: string,
  asset: UploadAsset,
): Promise<UploadResult> {
  assertSize(asset);
  const path = `${landId}/docs/${Date.now()}-${safeName(asset.name)}`;
  const blob = await uriToBlob(asset.uri);
  const { error } = await supabase.storage
    .from('land-documents')
    .upload(path, blob, {
      contentType: asset.mimeType ?? 'application/octet-stream',
      upsert: false,
    });
  if (error) throw error;
  return { path };
}

/**
 * Upload a payment receipt photo. Lives under same bucket as parent entity.
 */
export async function uploadLandPaymentReceipt(
  landId: string,
  paymentId: string,
  asset: UploadAsset,
): Promise<UploadResult> {
  assertSize(asset);
  const path = `${landId}/receipts/${paymentId}-${safeName(asset.name)}`;
  const blob = await uriToBlob(asset.uri);
  const { error } = await supabase.storage
    .from('land-documents')
    .upload(path, blob, {
      contentType: asset.mimeType ?? 'image/jpeg',
      upsert: false,
    });
  if (error) throw error;
  return { path };
}

export async function uploadPartnerPaymentReceipt(
  landId: string,
  partnerId: string,
  paymentId: string,
  asset: UploadAsset,
): Promise<UploadResult> {
  assertSize(asset);
  const path = `${landId}/partner-receipts/${partnerId}/${paymentId}-${safeName(asset.name)}`;
  const blob = await uriToBlob(asset.uri);
  const { error } = await supabase.storage
    .from('land-documents')
    .upload(path, blob, {
      contentType: asset.mimeType ?? 'image/jpeg',
      upsert: false,
    });
  if (error) throw error;
  return { path };
}

export function getPublicUrl(bucket: string, path: string): string {
  const { data } = supabase.storage.from(bucket).getPublicUrl(path);
  return data.publicUrl;
}

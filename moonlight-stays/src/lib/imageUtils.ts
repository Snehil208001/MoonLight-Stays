/**
 * Extracts the direct image URL when user pastes a Google Images link.
 * Google returns URLs like: https://www.google.com/imgres?imgurl=https%3A%2F%2Fexample.com%2Fimage.jpg
 * We need the actual image URL from the imgurl parameter.
 */
export function extractDirectImageUrl(input: string): string {
  const trimmed = input?.trim() || "";
  if (!trimmed) return "";
  try {
    if (/google\.(com|co\.[a-z]+)\/imgres/i.test(trimmed)) {
      const url = new URL(trimmed);
      const imgurl = url.searchParams.get("imgurl");
      if (imgurl) return imgurl;
    }
  } catch {
    /* ignore parse errors */
  }
  return trimmed;
}

export const DEFAULT_HOTEL_IMAGE = "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=800&auto=format&fit=crop";
export const DEFAULT_ROOM_IMAGE = "https://images.unsplash.com/photo-1590490360182-c33d57733427?q=80&w=800&auto=format&fit=crop";

const NEXT_PUBLIC_API_URL = process.env.NEXT_PUBLIC_API_URL || (process.env.NODE_ENV === 'production'
  ? 'https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1'
  : 'http://localhost:8080/api/v1');

/**
 * Resolves a photo URL for display. Handles:
 * - Full URLs (http/https) - use as-is
 * - Relative or absolute paths (/images/xyz or images/xyz) - prepend API base URL
 */
export function getImageSrc(url: string, defaultType: 'hotel' | 'room' = 'hotel'): string {
  if (!url?.trim()) return defaultType === 'room' ? DEFAULT_ROOM_IMAGE : DEFAULT_HOTEL_IMAGE;
  const u = url.trim();
  if (u.startsWith("http://") || u.startsWith("https://")) return u;
  
  // Check if it's a dummy text value (doesn't contain extension format)
  const hasExtension = /\.(jpg|jpeg|png|webp|gif|svg)$/i.test(u);
  if (!hasExtension) {
    return defaultType === 'room' ? DEFAULT_ROOM_IMAGE : DEFAULT_HOTEL_IMAGE;
  }

  const cleanUrl = u.startsWith("/") ? u : `/${u}`;
  const apiBase = NEXT_PUBLIC_API_URL.endsWith('/') ? NEXT_PUBLIC_API_URL.slice(0, -1) : NEXT_PUBLIC_API_URL;
  return `${apiBase}${cleanUrl}`;
}

/**
 * Get the first photo URL from an array, or null if none.
 */
export function getFirstImageSrc(photos?: string[], defaultType: 'hotel' | 'room' = 'hotel'): string | null {
  if (!photos?.length) return defaultType === 'room' ? DEFAULT_ROOM_IMAGE : DEFAULT_HOTEL_IMAGE;
  const src = getImageSrc(photos[0], defaultType);
  return src || null;
}

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

const NEXT_PUBLIC_API_URL = process.env.NEXT_PUBLIC_API_URL || (process.env.NODE_ENV === 'production'
  ? 'https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1'
  : 'http://localhost:8080/api/v1');

/**
 * Resolves a photo URL for display. Handles:
 * - Full URLs (http/https) - use as-is
 * - Relative or absolute paths (/images/xyz or images/xyz) - prepend API base URL
 */
export function getImageSrc(url: string): string {
  if (!url?.trim()) return "";
  const u = url.trim();
  if (u.startsWith("http://") || u.startsWith("https://")) return u;
  const cleanUrl = u.startsWith("/") ? u : `/${u}`;
  const apiBase = NEXT_PUBLIC_API_URL.endsWith('/') ? NEXT_PUBLIC_API_URL.slice(0, -1) : NEXT_PUBLIC_API_URL;
  return `${apiBase}${cleanUrl}`;
}

/**
 * Get the first photo URL from an array, or null if none.
 */
export function getFirstImageSrc(photos?: string[]): string | null {
  if (!photos?.length) return null;
  const src = getImageSrc(photos[0]);
  return src || null;
}

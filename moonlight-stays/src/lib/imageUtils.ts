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

/** Backend origin when using remote API (e.g. Elastic Beanstalk) */
function getBackendOrigin(): string {
  const apiUrl = typeof window !== "undefined" ? process.env.NEXT_PUBLIC_API_URL : undefined;
  if (apiUrl && (apiUrl.startsWith("http://") || apiUrl.startsWith("https://"))) {
    try {
      return new URL(apiUrl).origin;
    } catch {
      return "";
    }
  }
  return "";
}

/**
 * Resolves a photo URL for display. Handles:
 * - Full URLs (http/https) - use as-is
 * - Absolute paths (/images/xyz) - prepend backend origin when using remote API
 * - Relative/filenames (xyz) - prepend /images/ (or full URL when remote)
 */
export function getImageSrc(url: string): string {
  if (!url?.trim()) return "";
  const u = url.trim();
  if (u.startsWith("http://") || u.startsWith("https://")) return u;
  const origin = getBackendOrigin();
  const path = u.startsWith("/") ? u : `/images/${u}`;
  return origin ? `${origin}${path}` : path;
}

/**
 * Get the first photo URL from an array, or null if none.
 */
export function getFirstImageSrc(photos?: string[]): string | null {
  if (!photos?.length) return null;
  const src = getImageSrc(photos[0]);
  return src || null;
}

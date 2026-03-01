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

/**
 * Resolves a photo URL for display. Handles:
 * - Full URLs (http/https) - use as-is
 * - Absolute paths (/images/xyz) - use as-is (Next.js rewrites proxy to backend)
 * - Relative/filenames (xyz) - normalize to /images/xyz
 */
export function getImageSrc(url: string): string {
  if (!url?.trim()) return "";
  const u = url.trim();
  if (u.startsWith("http://") || u.startsWith("https://")) return u;
  return u.startsWith("/") ? u : `/images/${u}`;
}

/**
 * Get the first photo URL from an array, or null if none.
 */
export function getFirstImageSrc(photos?: string[]): string | null {
  if (!photos?.length) return null;
  const src = getImageSrc(photos[0]);
  return src || null;
}

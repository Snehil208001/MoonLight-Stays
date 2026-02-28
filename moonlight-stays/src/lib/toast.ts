import toast from "react-hot-toast";

/** Show error toast from API/caught error. Extracts message consistently. */
export function showApiError(err: unknown): string {
  const msg = err instanceof Error ? err.message : "Something went wrong";
  toast.error(msg);
  return msg;
}

/** Show success toast. */
export function showSuccess(message: string): void {
  toast.success(message);
}

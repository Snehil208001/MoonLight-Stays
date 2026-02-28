"use client";

import { useEffect } from "react";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div
      className="min-h-screen flex flex-col items-center justify-center p-8"
      style={{ background: "#0a0a1a", color: "#fff" }}
    >
      <h1 className="text-xl font-bold mb-4">Something went wrong</h1>
      <p className="text-white/70 mb-6 text-center max-w-md">
        The app encountered an error. Try refreshing the page.
      </p>
      <button
        onClick={() => reset()}
        className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-medium hover:bg-[#00FFFF]/30"
      >
        Try again
      </button>
    </div>
  );
}

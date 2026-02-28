"use client";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="en" className="dark">
      <body style={{ background: "#0a0a1a", color: "#fff", minHeight: "100vh", margin: 0, fontFamily: "system-ui, sans-serif" }}>
        <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "2rem" }}>
          <h1 style={{ fontSize: "1.25rem", fontWeight: 700, marginBottom: "1rem" }}>Something went wrong</h1>
          <p style={{ color: "rgba(255,255,255,0.7)", marginBottom: "1.5rem", textAlign: "center", maxWidth: "28rem" }}>
            The app encountered a critical error. Try refreshing the page.
          </p>
          <button
            onClick={() => reset()}
            style={{
              padding: "0.5rem 1rem",
              borderRadius: "0.75rem",
              background: "rgba(0,255,255,0.2)",
              border: "1px solid rgba(0,255,255,0.5)",
              color: "#00FFFF",
              fontWeight: 500,
              cursor: "pointer",
            }}
          >
            Try again
          </button>
        </div>
      </body>
    </html>
  );
}

import type { Config } from "tailwindcss";

// Design tokens — canonical source: ../design-system/tokens.json
// Keep in sync with Android: app/src/main/java/.../ui/theme/
const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["var(--font-plus-jakarta)", "Inter", "system-ui", "sans-serif"],
      },
      colors: {
        accent: {
          cyan: "#00FFFF",
          coral: "#FF7F50",
        },
        midnight: {
          950: "#0a0a1a",
          900: "#0f0f23",
          800: "#15152e",
          700: "#1a1a3e",
        },
        success: "#00E479",
        warning: "#FFC857",
        error: "#FF4D6D",
        rating: "#FFC857",
        glass: {
          DEFAULT: "rgba(255,255,255,0.05)",
          strong: "rgba(255,255,255,0.08)",
          hover: "rgba(255,255,255,0.10)",
          border: "rgba(255,255,255,0.10)",
          "border-strong": "rgba(255,255,255,0.20)",
        },
      },
      transitionDuration: {
        fast: "150ms",
        base: "250ms",
        slow: "400ms",
        screen: "500ms",
      },
      transitionTimingFunction: {
        standard: "cubic-bezier(0.2, 0, 0, 1)",
        decelerate: "cubic-bezier(0, 0, 0, 1)",
        accelerate: "cubic-bezier(0.3, 0, 1, 1)",
      },
      boxShadow: {
        "elevation-1": "0 2px 8px rgba(0,0,0,0.30)",
        "elevation-2": "0 8px 24px rgba(0,0,0,0.35)",
        "elevation-3": "0 16px 48px rgba(0,0,0,0.45)",
        "glow-cyan": "0 0 20px rgba(0,255,255,0.30), 0 0 40px rgba(0,255,255,0.20)",
        "glow-coral": "0 0 20px rgba(255,127,80,0.30)",
      },
      animation: {
        "mesh-gradient": "mesh-gradient 15s ease infinite",
        "float": "float 6s ease-in-out infinite",
        "skeleton": "skeleton-pulse 1.5s ease-in-out infinite",
      },
      keyframes: {
        "mesh-gradient": {
          "0%, 100%": { backgroundPosition: "0% 50%" },
          "50%": { backgroundPosition: "100% 50%" },
        },
        float: {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-12px)" },
        },
        "skeleton-pulse": {
          "0%, 100%": { opacity: "0.4" },
          "50%": { opacity: "0.7" },
        },
      },
      backdropBlur: {
        xs: "2px",
      },
    },
  },
  plugins: [],
};

export default config;

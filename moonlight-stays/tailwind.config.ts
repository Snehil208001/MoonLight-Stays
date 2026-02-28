import type { Config } from "tailwindcss";

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
      },
      animation: {
        "mesh-gradient": "mesh-gradient 15s ease infinite",
        "float": "float 6s ease-in-out infinite",
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
      },
      backdropBlur: {
        xs: "2px",
      },
    },
  },
  plugins: [],
};

export default config;

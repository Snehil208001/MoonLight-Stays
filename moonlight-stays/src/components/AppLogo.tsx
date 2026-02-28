"use client";

import { motion } from "framer-motion";

interface AppLogoProps {
  size?: "sm" | "md" | "lg";
  showGlow?: boolean;
  className?: string;
}

const sizes = {
  sm: 48,
  md: 80,
  lg: 120,
};

export function AppLogo({ size = "md", showGlow = true, className = "" }: AppLogoProps) {
  const s = sizes[size];

  return (
    <motion.div
      className={`relative inline-flex items-center justify-center ${className}`}
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.6, ease: "easeOut" }}
    >
      {showGlow && (
        <div
          className="absolute inset-0 rounded-full blur-2xl opacity-40"
          style={{
            width: s * 1.8,
            height: s * 1.8,
            background: "radial-gradient(circle, rgba(0,255,255,0.4) 0%, transparent 70%)",
          }}
        />
      )}
      <svg
        width={s}
        height={s}
        viewBox="0 0 120 120"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="relative drop-shadow-lg"
      >
        {/* Frosted glass crescent moon */}
        <defs>
          <linearGradient id="moonGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="rgba(255,255,255,0.9)" />
            <stop offset="100%" stopColor="rgba(200,220,255,0.7)" />
          </linearGradient>
          <filter id="glow">
            <feGaussianBlur stdDeviation="2" result="coloredBlur" />
            <feMerge>
              <feMergeNode in="coloredBlur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
          <filter id="innerGlow">
            <feGaussianBlur stdDeviation="1" result="blur" />
            <feFlood floodColor="#00FFFF" floodOpacity="0.5" />
            <feComposite in2="blur" operator="in" />
            <feMerge>
              <feMergeNode />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        {/* Crescent - outer arc */}
        <path
          d="M60 20 A45 45 0 1 1 60 100 A45 45 0 1 1 60 20"
          fill="url(#moonGrad)"
          opacity="0.95"
          filter="url(#glow)"
        />
        {/* Crescent - inner cutout */}
        <path
          d="M60 35 A30 30 0 1 1 60 85 A30 30 0 1 1 60 35"
          fill="#0a0a1a"
          opacity="0.6"
        />

        {/* Minimalist house/door shape - geometric */}
        <g transform="translate(52, 45)" filter="url(#innerGlow)">
          <path
            d="M8 0 L16 8 L16 24 L0 24 L0 8 Z"
            fill="none"
            stroke="rgba(0,255,255,0.9)"
            strokeWidth="2"
            strokeLinejoin="round"
          />
          <rect x="5" y="12" width="6" height="8" fill="rgba(0,255,255,0.3)" stroke="rgba(0,255,255,0.6)" strokeWidth="1" />
        </g>
      </svg>
    </motion.div>
  );
}

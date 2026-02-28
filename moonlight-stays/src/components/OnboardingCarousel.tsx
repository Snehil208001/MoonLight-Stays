"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronRight, Sparkles } from "lucide-react";
import { AppLogo } from "./AppLogo";

interface OnboardingCarouselProps {
  onComplete: () => void;
  onSkip: () => void;
}

const steps = [
  {
    title: "Discover the Ethereal",
    subtext: "Find breathtaking hotels curated for your vibe.",
    icon: Sparkles,
  },
  {
    title: "Smart Dynamic Pricing",
    subtext: "Our engine tracks urgency, holidays, and surges to give you transparent rates.",
    highlight: true,
    icon: Sparkles,
  },
  {
    title: "Seamless Escapes",
    subtext: "Book instantly and securely. Your getaway awaits.",
    icon: Sparkles,
  },
];

export function OnboardingCarousel({ onComplete, onSkip }: OnboardingCarouselProps) {
  const [step, setStep] = useState(0);
  const isLast = step === steps.length - 1;

  return (
    <motion.div
      className="fixed inset-0 z-50 flex flex-col items-center justify-center mesh-gradient-bg px-6"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.5 }}
    >
      <button
        onClick={onSkip}
        className="absolute top-6 right-6 text-sm text-white/60 hover:text-white/90 transition-colors"
      >
        Skip to Search
      </button>

      <AnimatePresence mode="wait">
        <motion.div
          key={step}
          className="glass rounded-2xl p-8 md:p-12 max-w-md w-full text-center"
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -20 }}
          transition={{ duration: 0.4 }}
        >
          <div className="flex justify-center mb-6">
            <AppLogo size="sm" showGlow={false} />
          </div>
          <h2 className="text-2xl md:text-3xl font-bold text-white mb-4">
            {steps[step].title}
          </h2>
          <p
            className={`text-base md:text-lg ${
              steps[step].highlight
                ? "text-[#00FFFF] font-medium glow-cyan-text"
                : "text-white/80"
            }`}
          >
            {steps[step].subtext}
          </p>
        </motion.div>
      </AnimatePresence>

      <div className="flex items-center gap-4 mt-10">
        <div className="flex gap-2">
          {steps.map((_, i) => (
            <div
              key={i}
              className={`w-2 h-2 rounded-full transition-all ${
                i === step ? "bg-[#00FFFF] w-6" : "bg-white/30"
              }`}
            />
          ))}
        </div>
        <motion.button
          onClick={() => (isLast ? onComplete() : setStep((s) => s + 1))}
          className="flex items-center gap-2 px-6 py-3 rounded-xl bg-white/10 hover:bg-white/20 border border-white/20 hover:border-[#00FFFF]/50 text-white font-medium transition-all glow-cyan hover:glow-cyan"
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
        >
          {isLast ? "Get Started" : "Next"}
          <ChevronRight className="w-5 h-5" />
        </motion.button>
      </div>
    </motion.div>
  );
}

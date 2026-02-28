"use client";

import { useRouter } from "next/navigation";
import { motion } from "framer-motion";
import { XCircle } from "lucide-react";
import { Navbar } from "@/components/Navbar";
import { ROUTES } from "@/lib/constants";

export default function PaymentFailurePage() {
  const router = useRouter();

  return (
    <main className="min-h-screen mesh-gradient-bg">
      <Navbar />
    <div className="flex items-center justify-center min-h-[calc(100vh-4rem)] p-4">
      <motion.div
        className="glass rounded-2xl p-8 md:p-12 max-w-md w-full text-center"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5 }}
      >
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ delay: 0.2, type: "spring", stiffness: 200 }}
        >
          <XCircle className="w-20 h-20 text-red-400 mx-auto mb-6" />
        </motion.div>
        <h1 className="text-2xl font-bold text-white mb-2">Payment Failed</h1>
        <p className="text-white/70 mb-6">
          Your payment could not be processed. Please try again.
        </p>
        <button
          onClick={() => router.push(ROUTES.HOME)}
          className="px-6 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-medium hover:bg-[#00FFFF]/30 transition-all"
        >
          Return Home
        </button>
      </motion.div>
    </div>
    </main>
  );
}

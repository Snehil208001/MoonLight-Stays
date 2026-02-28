"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { motion } from "framer-motion";
import { CheckCircle } from "lucide-react";
import { Navbar } from "@/components/Navbar";
import { ROUTES } from "@/lib/constants";

export default function PaymentSuccessPage() {
  const router = useRouter();

  useEffect(() => {
    const t = setTimeout(() => router.push(ROUTES.BOOKINGS), 5000);
    return () => clearTimeout(t);
  }, [router]);

  return (
    <main className="min-h-screen mesh-gradient-bg flex items-center justify-center p-4">
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
          <CheckCircle className="w-20 h-20 text-[#00FFFF] mx-auto mb-6" />
        </motion.div>
        <h1 className="text-2xl font-bold text-white mb-2">Payment Successful!</h1>
        <p className="text-white/70 mb-6">
          Your booking is confirmed. A confirmation email has been sent.
        </p>
        <p className="text-sm text-white/50">
          Redirecting to your bookings in 5 seconds...
        </p>
        <button
          onClick={() => router.push(ROUTES.BOOKINGS)}
          className="mt-6 px-6 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-medium hover:bg-[#00FFFF]/30 transition-all"
        >
          View Bookings
        </button>
      </motion.div>
    </main>
  );
}

"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, User, Building2 } from "lucide-react";
import { api } from "@/lib/api";
import { showApiError, showSuccess } from "@/lib/toast";

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: (opts?: { isHotelManager?: boolean }) => void;
}

type UserType = "guest" | "hotel_manager";
type GuestMode = "login" | "signup";

export function AuthModal({ isOpen, onClose, onSuccess }: AuthModalProps) {
  const [userType, setUserType] = useState<UserType>("guest");
  const [guestMode, setGuestMode] = useState<GuestMode>("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      if (guestMode === "signup" && userType === "guest") {
        await api.signup(email, password, name);
        await api.login(email, password);
      } else {
        await api.login(email, password);
      }
      showSuccess(userType === "guest" && guestMode === "signup" ? "Account created! Welcome." : "Signed in successfully.");
      onSuccess?.({ isHotelManager: userType === "hotel_manager" });
      onClose();
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Something went wrong";
      setError(msg);
      showApiError(e);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setError("");
    setEmail("");
    setPassword("");
    setName("");
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="glass rounded-2xl max-w-md w-full p-6"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold text-white">
                  {userType === "guest" && guestMode === "login" && "Guest Sign In"}
                  {userType === "guest" && guestMode === "signup" && "Create Guest Account"}
                  {userType === "hotel_manager" && "Hotel Manager Sign In"}
                </h2>
                <button
                  onClick={onClose}
                  className="p-2 rounded-lg hover:bg-white/10 transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* User type tabs: Guest | Hotel Manager */}
              <div className="flex gap-2 p-1 rounded-xl bg-white/5 mb-6">
                <button
                  type="button"
                  onClick={() => { setUserType("guest"); resetForm(); setGuestMode("login"); }}
                  className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-medium transition-all ${
                    userType === "guest"
                      ? "bg-[#00FFFF]/20 text-[#00FFFF] border border-[#00FFFF]/50"
                      : "text-white/60 hover:text-white/80"
                  }`}
                >
                  <User className="w-4 h-4" />
                  Guest
                </button>
                <button
                  type="button"
                  onClick={() => { setUserType("hotel_manager"); resetForm(); }}
                  className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-medium transition-all ${
                    userType === "hotel_manager"
                      ? "bg-[#00FFFF]/20 text-[#00FFFF] border border-[#00FFFF]/50"
                      : "text-white/60 hover:text-white/80"
                  }`}
                >
                  <Building2 className="w-4 h-4" />
                  Hotel Manager
                </button>
              </div>

              {userType === "hotel_manager" && (
                <p className="mb-4 text-xs text-white/50 bg-amber-500/10 border border-amber-500/30 rounded-lg px-3 py-2">
                  Hotel Manager accounts cannot be created here. Contact your administrator for access.
                </p>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                {userType === "guest" && guestMode === "signup" && (
                  <div>
                    <label className="block text-sm text-white/70 mb-2">Name</label>
                    <input
                      type="text"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      required
                      className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-sm text-white/70 mb-2">Email</label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white"
                  />
                </div>
                <div>
                  <label className="block text-sm text-white/70 mb-2">Password</label>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white"
                  />
                </div>
                {error && <p className="text-red-400 text-sm">{error}</p>}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-semibold hover:bg-[#00FFFF]/30 disabled:opacity-50 transition-all"
                >
                  {loading ? "Please wait..." : userType === "guest" && guestMode === "signup" ? "Create Account" : "Sign In"}
                </button>
              </form>

              {userType === "guest" && (
                <p className="mt-4 text-center text-sm text-white/60">
                  {guestMode === "login" ? (
                    <>
                      Don&apos;t have an account?{" "}
                      <button
                        type="button"
                        onClick={() => setGuestMode("signup")}
                        className="text-[#00FFFF] hover:underline"
                      >
                        Sign up
                      </button>
                    </>
                  ) : (
                    <>
                      Already have an account?{" "}
                      <button
                        type="button"
                        onClick={() => setGuestMode("login")}
                        className="text-[#00FFFF] hover:underline"
                      >
                        Sign in
                      </button>
                    </>
                  )}
                </p>
              )}
            </motion.div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

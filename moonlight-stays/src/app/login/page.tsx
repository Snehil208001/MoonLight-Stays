"use client";

import { Suspense, useEffect } from "react";
import { useRouter } from "next/navigation";
import { AuthModal } from "@/components/AuthModal";
import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/hooks/useAuth";
import { useReturnUrl } from "@/components/ProtectedRoute";
import { ROUTES } from "@/lib/constants";

function LoginContent() {
  const router = useRouter();
  const returnUrl = useReturnUrl();
  const { isLoggedIn, loading, refreshAuth } = useAuth();

  useEffect(() => {
    if (!loading && isLoggedIn) {
      router.replace(returnUrl);
    }
  }, [isLoggedIn, loading, router, returnUrl]);

  const handleSuccess = async (opts?: { isHotelManager?: boolean }) => {
    await refreshAuth();
    if (opts?.isHotelManager) {
      router.replace(ROUTES.ADMIN);
    } else {
      router.replace(returnUrl);
    }
  };

  if (loading) {
    return (
      <main className="min-h-screen mesh-gradient-bg flex items-center justify-center">
        <div className="text-white/70">Loading...</div>
      </main>
    );
  }

  return (
    <main className="min-h-screen mesh-gradient-bg">
      <Navbar />
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <AuthModal isOpen={true} onClose={() => router.push(returnUrl)} onSuccess={handleSuccess} />
      </div>
    </main>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <main className="min-h-screen mesh-gradient-bg flex items-center justify-center">
        <div className="text-white/70">Loading...</div>
      </main>
    }>
      <LoginContent />
    </Suspense>
  );
}

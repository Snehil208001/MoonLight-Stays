"use client";

import { useEffect } from "react";
import { useRouter, usePathname, useSearchParams } from "next/navigation";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { refreshAuth } from "@/store/authSlice";
import { ROUTES } from "@/lib/constants";

interface ProtectedRouteProps {
  children: React.ReactNode;
  /** Require hotel manager role (admin only) */
  requireHotelManager?: boolean;
}

export function ProtectedRoute({ children, requireHotelManager = false }: ProtectedRouteProps) {
  const router = useRouter();
  const pathname = usePathname();
  const dispatch = useAppDispatch();
  const user = useAppSelector((s) => s.auth.user);
  const loading = useAppSelector((s) => s.auth.loading);

  useEffect(() => {
    if (loading) return;

    if (!user) {
      const returnUrl = encodeURIComponent(pathname || ROUTES.HOME);
      router.replace(`${ROUTES.LOGIN}?returnUrl=${returnUrl}`);
      return;
    }

    if (requireHotelManager && !user.roles?.includes("HOTEL_MANAGER")) {
      router.replace(ROUTES.HOME);
    }
  }, [user, loading, requireHotelManager, pathname, router]);

  if (loading) {
    return (
      <main className="min-h-screen mesh-gradient-bg flex items-center justify-center">
        <div className="text-white/70">Loading...</div>
      </main>
    );
  }

  if (!user) return null;
  if (requireHotelManager && !user.roles?.includes("HOTEL_MANAGER")) return null;

  return <>{children}</>;
}

/** Get return URL from query params, defaulting to home */
export function useReturnUrl() {
  const searchParams = useSearchParams();
  return searchParams.get("returnUrl") || ROUTES.HOME;
}

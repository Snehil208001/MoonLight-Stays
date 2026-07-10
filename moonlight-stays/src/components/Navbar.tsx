"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { AppLogo } from "./AppLogo";
import { LogIn, LogOut, Calendar, User, Building2, Heart, Sparkles } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES } from "@/lib/constants";

export function Navbar() {
  const pathname = usePathname();
  const { isLoggedIn, isHotelManager, logout } = useAuth();

  return (
    <nav className="glass sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link href={ROUTES.HOME} className="flex items-center gap-3 text-white hover:opacity-90">
            <AppLogo size="sm" showGlow={false} />
            <span className="text-xl font-bold">Moonlight Stays</span>
          </Link>

          <div className="flex items-center gap-2">
            <Link
              href={ROUTES.TRIP_PLANNER}
              className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                pathname === ROUTES.TRIP_PLANNER
                  ? "bg-[#00FFFF]/20 text-[#00FFFF]"
                  : "hover:bg-white/10 text-white"
              }`}
            >
              <Sparkles className="w-4 h-4" />
              Trip Planner
            </Link>
            {isLoggedIn && (
              <>
                <Link
                  href={ROUTES.BOOKINGS}
                  className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                    pathname === ROUTES.BOOKINGS
                      ? "bg-[#00FFFF]/20 text-[#00FFFF]"
                      : "hover:bg-white/10 text-white"
                  }`}
                >
                  <Calendar className="w-4 h-4" />
                  My Bookings
                </Link>
                <Link
                  href={ROUTES.PROFILE}
                  className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                    pathname === ROUTES.PROFILE
                      ? "bg-[#00FFFF]/20 text-[#00FFFF]"
                      : "hover:bg-white/10 text-white"
                  }`}
                >
                  <User className="w-4 h-4" />
                  Profile
                </Link>
                <Link
                  href="/favorites"
                  className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                    pathname === "/favorites"
                      ? "bg-[#00FFFF]/20 text-[#00FFFF]"
                      : "hover:bg-white/10 text-white"
                  }`}
                >
                  <Heart className="w-4 h-4" />
                  Favorites
                </Link>
                {isHotelManager && (
                  <Link
                    href={ROUTES.ADMIN}
                    className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                      pathname === ROUTES.ADMIN
                        ? "bg-[#00FFFF]/20 text-[#00FFFF]"
                        : "hover:bg-white/10 text-white"
                    }`}
                  >
                    <Building2 className="w-4 h-4" />
                    Admin
                  </Link>
                )}
              </>
            )}
            {isLoggedIn ? (
              <button
                onClick={logout}
                className="flex items-center gap-2 px-4 py-2 rounded-xl glass hover:bg-white/10 text-white text-sm font-medium transition-all"
              >
                <LogOut className="w-4 h-5" />
                Logout
              </button>
            ) : (
              <Link
                href={ROUTES.LOGIN}
                className="flex items-center gap-2 px-4 py-2 rounded-xl glass hover:bg-white/10 text-white text-sm font-medium transition-all"
              >
                <LogIn className="w-4 h-5" />
                Sign In
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

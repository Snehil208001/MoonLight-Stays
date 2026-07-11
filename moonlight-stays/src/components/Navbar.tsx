"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { AppLogo } from "./AppLogo";
import {
  LogIn,
  LogOut,
  Calendar,
  User,
  Building2,
  Heart,
  Sparkles,
  Menu,
  X,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES } from "@/lib/constants";

type NavItem = { href: string; label: string; icon: LucideIcon };

export function Navbar() {
  const pathname = usePathname();
  const { isLoggedIn, isHotelManager, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const items: NavItem[] = [
    ...(isLoggedIn
      ? [
          { href: ROUTES.TRIP_PLANNER, label: "Trip Planner", icon: Sparkles },
          { href: ROUTES.BOOKINGS, label: "My Bookings", icon: Calendar },
          { href: ROUTES.PROFILE, label: "Profile", icon: User },
          { href: ROUTES.FAVORITES, label: "Favorites", icon: Heart },
          ...(isHotelManager
            ? [{ href: ROUTES.ADMIN, label: "Admin", icon: Building2 }]
            : []),
        ]
      : []),
  ];

  const linkClass = (href: string) =>
    `flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
      pathname === href
        ? "bg-[#00FFFF]/20 text-[#00FFFF]"
        : "hover:bg-white/10 text-white"
    }`;

  return (
    <nav className="glass sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link
            href={ROUTES.HOME}
            className="flex items-center gap-3 text-white hover:opacity-90"
            onClick={() => setMobileOpen(false)}
          >
            <AppLogo size="sm" showGlow={false} />
            <span className="text-xl font-bold">Moonlight Stays</span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-2">
            {items.map(({ href, label, icon: Icon }) => (
              <Link key={href} href={href} className={linkClass(href)}>
                <Icon className="w-4 h-4" />
                {label}
              </Link>
            ))}
            {isLoggedIn ? (
              <button
                onClick={logout}
                className="flex items-center gap-2 px-4 py-2 rounded-xl glass hover:bg-white/10 text-white text-sm font-medium transition-all"
              >
                <LogOut className="w-4 h-4" />
                Logout
              </button>
            ) : (
              <Link
                href={ROUTES.LOGIN}
                className="flex items-center gap-2 px-4 py-2 rounded-xl glass hover:bg-white/10 text-white text-sm font-medium transition-all"
              >
                <LogIn className="w-4 h-4" />
                Sign In
              </Link>
            )}
          </div>

          {/* Mobile toggle */}
          <button
            type="button"
            aria-label={mobileOpen ? "Close menu" : "Open menu"}
            aria-expanded={mobileOpen}
            onClick={() => setMobileOpen((o) => !o)}
            className="md:hidden flex items-center justify-center w-10 h-10 rounded-xl glass hover:bg-white/10 text-white transition-all"
          >
            {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="md:hidden border-t border-white/10 px-4 py-3 space-y-1">
          {items.map(({ href, label, icon: Icon }) => (
            <Link
              key={href}
              href={href}
              onClick={() => setMobileOpen(false)}
              className={linkClass(href)}
            >
              <Icon className="w-4 h-4" />
              {label}
            </Link>
          ))}
          {isLoggedIn ? (
            <button
              onClick={() => {
                setMobileOpen(false);
                logout();
              }}
              className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium text-white hover:bg-white/10 transition-all"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          ) : (
            <Link
              href={ROUTES.LOGIN}
              onClick={() => setMobileOpen(false)}
              className={linkClass(ROUTES.LOGIN)}
            >
              <LogIn className="w-4 h-4" />
              Sign In
            </Link>
          )}
        </div>
      )}
    </nav>
  );
}

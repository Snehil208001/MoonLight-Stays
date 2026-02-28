"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { useAuth } from "@/hooks/useAuth";
import { api } from "@/lib/api";
import { showApiError, showSuccess } from "@/lib/toast";
import { ArrowLeft, Heart } from "lucide-react";
import { ROUTES } from "@/lib/constants";

export default function ProfilePage() {
  const { user, isLoggedIn, refreshAuth, loading: authLoading } = useAuth();
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState("");

  useEffect(() => {
    if (isLoggedIn) {
      api.getProfile().then((p) => setName(p.name ?? "")).catch(() => {});
    }
  }, [isLoggedIn]);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.updateProfile({ name });
      await refreshAuth();
      showSuccess("Profile updated.");
      setEditing(false);
    } catch (e) {
      showApiError(e);
    }
  };

  return (
    <ProtectedRoute>
      {authLoading ? (
        <main className="min-h-screen mesh-gradient-bg flex items-center justify-center">
          <div className="text-white/70">Loading...</div>
        </main>
      ) : (
        <main className="min-h-screen mesh-gradient-bg">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-8">
        <Link
          href={ROUTES.HOME}
          className="inline-flex items-center gap-2 text-white/70 hover:text-white mb-6"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Search
        </Link>

        <div className="glass rounded-2xl p-6 mb-8">
          <h1 className="text-2xl font-bold text-white mb-4">Profile</h1>
          {editing ? (
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div>
                <label className="block text-sm text-white/70 mb-2">Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF]"
                >
                  Save
                </button>
                <button
                  type="button"
                  onClick={() => setEditing(false)}
                  className="px-4 py-2 rounded-xl bg-white/10 text-white"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : (
            <div>
              <p className="text-white/70">Name: {user?.name ?? "-"}</p>
              <p className="text-white/70 mt-1">Email: {user?.email ?? "-"}</p>
              <button
                onClick={() => setEditing(true)}
                className="mt-4 px-4 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white text-sm"
              >
                Edit Profile
              </button>
            </div>
          )}
        </div>

        <div className="mt-6">
          <Link
            href={ROUTES.FAVORITES}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00FFFF]/10 hover:bg-[#00FFFF]/20 border border-[#00FFFF]/30 text-[#00FFFF] text-sm font-medium"
          >
            <Heart className="w-4 h-4" />
            View Favorites
          </Link>
        </div>
      </div>
        </main>
      )}
    </ProtectedRoute>
  );
}

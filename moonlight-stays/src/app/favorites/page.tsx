"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { motion } from "framer-motion";
import { Navbar } from "@/components/Navbar";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { useAuth } from "@/hooks/useAuth";
import { api, type Hotel } from "@/lib/api";
import { showApiError, showSuccess } from "@/lib/toast";
import { ArrowLeft, MapPin, Heart } from "lucide-react";
import { ROUTES } from "@/lib/constants";

export default function FavoritesPage() {
  const { isLoggedIn, loading: authLoading } = useAuth();
  const [favorites, setFavorites] = useState<Hotel[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn) return;
    api
      .getFavoriteHotels()
      .then((list) => setFavorites(Array.isArray(list) ? list : []))
      .catch((e) => { setFavorites([]); showApiError(e); })
      .finally(() => setLoading(false));
  }, [isLoggedIn]);

  const handleRemoveFavorite = async (hotelId: number) => {
    try {
      await api.removeFromFavorites(hotelId);
      showSuccess("Removed from favorites.");
      setFavorites((f) => f.filter((h) => h.id !== hotelId));
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

        <h1 className="text-2xl font-bold text-white mb-6 flex items-center gap-2">
          <Heart className="w-7 h-7 text-[#00FFFF]" />
          Favorites
        </h1>

        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="glass rounded-2xl h-24 animate-pulse" />
            ))}
          </div>
        ) : favorites.length === 0 ? (
          <div className="glass rounded-2xl p-12 text-center text-white/60">
            <Heart className="w-16 h-16 mx-auto mb-4 text-white/20" />
            <p className="text-lg mb-2">No favorites yet.</p>
            <p>
              <Link href={ROUTES.HOME} className="text-[#00FFFF] hover:underline">
                Browse hotels
              </Link>{" "}
              and add some!
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {favorites.map((h) => (
              <motion.div
                key={h.id}
                className="glass rounded-2xl p-4 flex justify-between items-center"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
              >
                <Link href={`/hotels/${h.id}`} className="flex-1">
                  <h3 className="font-bold text-white">{h.name}</h3>
                  <p className="text-sm text-white/60 flex items-center gap-1">
                    <MapPin className="w-4 h-4" />
                    {h.city}
                  </p>
                </Link>
                <button
                  onClick={() => handleRemoveFavorite(h.id)}
                  className="px-4 py-2 rounded-xl bg-red-500/20 hover:bg-red-500/30 border border-red-500/50 text-red-400 text-sm font-medium"
                >
                  Remove
                </button>
              </motion.div>
            ))}
          </div>
        )}
      </div>
        </main>
      )}
    </ProtectedRoute>
  );
}

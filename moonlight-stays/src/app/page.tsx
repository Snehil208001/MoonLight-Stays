"use client";

import { useState, useEffect, useCallback, Suspense, useMemo, useRef } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { AnimatePresence } from "framer-motion";
import { AppLogo } from "@/components/AppLogo";
import { SplashScreen } from "@/components/SplashScreen";
import { OnboardingCarousel } from "@/components/OnboardingCarousel";
import { HeroSearch, type SearchParams } from "@/components/HeroSearch";
import { HotelCard } from "@/components/HotelCard";
import { BookingModal } from "@/components/BookingModal";
import { AuthModal } from "@/components/AuthModal";
import { Navbar } from "@/components/Navbar";
import { api, type HotelPriceDto, type HotelInfoDto, type RoomPriceDto } from "@/lib/api";
import { useAuth } from "@/hooks/useAuth";
import { showApiError, showSuccess } from "@/lib/toast";

type AppState = "splash" | "onboarding" | "landing";
const ONBOARDING_KEY = "moonlight_onboarding_done";

function HomeContent() {
  const searchParamsUrl = useSearchParams();
  const router = useRouter();
  const { isLoggedIn, refreshAuth } = useAuth();
  const [appState, setAppState] = useState<AppState>("splash");

  useEffect(() => {
    if (localStorage.getItem(ONBOARDING_KEY) === "1") {
      setAppState("landing");
    }
  }, []);
  const [hotels, setHotels] = useState<HotelPriceDto[]>([]);
  const [searchParams, setSearchParams] = useState<SearchParams | null>(null);
  const [loading, setLoading] = useState(false);
  const [favoriteIds, setFavoriteIds] = useState<Set<number>>(new Set());
  const [selectedHotel, setSelectedHotel] = useState<HotelPriceDto | null>(null);
  const [hotelInfo, setHotelInfo] = useState<HotelInfoDto | null>(null);
  const [hotelInfoLoading, setHotelInfoLoading] = useState(false);
  const [roomPrices, setRoomPrices] = useState<RoomPriceDto[]>([]);
  const [showAuth, setShowAuth] = useState(false);
  const [promoCode, setPromoCode] = useState("");
  const [promoDiscount, setPromoDiscount] = useState<number | null>(null);
  const [promoValidating, setPromoValidating] = useState(false);
  const [availablePromos, setAvailablePromos] = useState<{ code: string; discountPercentage: number }[]>([]);

  useEffect(() => {
    if (searchParamsUrl.get("auth") === "1" || searchParamsUrl.get("signin") === "1") {
      setShowAuth(true);
    }
  }, [searchParamsUrl]);

  useEffect(() => {
    if (isLoggedIn && appState === "landing") {
      api.getFavoriteHotels().then((list) => {
        const ids = new Set((list ?? []).map((h) => h.id));
        setFavoriteIds(ids);
      }).catch((e) => { setFavoriteIds(new Set()); showApiError(e); });
    }
  }, [isLoggedIn, appState]);

  useEffect(() => {
    if (appState === "landing") {
      api.getActivePromoCodes().then(setAvailablePromos).catch(() => setAvailablePromos([]));
    }
  }, [appState]);

  useEffect(() => {
    if (appState !== "splash") return;
    const t = setTimeout(() => setAppState("onboarding"), 2500);
    return () => clearTimeout(t);
  }, [appState]);

  const initialSearchFromUrl = useMemo(() => {
    const city = searchParamsUrl.get("city") ?? "";
    const checkIn = searchParamsUrl.get("checkIn");
    const checkOut = searchParamsUrl.get("checkOut");
    const rooms = searchParamsUrl.get("rooms");
    const minP = searchParamsUrl.get("minPrice");
    const maxP = searchParamsUrl.get("maxPrice");
    const roomType = searchParamsUrl.get("roomType") ?? "";
    const amenity = searchParamsUrl.get("amenity") ?? "";
    if (!city && !checkIn && !checkOut) return null;
    const today = new Date().toISOString().slice(0, 10);
    const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
    return {
      city,
      checkInDate: checkIn ?? today,
      checkOutDate: checkOut ?? tomorrow,
      roomsCount: rooms ? parseInt(rooms, 10) || 1 : 1,
      minPrice: minP ? parseInt(minP, 10) : undefined,
      maxPrice: maxP ? parseInt(maxP, 10) : undefined,
      roomType: roomType || undefined,
      amenity: amenity || undefined,
    } satisfies SearchParams;
  }, [searchParamsUrl]);

  const handleSearch = useCallback(async (params: SearchParams) => {
    setLoading(true);
    setSearchParams(params);
    try {
      const q = new URLSearchParams();
      if (params.city) q.set("city", params.city);
      q.set("checkIn", params.checkInDate);
      q.set("checkOut", params.checkOutDate);
      q.set("rooms", String(params.roomsCount));
      if (params.minPrice != null) q.set("minPrice", String(params.minPrice));
      if (params.maxPrice != null) q.set("maxPrice", String(params.maxPrice));
      if (params.roomType) q.set("roomType", params.roomType);
      if (params.amenity) q.set("amenity", params.amenity);
      router.replace(`/?${q.toString()}`, { scroll: false });
    } catch {
      /* ignore URL update errors */
    }
    try {
      const data = await api.searchHotels({
        city: params.city,
        checkInDate: params.checkInDate,
        endDate: params.checkOutDate,
        roomsCount: params.roomsCount,
        minPrice: params.minPrice,
        maxPrice: params.maxPrice,
        roomType: params.roomType,
        amenity: params.amenity,
        page: 0,
        size: 50,
      });
      setHotels(Array.isArray(data) ? data : []);
    } catch (e) {
      showApiError(e);
      setHotels([]);
    } finally {
      setLoading(false);
    }
  }, [router]);

  const hasRestoredFromUrl = useRef(false);
  useEffect(() => {
    if (appState !== "landing" || !initialSearchFromUrl || hasRestoredFromUrl.current) return;
    const alreadyMatches =
      searchParams?.city === initialSearchFromUrl.city &&
      searchParams?.checkInDate === initialSearchFromUrl.checkInDate &&
      searchParams?.checkOutDate === initialSearchFromUrl.checkOutDate &&
      searchParams?.roomsCount === initialSearchFromUrl.roomsCount;
    if (alreadyMatches) return;
    hasRestoredFromUrl.current = true;
    handleSearch(initialSearchFromUrl);
  }, [appState, initialSearchFromUrl, searchParams, handleSearch]);

  const handleBook = useCallback(
    async (hotel: HotelPriceDto) => {
      if (!isLoggedIn) {
        setShowAuth(true);
        return;
      }
      setSelectedHotel(hotel);
      setHotelInfo(null);
      setRoomPrices([]);
      setHotelInfoLoading(true);
      const ci = searchParams?.checkInDate ?? new Date().toISOString().slice(0, 10);
      const co = searchParams?.checkOutDate ?? new Date(Date.now() + 86400000).toISOString().slice(0, 10);
      const rooms = searchParams?.roomsCount ?? 1;
      try {
        const [info, prices] = await Promise.all([
          api.getHotelInfo(hotel.hotel.id),
          api.getRoomPrices(hotel.hotel.id, ci, co, rooms),
        ]);
        setHotelInfo(info);
        setRoomPrices(prices ?? []);
      } catch (e) {
        showApiError(e);
        setHotelInfo(null);
        setRoomPrices([]);
      } finally {
        setHotelInfoLoading(false);
      }
    },
    [isLoggedIn, searchParams]
  );

  const handleProceedToPayment = useCallback(
    async (params: {
      hotelId: number;
      roomId: number;
      checkInDate: string;
      checkOutDate: string;
      roomsCount: number;
      promoCode?: string;
    }) => {
      const booking = await api.initBooking(params);
      const { sessionUrl } = await api.initiatePayment(booking.id);
      if (sessionUrl) window.location.href = sessionUrl;
    },
    []
  );

  const handleAuthSuccess = useCallback(async () => {
    await refreshAuth();
  }, [refreshAuth]);

  const handleApplyPromo = useCallback(async (codeOverride?: string) => {
    const code = (codeOverride ?? promoCode).trim().replace(/['"]+$|^['"]+/g, "").trim();
    if (!code) {
      setPromoDiscount(null);
      return;
    }
    if (codeOverride) setPromoCode(codeOverride);
    setPromoValidating(true);
    try {
      const res = await api.validatePromoCode(code);
      if (res.valid && res.discountPercentage != null) {
        setPromoDiscount(res.discountPercentage);
        showSuccess(`${res.discountPercentage}% discount applied!`);
      } else {
        setPromoDiscount(null);
        showApiError(new Error("Invalid or expired promo code"));
      }
    } catch {
      setPromoDiscount(null);
      showApiError(new Error("Invalid or expired promo code"));
    } finally {
      setPromoValidating(false);
    }
  }, [promoCode]);

  const handleToggleFavorite = useCallback(async (hotelId: number) => {
    const isFav = favoriteIds.has(hotelId);
    try {
      if (isFav) {
        await api.removeFromFavorites(hotelId);
        showSuccess("Removed from favorites.");
        setFavoriteIds((s) => {
          const next = new Set(s);
          next.delete(hotelId);
          return next;
        });
      } else {
        await api.addToFavorites(hotelId);
        showSuccess("Added to favorites.");
        setFavoriteIds((s) => new Set(Array.from(s).concat(hotelId)));
      }
    } catch (e) {
      showApiError(e);
    }
  }, [favoriteIds]);

  return (
    <main className="min-h-screen mesh-gradient-bg">
      <AnimatePresence mode="wait">
        {appState === "splash" && (
          <SplashScreen key="splash" onComplete={() => setAppState("onboarding")} />
        )}
        {appState === "onboarding" && (
          <OnboardingCarousel
            key="onboarding"
            onComplete={() => {
              if (typeof window !== "undefined") localStorage.setItem(ONBOARDING_KEY, "1");
              setAppState("landing");
            }}
            onSkip={() => {
              if (typeof window !== "undefined") localStorage.setItem(ONBOARDING_KEY, "1");
              setAppState("landing");
            }}
          />
        )}
      </AnimatePresence>

      {appState === "landing" && (
        <div className="min-h-screen">
          <Navbar />

          <section className="pt-12 pb-8 px-4">
            <div className="max-w-4xl mx-auto mb-4">
              <h1 className="text-3xl md:text-4xl font-bold text-center text-white mb-2">
                Find Your Ethereal Escape
              </h1>
              <p className="text-center text-white/70">
                Discover breathtaking hotels with transparent pricing
              </p>
            </div>
            <div className="max-w-4xl mx-auto px-4">
              <HeroSearch onSearch={handleSearch} isLoading={loading} initialValues={initialSearchFromUrl ?? undefined} />
            </div>
          </section>

          <section className="pb-20 px-4">
            <div className="max-w-7xl mx-auto">
              {hotels.length > 0 && (
                <div className="mb-6 flex flex-col gap-2">
                  <div className="flex flex-wrap items-center gap-3">
                    <div className="flex gap-2 flex-1 min-w-[200px]">
                      <input
                        type="text"
                        value={promoCode}
                        onChange={(e) => setPromoCode(e.target.value.toUpperCase())}
                        onKeyDown={(e) => e.key === "Enter" && handleApplyPromo()}
                        placeholder="Promo code"
                        className="px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white placeholder:text-white/40 focus:border-[#00FFFF]/50 outline-none flex-1"
                      />
                      <button
                        type="button"
                        onClick={() => handleApplyPromo()}
                        disabled={promoValidating || !promoCode.trim()}
                        className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] text-sm font-medium disabled:opacity-50"
                      >
                        {promoValidating ? "Checking..." : "Apply"}
                      </button>
                    </div>
                    {promoDiscount != null && (
                      <span className="text-green-400 text-sm font-medium">
                        {promoDiscount}% off applied
                      </span>
                    )}
                  </div>
                  {availablePromos.length > 0 && (
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-white/50 text-sm">Available promos:</span>
                      {availablePromos.map((p) => (
                        <button
                          key={p.code}
                          type="button"
                          onClick={() => handleApplyPromo(p.code)}
                          className="px-2 py-1 rounded-lg bg-white/10 hover:bg-[#00FFFF]/20 hover:border-[#00FFFF]/50 border border-white/10 text-white/90 text-sm"
                        >
                          {p.code} ({p.discountPercentage}% off)
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )}
              {loading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                  {[1, 2, 3, 4, 5, 6].map((i) => (
                    <div
                      key={i}
                      className="glass rounded-2xl h-80 animate-pulse"
                    />
                  ))}
                </div>
              ) : hotels.length > 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                  {hotels.map((h) => (
                    <HotelCard
                      key={h.hotel.id}
                      hotel={h}
                      onBook={handleBook}
                      searchParams={searchParams}
                      isFavorite={favoriteIds.has(h.hotel.id)}
                      onToggleFavorite={isLoggedIn ? handleToggleFavorite : undefined}
                      isLoggedIn={isLoggedIn}
                      discountPercentage={promoDiscount ?? undefined}
                    />
                  ))}
                </div>
              ) : searchParams ? (
                <div className="text-center py-16 text-white/60">
                  No hotels found. Try a different city, leave city empty to browse all, or check your dates.
                </div>
              ) : (
                <div className="text-center py-16 text-white/50">
                  Search for hotels to get started. Leave city empty to browse all hotels.
                </div>
              )}
            </div>
          </section>
        </div>
      )}

      {selectedHotel && (
        <BookingModal
          isOpen={!!selectedHotel}
          onClose={() => { setSelectedHotel(null); setRoomPrices([]); }}
          hotelData={selectedHotel}
          hotelInfo={hotelInfo}
          hotelInfoLoading={hotelInfoLoading}
          checkIn={searchParams?.checkInDate ?? new Date().toISOString().slice(0, 10)}
          checkOut={searchParams?.checkOutDate ?? new Date(Date.now() + 86400000).toISOString().slice(0, 10)}
          roomsCount={searchParams?.roomsCount ?? 1}
          roomPrices={roomPrices}
          onProceedToPayment={handleProceedToPayment}
        />
      )}

      <AuthModal
        isOpen={showAuth}
        onClose={() => setShowAuth(false)}
        onSuccess={handleAuthSuccess}
      />
    </main>
  );
}

export default function Home() {
  return (
    <Suspense fallback={<div className="min-h-screen mesh-gradient-bg flex items-center justify-center"><div className="text-white/70">Loading...</div></div>}>
      <HomeContent />
    </Suspense>
  );
}

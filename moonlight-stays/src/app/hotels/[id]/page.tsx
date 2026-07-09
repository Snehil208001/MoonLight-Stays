"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { useParams, useSearchParams } from "next/navigation";
import { motion } from "framer-motion";
import { Navbar } from "@/components/Navbar";
import { BookingModal } from "@/components/BookingModal";
import { AuthModal } from "@/components/AuthModal";
import { useAuth } from "@/hooks/useAuth";
import { api, type HotelInfoDto, type ReviewDto, type HotelPriceDto, type RoomDto, type RoomPriceDto } from "@/lib/api";
import { showApiError, showSuccess } from "@/lib/toast";
import { ArrowLeft, MapPin, Star, Heart, Bed, ChevronRight } from "lucide-react";
import { getImageSrc } from "@/lib/imageUtils";
import { RoomDetailModal } from "@/components/RoomDetailModal";

export default function HotelDetailPage() {
  const params = useParams();
  const searchParamsUrl = useSearchParams();
  const id = Number(params.id);
  const { isLoggedIn, user, refreshAuth } = useAuth();
  const [hotelInfo, setHotelInfo] = useState<HotelInfoDto | null>(null);
  const [reviews, setReviews] = useState<ReviewDto[]>([]);
  const [avgRating, setAvgRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewContent, setReviewContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [canReview, setCanReview] = useState(false);
  const [showBooking, setShowBooking] = useState(false);
  const [showAuth, setShowAuth] = useState(false);
  const [hotelPriceData, setHotelPriceData] = useState<HotelPriceDto | null>(null);
  const [localCheckIn, setLocalCheckIn] = useState("");
  const [localCheckOut, setLocalCheckOut] = useState("");
  const [localRooms, setLocalRooms] = useState(1);
  const [selectedHotelPhotoIndex, setSelectedHotelPhotoIndex] = useState(0);
  const [roomPrices, setRoomPrices] = useState<RoomPriceDto[]>([]);
  const [roomPricesLoading, setRoomPricesLoading] = useState(false);
  const [selectedRoomDetail, setSelectedRoomDetail] = useState<RoomDto | null>(null);
  const [preSelectedRoomId, setPreSelectedRoomId] = useState<number | undefined>(undefined);

  const checkIn = searchParamsUrl.get("checkIn") || localCheckIn;
  const checkOut = searchParamsUrl.get("checkOut") || localCheckOut;
  const roomsCount = parseInt(searchParamsUrl.get("rooms") ?? String(localRooms), 10) || localRooms || 1;

  useEffect(() => {
    setSelectedHotelPhotoIndex(0);
  }, [id]);

  useEffect(() => {
    if (isNaN(id)) return;
    Promise.all([
      api.getHotelInfo(id),
      api.getHotelReviews(id, 0, 20),
      api.getHotelAverageRating(id),
    ])
      .then(([info, revRes, avg]) => {
        setHotelInfo(info);
        setReviews(revRes.content ?? []);
        setAvgRating(avg ?? 0);
      })
      .catch(() => setHotelInfo(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!hotelInfo || !checkIn || !checkOut) {
      setRoomPrices([]);
      setRoomPricesLoading(false);
      return;
    }
    setRoomPricesLoading(true);
    api
      .getRoomPrices(id, checkIn, checkOut, roomsCount || 1)
      .then((prices) => setRoomPrices(prices ?? []))
      .catch(() => setRoomPrices([]))
      .finally(() => setRoomPricesLoading(false));
  }, [hotelInfo, id, checkIn, checkOut, roomsCount]);

  useEffect(() => {
    if (!isLoggedIn || !id) return;
    api.getFavoriteHotels().then((list) => {
      const fav = (list ?? []).some((h) => h.id === id);
      setIsFavorite(fav);
    }).catch((e) => { setIsFavorite(false); showApiError(e); });
  }, [isLoggedIn, id]);

  useEffect(() => {
    if (!isLoggedIn || !user || !id) {
      setCanReview(false);
      return;
    }
    Promise.all([api.getMyBookings(0, 100), api.getHotelReviews(id, 0, 100)])
      .then(([bookingsRes, revRes]) => {
        const confirmedForHotel = (bookingsRes.content ?? []).some((b) => {
          if (b.hotel?.id !== id) return false;
          if (b.bookingStatus !== "CONFIRMED" && b.bookingStatus !== "PAID") return false;
          
          const checkoutDate = new Date(b.checkOutDate);
          const today = new Date();
          checkoutDate.setHours(0, 0, 0, 0);
          today.setHours(0, 0, 0, 0);
          return checkoutDate <= today;
        });
        const alreadyReviewed = (revRes.content ?? []).some((r) => r.userId === user.id);
        setCanReview(confirmedForHotel && !alreadyReviewed);
      })
      .catch((e) => { setCanReview(false); showApiError(e); });
  }, [isLoggedIn, user, id]);

  const handleAddFavorite = async () => {
    if (!isLoggedIn) return;
    try {
      await api.addToFavorites(id);
      showSuccess("Added to favorites.");
      setIsFavorite(true);
    } catch (e) {
      showApiError(e);
    }
  };

  const handleRemoveFavorite = async () => {
    try {
      await api.removeFromFavorites(id);
      showSuccess("Removed from favorites.");
      setIsFavorite(false);
    } catch (e) {
      showApiError(e);
    }
  };

  const handleBookClick = useCallback(async (skipAuthCheck = false) => {
    if (!skipAuthCheck && !isLoggedIn) {
      const ok = await refreshAuth();
      if (!ok) {
        setShowAuth(true);
        return;
      }
    }
    if (!hotelInfo) return;
    const hotel = hotelInfo.hotelDto;
    const ci = checkIn || new Date().toISOString().slice(0, 10);
    const co = checkOut || new Date(Date.now() + 86400000).toISOString().slice(0, 10);
    const rooms = roomsCount || 1;
    if (ci && co) {
      api
        .getRoomPrices(id, ci, co, rooms)
        .then((prices) => {
          setHotelPriceData({
            hotel,
            price: hotelInfo.rooms?.[0]?.basePrice ?? hotel.basePrice ?? 0,
            roomPrices: prices ?? [],
          });
          setShowBooking(true);
        })
        .catch((e) => {
          showApiError(e);
          setHotelPriceData({
            hotel,
            price: hotelInfo.rooms?.[0]?.basePrice ?? hotel.basePrice ?? 0,
            roomPrices: [],
          });
          setShowBooking(true);
        });
    } else {
      setHotelPriceData({
        hotel,
        price: hotelInfo.rooms?.[0]?.basePrice ?? hotel.basePrice ?? 0,
        roomPrices: [],
      });
      setShowBooking(true);
    }
  }, [isLoggedIn, hotelInfo, id, checkIn, checkOut, roomsCount, refreshAuth]);

  const handleBookFromRoom = useCallback(
    (room: RoomDto) => {
      setPreSelectedRoomId(room.id);
      setSelectedRoomDetail(null);
      handleBookClick();
    },
    [handleBookClick]
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

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.addReview(id, reviewRating, reviewContent);
      showSuccess("Review added. Thank you!");
      setReviews((r) => [{ id: 0, rating: reviewRating, content: reviewContent, hotelId: id, userId: 0 }, ...r]);
      setShowReviewForm(false);
      setReviewContent("");
      const avg = await api.getHotelAverageRating(id);
      setAvgRating(avg);
    } catch (e) {
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading || !hotelInfo) {
    return (
      <div className="min-h-screen mesh-gradient-bg">
        <Navbar />
        <div className="max-w-4xl mx-auto px-4 py-8">
          {loading ? (
            <div className="glass rounded-2xl h-96 animate-pulse" />
          ) : (
            <div className="text-center text-white/60">Hotel not found</div>
          )}
        </div>
      </div>
    );
  }

  const hotel = hotelInfo.hotelDto;
  const checkInVal = checkIn || new Date().toISOString().slice(0, 10);
  const checkOutVal = checkOut || new Date(Date.now() + 86400000).toISOString().slice(0, 10);

  return (
    <div className="min-h-screen mesh-gradient-bg">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-8">
        <Link href="/" className="inline-flex items-center gap-2 text-white/70 hover:text-white mb-6">
          <ArrowLeft className="w-4 h-4" />
          Back to Search
        </Link>

        <div className="glass rounded-2xl overflow-hidden mb-6">
          <div className="relative">
            {hotel.photos?.length ? (
              <>
                <div className="relative w-full h-64 sm:h-80 bg-white/5">
                  {/* Hero image - selected photo */}
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={getImageSrc(hotel.photos[selectedHotelPhotoIndex])}
                    alt={`${hotel.name}`}
                    className="absolute inset-0 w-full h-full object-cover"
                    onError={(e) => {
                      const el = e.target as HTMLImageElement;
                      el.style.display = "none";
                    }}
                  />
                </div>
                {hotel.photos.length > 1 && (
                  <div className="flex gap-2 p-4 overflow-x-auto border-t border-white/10">
                    {hotel.photos.map((url, i) => (
                      <button
                        key={i}
                        type="button"
                        onClick={() => setSelectedHotelPhotoIndex(i)}
                        className={`flex-shrink-0 rounded-lg overflow-hidden transition-all ${
                          selectedHotelPhotoIndex === i ? "ring-2 ring-[#00FFFF]" : "hover:ring-2 hover:ring-[#00FFFF]/50"
                        }`}
                      >
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img
                          src={getImageSrc(url)}
                          alt={`${hotel.name} ${i + 1}`}
                          className="w-24 h-20 object-cover"
                        />
                      </button>
                    ))}
                  </div>
                )}
              </>
            ) : (
              <div className="w-full h-48 sm:h-64 bg-white/5 flex items-center justify-center">
                <span className="text-white/40 text-sm">No hotel images</span>
              </div>
            )}
          </div>
          <div className="p-6">
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-2xl font-bold text-white">{hotel.name}</h1>
                <p className="text-white/60 flex items-center gap-1 mt-1">
                  <MapPin className="w-4 h-4" />
                  {hotel.city}
                </p>
                <div className="flex items-center gap-2 mt-2">
                  <Star className="w-5 h-5 text-amber-400 fill-amber-400" />
                  <span className="text-white font-medium">{avgRating.toFixed(1)}</span>
                  <span className="text-white/50">({reviews.length} reviews)</span>
                </div>
              </div>
              {isLoggedIn && (
                <button
                  onClick={isFavorite ? handleRemoveFavorite : handleAddFavorite}
                  className={`flex items-center gap-2 px-4 py-2 rounded-xl ${
                    isFavorite ? "bg-red-500/20 text-red-400" : "bg-white/10 hover:bg-white/20 text-white"
                  }`}
                >
                  <Heart className={`w-5 h-5 ${isFavorite ? "fill-red-400" : ""}`} />
                  {isFavorite ? "Saved" : "Save"}
                </button>
              )}
            </div>
            {hotel.amenities?.length ? (
              <div className="mt-4 flex flex-wrap gap-2">
                {hotel.amenities.map((a, i) => (
                  <span key={i} className="px-3 py-1 rounded-lg bg-white/10 text-white/80 text-sm">
                    {a}
                  </span>
                ))}
              </div>
            ) : null}
          </div>
        </div>

        {(!checkIn || !checkOut) && (
          <div className="glass rounded-2xl p-6 mb-6">
            <h3 className="text-lg font-bold text-white mb-4">Select your dates</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div>
                <label className="block text-sm text-white/70 mb-1">Check-in</label>
                <input
                  type="date"
                  value={localCheckIn}
                  onChange={(e) => setLocalCheckIn(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white [color-scheme:dark]"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Check-out</label>
                <input
                  type="date"
                  value={localCheckOut}
                  onChange={(e) => setLocalCheckOut(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white [color-scheme:dark]"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Rooms</label>
                <input
                  type="number"
                  min={1}
                  value={localRooms}
                  onChange={(e) => setLocalRooms(parseInt(e.target.value) || 1)}
                  className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white"
                />
              </div>
            </div>
          </div>
        )}

        <div className="glass rounded-2xl p-6 mb-6">
          <div className="flex justify-between items-center mb-4 flex-wrap gap-2">
            <div>
              <h2 className="text-lg font-bold text-white">Rooms</h2>
              {roomPricesLoading ? (
                <p className="text-sm text-white/50 mt-0.5">Calculating price...</p>
              ) : roomPrices.length > 0 ? (
                <p className="text-sm text-[#00FFFF] mt-0.5">
                  From ₹{Math.round(Math.min(...roomPrices.map((p) => p.pricePerNight))).toLocaleString()}/night
                </p>
              ) : checkIn && checkOut ? (
                <p className="text-sm text-white/50 mt-0.5">Base price shown</p>
              ) : (
                <p className="text-sm text-white/50 mt-0.5">Select dates above to see price</p>
              )}
            </div>
            <button
              type="button"
              onClick={() => handleBookClick()}
              className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] text-sm font-medium cursor-pointer"
            >
              Book Now
            </button>
          </div>
          <div className="space-y-4">
            {hotelInfo.rooms?.map((room) => {
              const roomPrice = roomPrices.find((p) => p.roomId === room.id);
              const displayPrice = roomPrice ? roomPrice.pricePerNight : room.basePrice;
              return (
                <button
                  key={room.id}
                  type="button"
                  onClick={() => setSelectedRoomDetail(room)}
                  className="w-full text-left p-4 rounded-xl bg-white/5 flex flex-col sm:flex-row gap-4 hover:bg-white/10 transition-colors cursor-pointer border border-transparent hover:border-white/10"
                >
                  <div className="flex gap-2 items-start flex-shrink-0">
                    {(room.photos?.length ?? 0) > 0 ? (
                      <div className="flex gap-2 overflow-x-auto">
                        {room.photos!.map((url, i) => (
                          /* eslint-disable-next-line @next/next/no-img-element */
                          <img
                            key={i}
                            src={getImageSrc(url)}
                            alt={`${room.types} ${i + 1}`}
                            className="w-24 h-20 object-cover rounded-lg flex-shrink-0"
                          />
                        ))}
                      </div>
                    ) : (
                      <div className="w-24 h-20 rounded-lg bg-white/10 flex items-center justify-center">
                        <Bed className="w-8 h-8 text-white/30" />
                      </div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="font-medium text-white">{room.types}</h3>
                    <p className="text-sm text-white/60">
                      ₹{Math.round(Number(displayPrice)).toLocaleString()}/night
                      {" · "}Capacity: {room.capacity} · {room.totalCount} available
                    </p>
                  </div>
                  <span className="text-[#00FFFF] font-bold flex-shrink-0 flex items-center gap-1">
                    ₹{Math.round(Number(displayPrice)).toLocaleString()}
                    <ChevronRight className="w-4 h-4 text-white/50" />
                  </span>
                </button>
              );
            })}
          </div>
        </div>

        <div className="glass rounded-2xl p-6">
          <h2 className="text-lg font-bold text-white mb-4">Reviews</h2>
          {canReview && !showReviewForm && (
            <button
              onClick={() => setShowReviewForm(true)}
              className="mb-4 px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-sm"
            >
              Add Review
            </button>
          )}
          {showReviewForm && (
            <form onSubmit={handleSubmitReview} className="mb-6 p-4 rounded-xl bg-white/5 space-y-3">
              <div>
                <label className="block text-sm text-white/70 mb-1">Rating (1-5)</label>
                <select
                  value={reviewRating}
                  onChange={(e) => setReviewRating(parseInt(e.target.value))}
                  className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white [color-scheme:dark]"
                >
                  {[1, 2, 3, 4, 5].map((n) => (
                    <option key={n} value={n}>
                      {n} stars
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Review</label>
                <textarea
                  value={reviewContent}
                  onChange={(e) => setReviewContent(e.target.value)}
                  rows={3}
                  className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div className="flex gap-2">
                <button type="submit" disabled={submitting} className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-sm">
                  Submit
                </button>
                <button type="button" onClick={() => setShowReviewForm(false)} className="px-4 py-2 rounded-xl bg-white/10 text-white text-sm">
                  Cancel
                </button>
              </div>
            </form>
          )}
          <div className="space-y-3">
            {reviews.length === 0 ? (
              <p className="text-white/50">No reviews yet.</p>
            ) : (
              reviews.map((r) => (
                <div key={r.id} className="p-4 rounded-xl bg-white/5">
                  <div className="flex items-center gap-2 mb-1">
                    {[...Array(5)].map((_, i) => (
                      <Star
                        key={i}
                        className={`w-4 h-4 ${i < r.rating ? "text-amber-400 fill-amber-400" : "text-white/30"}`}
                      />
                    ))}
                  </div>
                  <p className="text-white/90">{r.content}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {selectedRoomDetail && hotelInfo && (
        <RoomDetailModal
          isOpen={!!selectedRoomDetail}
          onClose={() => setSelectedRoomDetail(null)}
          room={selectedRoomDetail}
          hotelName={hotelInfo.hotelDto.name}
          price={
            roomPrices.find((p) => p.roomId === selectedRoomDetail.id)?.pricePerNight ??
            selectedRoomDetail.basePrice
          }
          onBookNow={() => handleBookFromRoom(selectedRoomDetail)}
        />
      )}

      {hotelPriceData && hotelInfo && (
        <BookingModal
          isOpen={showBooking}
          onClose={() => { setShowBooking(false); setPreSelectedRoomId(undefined); }}
          hotelData={hotelPriceData}
          hotelInfo={hotelInfo}
          hotelInfoLoading={false}
          checkIn={checkInVal}
          checkOut={checkOutVal}
          roomsCount={roomsCount || 1}
          initialRoomId={preSelectedRoomId}
          roomPrices={roomPrices.length ? roomPrices : hotelPriceData.roomPrices ?? []}
          onProceedToPayment={handleProceedToPayment}
        />
      )}

      <AuthModal
        isOpen={showAuth}
        onClose={() => setShowAuth(false)}
        onSuccess={async () => {
          await refreshAuth();
          setShowAuth(false);
          handleBookClick(true);
        }}
      />
    </div>
  );
}

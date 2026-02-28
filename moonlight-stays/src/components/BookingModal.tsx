"use client";

import { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, CreditCard, Bed, Calendar, UserPlus } from "lucide-react";
import { getFirstImageSrc } from "@/lib/imageUtils";
import type { HotelPriceDto, HotelInfoDto, RoomDto, RoomPriceDto, GuestDto, GuestGender } from "@/lib/api";
import { showApiError, showSuccess } from "@/lib/toast";

interface BookingModalProps {
  isOpen: boolean;
  onClose: () => void;
  hotelData: HotelPriceDto;
  hotelInfo?: HotelInfoDto | null;
  hotelInfoLoading?: boolean;
  checkIn: string;
  checkOut: string;
  roomsCount: number;
  initialRoomId?: number;
  roomPrices?: RoomPriceDto[];
  onProceedToPayment: (params: {
    hotelId: number;
    roomId: number;
    checkInDate: string;
    checkOutDate: string;
    roomsCount: number;
    promoCode?: string;
    totalAmount: number;
  }) => Promise<void>;
}

type Step = 1 | 2;

const GENDERS: { value: GuestGender; label: string }[] = [
  { value: "MALE", label: "Male" },
  { value: "FEMALE", label: "Female" },
  { value: "OTHER", label: "Other" },
];

export function BookingModal({
  isOpen,
  onClose,
  hotelData,
  hotelInfo,
  hotelInfoLoading,
  checkIn,
  checkOut,
  roomsCount,
  initialRoomId,
  roomPrices = [],
  onProceedToPayment,
}: BookingModalProps) {
  const [step, setStep] = useState<Step>(1);
  const [bookingId, setBookingId] = useState<number | null>(null);
  const [promoCode, setPromoCode] = useState("");
  const [promoDiscount, setPromoDiscount] = useState<number | null>(null);
  const [promoValidating, setPromoValidating] = useState(false);
  const [availablePromos, setAvailablePromos] = useState<{ code: string; discountPercentage: number }[]>([]);
  const [selectedRoom, setSelectedRoom] = useState<RoomDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [guests, setGuests] = useState<GuestDto[]>([]);
  const [newGuest, setNewGuest] = useState({ name: "", gender: "MALE" as GuestGender, age: "" });

  useEffect(() => {
    if (hotelInfo?.rooms?.length) {
      const preSelect = initialRoomId
        ? hotelInfo.rooms.find((r) => r.id === initialRoomId)
        : null;
      const fallback = hotelInfo.rooms[0];
      setSelectedRoom(preSelect ?? fallback);
    } else {
      setSelectedRoom(null);
    }
  }, [hotelInfo, initialRoomId]);

  useEffect(() => {
    if (!isOpen) {
      setStep(1);
      setBookingId(null);
      setGuests([]);
      setNewGuest({ name: "", gender: "MALE", age: "" });
      setPromoDiscount(null);
    }
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      import("@/lib/api").then(({ api }) =>
        api.getActivePromoCodes().then((p) => setAvailablePromos(p)).catch(() => setAvailablePromos([]))
      );
    }
  }, [isOpen]);

  const handleApplyPromo = useCallback(async (codeOverride?: string) => {
    const code = (codeOverride ?? promoCode).trim().replace(/['"]+$|^['"]+/g, "").trim();
    if (!code) {
      setPromoDiscount(null);
      return;
    }
    if (codeOverride) setPromoCode(codeOverride);
    setPromoValidating(true);
    setError("");
    try {
      const { api } = await import("@/lib/api");
      const res = await api.validatePromoCode(code);
      if (res.valid && res.discountPercentage != null) {
        setPromoDiscount(res.discountPercentage);
        showSuccess(`${res.discountPercentage}% discount applied!`);
      } else {
        setPromoDiscount(null);
        setError("Invalid or expired promo code");
      }
    } catch {
      setPromoDiscount(null);
      setError("Invalid or expired promo code");
    } finally {
      setPromoValidating(false);
    }
  }, [promoCode]);

  const nights = Math.max(
    1,
    Math.ceil(
      (new Date(checkOut).getTime() - new Date(checkIn).getTime()) / (1000 * 60 * 60 * 24)
    )
  );
  const roomPrice = selectedRoom ? roomPrices.find((p) => p.roomId === selectedRoom.id) : null;
  const baseTotal = roomPrice
    ? roomPrice.totalForStay
    : (selectedRoom?.basePrice ?? hotelData.price ?? 0) * nights * roomsCount;
  const totalAmount =
    promoDiscount != null && promoDiscount > 0
      ? baseTotal * (1 - promoDiscount / 100)
      : baseTotal;

  const handleReserveAndAddGuests = async () => {
    setError("");
    if (hotelInfoLoading || !hotelInfo?.rooms?.length || !selectedRoom) {
      setError("Please select a room");
      return;
    }
    setLoading(true);
    try {
      const { api } = await import("@/lib/api");
      const sanitizedPromo = promoCode.trim().replace(/['"]+$|^['"]+/g, "").trim() || undefined;
      const booking = await api.initBooking({
        hotelId: hotelData.hotel.id,
        roomId: selectedRoom.id,
        checkInDate: checkIn,
        checkOutDate: checkOut,
        roomsCount,
        promoCode: sanitizedPromo,
      });
      setBookingId(booking.id);
      setStep(2);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Something went wrong";
      setError(msg);
      showApiError(e);
    } finally {
      setLoading(false);
    }
  };

  const addGuestToList = () => {
    const name = newGuest.name.trim();
    const age = parseInt(newGuest.age, 10);
    if (!name) return;
    if (isNaN(age) || age < 1 || age > 120) {
      setError("Please enter a valid age (1-120)");
      return;
    }
    setGuests((g) => [...g, { name, gender: newGuest.gender, age }]);
    setNewGuest({ name: "", gender: "MALE", age: "" });
    setError("");
  };

  const removeGuest = (index: number) => {
    setGuests((g) => g.filter((_, i) => i !== index));
  };

  const handleProceedToPayment = async () => {
    if (!bookingId) return;
    if (guests.length === 0) {
      setError("Please add at least one guest to proceed.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const { api } = await import("@/lib/api");
      await api.addGuests(bookingId, guests);
      const { sessionUrl } = await api.initiatePayment(bookingId);
      if (sessionUrl) window.location.href = sessionUrl;
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Something went wrong";
      setError(msg);
      showApiError(e);
    } finally {
      setLoading(false);
    }
  };

  const rooms = hotelInfo?.rooms ?? [];
  const hasRooms = rooms.length > 0;
  const canProceed = hasRooms && selectedRoom && !hotelInfoLoading;

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            className="fixed inset-0 bg-black/60 backdrop-blur-sm z-[100]"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.div
            className="fixed inset-0 z-[100] flex items-center justify-center p-4 pointer-events-none"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="glass rounded-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto pointer-events-auto"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="p-6">
                <div className="flex justify-between items-start mb-6">
                  <h2 className="text-xl font-bold text-white">
                    {step === 1 ? "Complete Your Booking" : "Add Guest Details"}
                  </h2>
                  <button
                    type="button"
                    onClick={onClose}
                    className="p-2 rounded-lg hover:bg-white/10 transition-colors"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>

                {step === 1 ? (
                  <>
                    <div className="space-y-4 mb-6">
                      <div className="flex justify-between text-sm">
                        <span className="text-white/70">Hotel</span>
                        <span className="text-white font-medium">{hotelData.hotel.name}</span>
                      </div>
                      <div className="flex justify-between text-sm items-center gap-2">
                        <span className="text-white/70 flex items-center gap-1">
                          <Calendar className="w-4 h-4" />
                          Dates
                        </span>
                        <span className="text-white">{checkIn} → {checkOut}</span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-white/70">Rooms × Nights</span>
                        <span className="text-white">
                          {roomsCount} × {nights} nights
                        </span>
                      </div>

                      {hotelInfoLoading && (
                        <div className="py-4 text-center text-white/60 text-sm">
                          Loading room options...
                        </div>
                      )}
                      {!hotelInfoLoading && !hasRooms && (
                        <div className="py-4 px-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
                          No rooms available for this hotel.
                        </div>
                      )}
                      {!hotelInfoLoading && hasRooms && (
                        <div>
                          <label className="block text-sm text-white/70 mb-2 flex items-center gap-1">
                            <Bed className="w-4 h-4" />
                            Select Room
                          </label>
                          {rooms.length === 1 ? (
                            <div className="flex gap-3 px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white items-center">
                              {getFirstImageSrc(rooms[0].photos) ? (
                                /* eslint-disable-next-line @next/next/no-img-element */
                                <img
                                  src={getFirstImageSrc(rooms[0].photos)!}
                                  alt=""
                                  className="w-16 h-14 object-cover rounded-lg flex-shrink-0"
                                />
                              ) : (
                                <div className="w-16 h-14 rounded-lg bg-white/10 flex items-center justify-center flex-shrink-0">
                                  <Bed className="w-6 h-6 text-white/30" />
                                </div>
                              )}
                              <div>
                                {rooms[0].types} — ₹{Math.round(Number(roomPrices.find((p) => p.roomId === rooms[0].id)?.pricePerNight ?? rooms[0].basePrice)).toLocaleString()}/night
                                {rooms[0].capacity ? ` · Capacity: ${rooms[0].capacity}` : ""}
                              </div>
                            </div>
                          ) : (
                            <div className="space-y-2">
                              {rooms.map((r) => (
                                <button
                                  key={r.id}
                                  type="button"
                                  onClick={() => setSelectedRoom(r)}
                                  className={`w-full flex gap-3 px-4 py-3 rounded-xl border text-left transition-all ${
                                    selectedRoom?.id === r.id
                                      ? "bg-[#00FFFF]/10 border-[#00FFFF]/50 text-white"
                                      : "bg-white/5 border-white/10 text-white hover:border-white/20"
                                  }`}
                                >
                                  {getFirstImageSrc(r.photos) ? (
                                    /* eslint-disable-next-line @next/next/no-img-element */
                                    <img
                                      src={getFirstImageSrc(r.photos)!}
                                      alt=""
                                      className="w-16 h-14 object-cover rounded-lg flex-shrink-0"
                                    />
                                  ) : (
                                    <div className="w-16 h-14 rounded-lg bg-white/10 flex items-center justify-center flex-shrink-0">
                                      <Bed className="w-6 h-6 text-white/30" />
                                    </div>
                                  )}
                                  <div className="flex-1 min-w-0">
                                    <span className="font-medium">{r.types}</span>
                                    <span className="text-white/60 text-sm block">
                                      ₹{Math.round(Number(roomPrices.find((p) => p.roomId === r.id)?.pricePerNight ?? r.basePrice)).toLocaleString()}/night
                                      {r.capacity ? ` · Capacity: ${r.capacity}` : ""}
                                    </span>
                                  </div>
                                </button>
                              ))}
                            </div>
                          )}
                        </div>
                      )}

                      <div>
                        <label className="block text-sm text-white/70 mb-2">Promo Code (optional)</label>
                        <div className="flex gap-2">
                          <input
                            type="text"
                            value={promoCode}
                            onChange={(e) => {
                              setPromoCode(e.target.value.toUpperCase());
                              setPromoDiscount(null);
                            }}
                            onKeyDown={(e) => e.key === "Enter" && handleApplyPromo()}
                            placeholder="Enter code"
                            className="flex-1 px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white placeholder:text-white/40"
                          />
                          <button
                            type="button"
                            onClick={() => handleApplyPromo()}
                            disabled={promoValidating || !promoCode.trim()}
                            className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] text-sm font-medium disabled:opacity-50"
                          >
                            {promoValidating ? "..." : "Apply"}
                          </button>
                        </div>
                        {availablePromos.length > 0 && (
                          <div className="flex flex-wrap gap-2 mt-2">
                            <span className="text-white/50 text-xs">Available:</span>
                            {availablePromos.map((p) => (
                              <button
                                key={p.code}
                                type="button"
                                onClick={() => handleApplyPromo(p.code)}
                                className="px-2 py-1 rounded-lg bg-white/10 hover:bg-[#00FFFF]/20 hover:border-[#00FFFF]/50 border border-white/10 text-white/90 text-xs"
                              >
                                {p.code} ({p.discountPercentage}% off)
                              </button>
                            ))}
                          </div>
                        )}
                        {promoDiscount != null && (
                          <p className="text-green-400 text-sm mt-1">{promoDiscount}% off applied</p>
                        )}
                      </div>

                      <div className="flex justify-between text-lg font-bold pt-2 border-t border-white/10">
                        <span className="text-white/80">Total Amount</span>
                        <div className="text-right">
                          {promoDiscount != null && promoDiscount > 0 && (
                            <span className="text-white/50 line-through text-sm block">
                              ₹{Math.round(baseTotal).toLocaleString()}
                            </span>
                          )}
                          <span className="text-[#00FFFF]">₹{Math.round(totalAmount).toLocaleString()}</span>
                        </div>
                      </div>
                    </div>

                    {error && <p className="text-red-400 text-sm mb-4">{error}</p>}

                    <div className="space-y-3">
                      <button
                        type="button"
                        onClick={handleReserveAndAddGuests}
                        disabled={loading || !canProceed}
                        className="w-full py-3 px-6 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] font-semibold flex items-center justify-center gap-2 glow-cyan disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                      >
                        <UserPlus className="w-5 h-5" />
                        {loading ? "Reserving..." : "Reserve & Add Guests"}
                      </button>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="space-y-4 mb-6">
                      <p className="text-white/70 text-sm">
                        Add guest details for your booking. At least one guest is required to proceed.
                      </p>

                      {guests.length > 0 && (
                        <div className="space-y-2">
                          {guests.map((g, i) => (
                            <div
                              key={i}
                              className="flex justify-between items-center px-4 py-2 rounded-xl bg-white/5 border border-white/10"
                            >
                              <span className="text-white">
                                {g.name} · {g.gender} · {g.age} yrs
                              </span>
                              <button
                                type="button"
                                onClick={() => removeGuest(i)}
                                className="text-red-400 hover:text-red-300 text-sm"
                              >
                                Remove
                              </button>
                            </div>
                          ))}
                        </div>
                      )}

                      <div className="p-4 rounded-xl bg-white/5 border border-white/10 space-y-3">
                        <h3 className="text-sm font-medium text-white/80">Add a guest</h3>
                        <div className="grid grid-cols-1 gap-3">
                          <input
                            type="text"
                            value={newGuest.name}
                            onChange={(e) => setNewGuest((g) => ({ ...g, name: e.target.value }))}
                            placeholder="Full name"
                            className="w-full px-4 py-2 rounded-lg bg-white/5 border border-white/10 text-white placeholder:text-white/40"
                          />
                          <div className="flex gap-2">
                            <select
                              value={newGuest.gender}
                              onChange={(e) =>
                                setNewGuest((g) => ({ ...g, gender: e.target.value as GuestGender }))
                              }
                              className="flex-1 px-4 py-2 rounded-lg bg-white/5 border border-white/10 text-white [color-scheme:dark]"
                            >
                              {GENDERS.map((opt) => (
                                <option key={opt.value} value={opt.value}>
                                  {opt.label}
                                </option>
                              ))}
                            </select>
                            <input
                              type="number"
                              min={1}
                              max={120}
                              value={newGuest.age}
                              onChange={(e) => setNewGuest((g) => ({ ...g, age: e.target.value }))}
                              placeholder="Age"
                              className="w-24 px-4 py-2 rounded-lg bg-white/5 border border-white/10 text-white placeholder:text-white/40"
                            />
                          </div>
                          <button
                            type="button"
                            onClick={addGuestToList}
                            className="w-full py-2 rounded-lg bg-white/10 hover:bg-white/20 text-white text-sm font-medium"
                          >
                            Add Guest
                          </button>
                        </div>
                      </div>

                      <div className="flex justify-between text-lg font-bold pt-2 border-t border-white/10">
                        <span className="text-white/80">Total</span>
                        <div className="text-right">
                          {promoDiscount != null && promoDiscount > 0 && (
                            <span className="text-white/50 line-through text-sm block">
                              ₹{Math.round(baseTotal).toLocaleString()}
                            </span>
                          )}
                          <span className="text-[#00FFFF]">₹{Math.round(totalAmount).toLocaleString()}</span>
                        </div>
                      </div>
                    </div>

                    {error && <p className="text-red-400 text-sm mb-4">{error}</p>}

                    <div className="flex gap-3">
                      <button
                        type="button"
                        onClick={() => setStep(1)}
                        className="py-2.5 px-4 rounded-xl bg-white/10 hover:bg-white/20 text-white text-sm font-medium"
                      >
                        Back
                      </button>
                      <button
                        type="button"
                        onClick={handleProceedToPayment}
                        disabled={loading || guests.length === 0}
                        className="flex-1 py-3 px-6 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] font-semibold flex items-center justify-center gap-2 glow-cyan disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                      >
                        <CreditCard className="w-5 h-5" />
                        {loading ? "Redirecting..." : "Proceed to Payment"}
                      </button>
                    </div>
                  </>
                )}
              </div>
            </motion.div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

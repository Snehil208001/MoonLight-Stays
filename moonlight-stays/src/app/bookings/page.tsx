"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/hooks/useAuth";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { api, type BookingDto } from "@/lib/api";
import { ROUTES } from "@/lib/constants";
import { showApiError, showSuccess } from "@/lib/toast";
import { Calendar, MapPin, ArrowLeft, X, Filter } from "lucide-react";

const STATUS_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "PAYMENT_PENDING", label: "Payment Pending" },
  { value: "RESERVED", label: "Reserved" },
  { value: "GUEST_ADDED", label: "Guest Added" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "EXPIRED", label: "Expired" },
];

export default function BookingsPage() {
  const router = useRouter();
  const { isLoggedIn, loading: authLoading } = useAuth();
  const [bookings, setBookings] = useState<BookingDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [detailBooking, setDetailBooking] = useState<BookingDto | null>(null);

  useEffect(() => {
    if (!isLoggedIn) return;
    setLoading(true);
    api
      .getMyBookings(page, 10, statusFilter === "ALL" ? undefined : statusFilter)
      .then(({ content, totalPages: tp }) => {
        setBookings(content);
        setTotalPages(tp);
      })
      .catch((e) => { setBookings([]); showApiError(e); })
      .finally(() => setLoading(false));
  }, [isLoggedIn, page, statusFilter]);

  const handleViewDetail = async (id: number) => {
    try {
      const b = await api.getBookingById(id);
      setDetailBooking(b);
    } catch (e) {
      showApiError(e);
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm("Are you sure you want to cancel this booking? A refund will be processed if applicable.")) return;
    try {
      await api.cancelBooking(id);
      showSuccess("Booking cancelled. Refund will be processed if applicable.");
      setBookings((b) => b.filter((x) => x.id !== id));
      if (detailBooking?.id === id) setDetailBooking(null);
    } catch (e) {
      showApiError(e);
    }
  };

  return (
    <ProtectedRoute>
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
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <h1 className="text-2xl font-bold text-white">My Bookings</h1>
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-white/60" />
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
              className="px-4 py-2 rounded-xl bg-white/10 border border-white/20 text-white text-sm focus:outline-none focus:ring-1 focus:ring-[#00FFFF]/50"
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value} className="bg-[#0a0a1a] text-white">
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="glass rounded-2xl h-32 animate-pulse" />
            ))}
          </div>
        ) : bookings.length === 0 ? (
          <div className="glass rounded-2xl p-12 text-center text-white/60">
            No bookings yet.{" "}
            <Link href={ROUTES.HOME} className="text-[#00FFFF] hover:underline">
              Search for hotels
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {bookings.map((b) => (
              <motion.div
                key={b.id}
                className="glass rounded-2xl p-6 cursor-pointer hover:ring-1 hover:ring-[#00FFFF]/30 transition-all"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                onClick={() => handleViewDetail(b.id)}
              >
                <div className="flex justify-between items-start">
                  <div>
                    <Link
                      href={b.hotel?.id ? `/hotels/${b.hotel.id}` : "#"}
                      className="text-lg font-bold text-white hover:text-[#00FFFF] transition-colors"
                    >
                      {b.hotel?.name}
                    </Link>
                    <p className="text-white/60 flex items-center gap-1 mt-1">
                      <MapPin className="w-4 h-4" />
                      {b.hotel?.city}
                    </p>
                    <p className="text-sm text-white/50 mt-2 flex items-center gap-1">
                      <Calendar className="w-4 h-4" />
                      {b.checkInDate} → {b.checkOutDate}
                    </p>
                    <p className="text-sm text-white/70 mt-1">
                      {b.roomsCount} room(s) · ₹{Math.round(b.amount).toLocaleString()}
                    </p>
                    {b.bookingStatus === "CONFIRMED" && b.hotel?.id && (
                      <Link
                        href={`/hotels/${b.hotel.id}`}
                        className="inline-block mt-2 text-sm text-[#00FFFF] hover:underline"
                      >
                        Add a review
                      </Link>
                    )}
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium ${
                        b.bookingStatus === "CONFIRMED"
                          ? "bg-green-500/20 text-green-400"
                          : b.bookingStatus === "CANCELLED"
                          ? "bg-red-500/20 text-red-400"
                          : "bg-amber-500/20 text-amber-400"
                      }`}
                    >
                      {b.bookingStatus}
                    </span>
                    {b.bookingStatus !== "CANCELLED" &&
                      b.bookingStatus !== "EXPIRED" && (
                        <button
                          onClick={(e) => { e.stopPropagation(); handleCancel(b.id); }}
                          className="text-sm text-red-400 hover:text-red-300"
                        >
                          Cancel
                        </button>
                      )}
                  </div>
                </div>
              </motion.div>
            ))}
            {totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-6">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-4 py-2 rounded-xl bg-white/10 disabled:opacity-50 text-white"
                >
                  Previous
                </button>
                <span className="px-4 py-2 text-white/70">
                  {page + 1} / {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= totalPages - 1}
                  className="px-4 py-2 rounded-xl bg-white/10 disabled:opacity-50 text-white"
                >
                  Next
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      <AnimatePresence>
        {detailBooking && (
          <>
            <motion.div
              className="fixed inset-0 bg-black/60 z-50"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setDetailBooking(null)}
            />
            <motion.div
              className="fixed inset-0 z-50 flex items-center justify-center p-4"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
            >
              <motion.div
                className="glass rounded-2xl p-6 max-w-md w-full max-h-[90vh] overflow-y-auto"
                initial={{ scale: 0.9 }}
                animate={{ scale: 1 }}
                exit={{ scale: 0.9 }}
                onClick={(e) => e.stopPropagation()}
              >
                <div className="flex justify-between items-start mb-4">
                  <h2 className="text-xl font-bold text-white">Booking Details</h2>
                  <button
                    onClick={() => setDetailBooking(null)}
                    className="p-2 rounded-lg hover:bg-white/10 text-white"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
                <div className="space-y-3 text-sm">
                  <div className="flex justify-between">
                    <span className="text-white/60">Hotel</span>
                    <Link
                      href={detailBooking.hotel?.id ? `/hotels/${detailBooking.hotel.id}` : "#"}
                      className="text-[#00FFFF] hover:underline font-medium"
                    >
                      {detailBooking.hotel?.name}
                    </Link>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-white/60">Room</span>
                    <span className="text-white">{detailBooking.room?.types}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-white/60">Check-in</span>
                    <span className="text-white">{detailBooking.checkInDate}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-white/60">Check-out</span>
                    <span className="text-white">{detailBooking.checkOutDate}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-white/60">Rooms</span>
                    <span className="text-white">{detailBooking.roomsCount}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-white/60">Amount</span>
                    <span className="text-[#00FFFF] font-bold">₹{Math.round(detailBooking.amount).toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-white/60">Status</span>
                    <span
                      className={`px-2 py-0.5 rounded text-xs font-medium ${
                        detailBooking.bookingStatus === "CONFIRMED"
                          ? "bg-green-500/20 text-green-400"
                          : detailBooking.bookingStatus === "CANCELLED"
                          ? "bg-red-500/20 text-red-400"
                          : "bg-amber-500/20 text-amber-400"
                      }`}
                    >
                      {detailBooking.bookingStatus}
                    </span>
                  </div>
                </div>
                {detailBooking.bookingStatus !== "CANCELLED" &&
                  detailBooking.bookingStatus !== "CONFIRMED" &&
                  detailBooking.bookingStatus !== "EXPIRED" && (
                  <div className="mt-4 flex gap-2">
                    <button
                      onClick={() => handleCancel(detailBooking.id)}
                      className="flex-1 py-2 rounded-xl bg-red-500/20 border border-red-500/50 text-red-400 text-sm font-medium"
                    >
                      Cancel Booking
                    </button>
                  </div>
                )}
              </motion.div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </main>
    </ProtectedRoute>
  );
}

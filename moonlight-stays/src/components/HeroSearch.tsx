"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Search, MapPin, Calendar, Bed, ChevronDown, ChevronUp, IndianRupee } from "lucide-react";

export interface SearchParams {
  city: string;
  checkInDate: string;
  checkOutDate: string;
  roomsCount: number;
  minPrice?: number;
  maxPrice?: number;
  roomType?: string;
  amenity?: string;
}

interface HeroSearchProps {
  onSearch: (params: SearchParams) => void;
  isLoading?: boolean;
  /** Restore search from URL when user navigates back */
  initialValues?: Partial<SearchParams>;
}

const today = new Date().toISOString().split("T")[0];
const tomorrow = new Date(Date.now() + 86400000).toISOString().split("T")[0];

export function HeroSearch({ onSearch, isLoading, initialValues }: HeroSearchProps) {
  const [city, setCity] = useState(initialValues?.city ?? "");
  const [checkIn, setCheckIn] = useState(initialValues?.checkInDate ?? today);
  const [checkOut, setCheckOut] = useState(initialValues?.checkOutDate ?? tomorrow);
  const [rooms, setRooms] = useState(initialValues?.roomsCount ?? 1);
  const [showFilters, setShowFilters] = useState(false);
  const [minPrice, setMinPrice] = useState(initialValues?.minPrice != null ? String(initialValues.minPrice) : "");
  const [maxPrice, setMaxPrice] = useState(initialValues?.maxPrice != null ? String(initialValues.maxPrice) : "");
  const [roomType, setRoomType] = useState(initialValues?.roomType ?? "");
  const [amenity, setAmenity] = useState(initialValues?.amenity ?? "");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch({
      city: city.trim(),
      checkInDate: checkIn,
      checkOutDate: checkOut,
      roomsCount: rooms,
      minPrice: minPrice ? parseInt(minPrice, 10) : undefined,
      maxPrice: maxPrice ? parseInt(maxPrice, 10) : undefined,
      roomType: roomType.trim() || undefined,
      amenity: amenity.trim() || undefined,
    });
  };

  const hasFilters = minPrice || maxPrice || roomType.trim() || amenity.trim();

  return (
    <motion.form
      onSubmit={handleSubmit}
      className="glass rounded-2xl p-6 md:p-8 max-w-4xl w-full mx-auto"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, delay: 0.2 }}
    >
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 items-end">
        <div className="lg:col-span-2">
          <label className="block text-sm text-white/70 mb-2">City</label>
          <div className="relative">
            <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-white/50" />
            <input
              type="text"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="e.g. Mumbai, Delhi (leave empty for all)"
              className="w-full pl-10 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 focus:ring-1 focus:ring-[#00FFFF]/30 outline-none transition-all text-white placeholder:text-white/40"
            />
          </div>
        </div>
        <div>
          <label className="block text-sm text-white/70 mb-2">Check-in</label>
          <div className="relative">
            <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-white/50" />
            <input
              type="date"
              value={checkIn}
              min={today}
              onChange={(e) => setCheckIn(e.target.value)}
              className="w-full pl-10 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 focus:ring-1 focus:ring-[#00FFFF]/30 outline-none transition-all text-white [color-scheme:dark]"
            />
          </div>
        </div>
        <div>
          <label className="block text-sm text-white/70 mb-2">Check-out</label>
          <div className="relative">
            <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-white/50" />
            <input
              type="date"
              value={checkOut}
              min={checkIn}
              onChange={(e) => setCheckOut(e.target.value)}
              className="w-full pl-10 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 focus:ring-1 focus:ring-[#00FFFF]/30 outline-none transition-all text-white [color-scheme:dark]"
            />
          </div>
        </div>
        <div>
          <label className="block text-sm text-white/70 mb-2">Rooms</label>
          <div className="relative">
            <Bed className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-white/50" />
            <input
              type="number"
              min={1}
              max={10}
              value={rooms}
              onChange={(e) => setRooms(Math.max(1, parseInt(e.target.value) || 1))}
              className="w-full pl-10 pr-4 py-3 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 focus:ring-1 focus:ring-[#00FFFF]/30 outline-none transition-all text-white"
            />
          </div>
        </div>
        <div className="lg:col-span-1 flex items-end">
          <motion.button
            type="submit"
            disabled={isLoading}
            className="w-full py-3 px-6 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] font-semibold flex items-center justify-center gap-2 glow-cyan disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            {isLoading ? (
              <span className="animate-pulse">Searching...</span>
            ) : (
              <>
                <Search className="w-5 h-5" />
                Search
              </>
            )}
          </motion.button>
        </div>
      </div>

      <div className="mt-4 pt-4 border-t border-white/10">
        <button
          type="button"
          onClick={() => setShowFilters(!showFilters)}
          className="flex items-center gap-2 text-white/70 hover:text-white text-sm font-medium transition-colors"
        >
          {showFilters ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          More filters
          {hasFilters && (
            <span className="px-2 py-0.5 rounded-full bg-[#00FFFF]/20 text-[#00FFFF] text-xs">
              Active
            </span>
          )}
        </button>
        <AnimatePresence>
          {showFilters && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: "auto", opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.2 }}
              className="overflow-hidden"
            >
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
                <div>
                  <label className="block text-sm text-white/70 mb-2 flex items-center gap-1">
                    <IndianRupee className="w-4 h-4" />
                    Min price (₹/night)
                  </label>
                  <input
                    type="number"
                    min={0}
                    value={minPrice}
                    onChange={(e) => setMinPrice(e.target.value)}
                    placeholder="e.g. 1000"
                    className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white placeholder:text-white/40"
                  />
                </div>
                <div>
                  <label className="block text-sm text-white/70 mb-2 flex items-center gap-1">
                    <IndianRupee className="w-4 h-4" />
                    Max price (₹/night)
                  </label>
                  <input
                    type="number"
                    min={0}
                    value={maxPrice}
                    onChange={(e) => setMaxPrice(e.target.value)}
                    placeholder="e.g. 5000"
                    className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white placeholder:text-white/40"
                  />
                </div>
                <div>
                  <label className="block text-sm text-white/70 mb-2 flex items-center gap-1">
                    <Bed className="w-4 h-4" />
                    Room type
                  </label>
                  <input
                    type="text"
                    value={roomType}
                    onChange={(e) => setRoomType(e.target.value)}
                    placeholder="e.g. Deluxe, Suite"
                    className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white placeholder:text-white/40"
                  />
                </div>
                <div>
                  <label className="block text-sm text-white/70 mb-2">Amenity</label>
                  <input
                    type="text"
                    value={amenity}
                    onChange={(e) => setAmenity(e.target.value)}
                    placeholder="e.g. Pool, WiFi"
                    className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus:border-[#00FFFF]/50 outline-none text-white placeholder:text-white/40"
                  />
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.form>
  );
}

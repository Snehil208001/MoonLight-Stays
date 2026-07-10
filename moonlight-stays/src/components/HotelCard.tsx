"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { MapPin, Zap, Heart } from "lucide-react";
import type { HotelPriceDto } from "@/lib/api";
import { getFirstImageSrc } from "@/lib/imageUtils";

interface HotelCardProps {
  hotel: HotelPriceDto;
  onBook: (hotel: HotelPriceDto) => void;
  searchParams?: { checkInDate: string; checkOutDate: string; roomsCount: number } | null;
  isFavorite?: boolean;
  onToggleFavorite?: (hotelId: number) => void;
  isLoggedIn?: boolean;
  /** Discount percentage (0-100) from valid promo code - shows discounted price */
  discountPercentage?: number;
}

export function HotelCard({ hotel, onBook, searchParams, isFavorite, onToggleFavorite, isLoggedIn, discountPercentage }: HotelCardProps) {
  const imgUrl = getFirstImageSrc(hotel.hotel.photos);
  const basePrice = hotel.hotel.basePrice ?? 0;
  const isSurge = hotel.hotel.dynamicPricingActive ?? (basePrice > 0 && hotel.price > basePrice * 1.1);
  const displayPrice =
    discountPercentage != null && discountPercentage > 0
      ? hotel.price * (1 - discountPercentage / 100)
      : hotel.price;

  const detailHref = searchParams
    ? `/hotels/${hotel.hotel.id}?checkIn=${searchParams.checkInDate}&checkOut=${searchParams.checkOutDate}&rooms=${searchParams.roomsCount}`
    : `/hotels/${hotel.hotel.id}`;

  return (
    <motion.div
      className="glass rounded-2xl overflow-hidden flex flex-col"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      <Link href={detailHref} className="block flex-shrink-0 group">
        <div className="relative h-48 overflow-hidden bg-white/5">
          {imgUrl ? (
            /* eslint-disable-next-line @next/next/no-img-element */
            <img
              src={imgUrl}
              alt={hotel.hotel.name}
              className="absolute inset-0 w-full h-full object-cover object-center transition-transform hover:scale-105"
              onError={(e) => {
                (e.target as HTMLImageElement).style.display = "none";
              }}
            />
          ) : (
            <div className="absolute inset-0 flex items-center justify-center text-white/30 text-sm">
              No image
            </div>
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/30 to-transparent pointer-events-none" />
          {isLoggedIn && onToggleFavorite && (
            <button
              type="button"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onToggleFavorite(hotel.hotel.id);
              }}
              className="absolute top-3 left-3 p-2 rounded-full transition-colors z-10 pointer-events-auto"
            >
              <Heart
                className={`w-5 h-5 ${isFavorite ? "fill-red-400 text-red-400" : "text-white"}`}
              />
            </button>
          )}
          {isSurge && (
            <div className="absolute top-3 right-3 px-3 py-1 rounded-full bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-xs font-semibold flex items-center gap-1 pointer-events-none">
              <Zap className="w-3 h-3" />
              Surge Pricing
            </div>
          )}
          <div className="absolute bottom-0 left-0 right-0 p-3 pt-8 bg-gradient-to-t from-black/95 to-transparent">
            <h3 className="text-lg font-bold text-white drop-shadow-md group-hover:underline">
              {hotel.hotel.name}
            </h3>
            <p className="text-sm text-white/90 flex items-center gap-1 mt-0.5">
              <MapPin className="w-4 h-4 flex-shrink-0" />
              <span>{hotel.hotel.city}</span>
            </p>
          </div>
        </div>
      </Link>
      <div className="p-4 flex items-center justify-between flex-1 min-h-0">
        <div>
          {discountPercentage != null && discountPercentage > 0 ? (
            <div className="flex items-baseline gap-2">
              <span className="text-white/50 line-through text-sm">
                ₹{Math.round(hotel.price).toLocaleString()}
              </span>
              <span className="text-2xl font-bold text-[#00FFFF]">
                ₹{Math.round(displayPrice).toLocaleString()}
              </span>
              <span className="text-green-400 text-xs font-medium">
                {discountPercentage}% off
              </span>
            </div>
          ) : (
            <span className="text-2xl font-bold text-[#00FFFF]">
              ₹{Math.round(displayPrice).toLocaleString()}
            </span>
          )}
          <span className="text-white/60 text-sm ml-1">/ night</span>
        </div>
        <button
          type="button"
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onBook(hotel);
          }}
          className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] text-sm font-medium glow-cyan transition-all cursor-pointer active:scale-95"
        >
          Book Now
        </button>
      </div>
    </motion.div>
  );
}

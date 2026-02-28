"use client";

import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, Bed, Users, Wifi } from "lucide-react";
import { getImageSrc } from "@/lib/imageUtils";
import type { RoomDto } from "@/lib/api";

interface RoomDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  room: RoomDto;
  hotelName: string;
  price: number;
  onBookNow: () => void;
}

export function RoomDetailModal({
  isOpen,
  onClose,
  room,
  hotelName,
  price,
  onBookNow,
}: RoomDetailModalProps) {
  const [selectedPhotoIndex, setSelectedPhotoIndex] = useState(0);
  const amenities = room.amenities ?? [];
  const photos = room.photos ?? [];

  useEffect(() => {
    setSelectedPhotoIndex(0);
  }, [room.id]);

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="glass rounded-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto"
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          onClick={(e) => e.stopPropagation()}
        >
          <div className="p-6">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h2 className="text-xl font-bold text-white">{room.types}</h2>
                <p className="text-white/60 text-sm">{hotelName}</p>
              </div>
              <button
                type="button"
                onClick={onClose}
                className="p-2 rounded-lg hover:bg-white/10 text-white/70 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {photos.length > 0 && (
              <div className="mb-4 -mx-6">
                <div className="relative w-full h-48 sm:h-56 bg-white/5 rounded-t-2xl overflow-hidden">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={getImageSrc(photos[selectedPhotoIndex])}
                    alt={`${room.types} ${selectedPhotoIndex + 1}`}
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      const el = e.target as HTMLImageElement;
                      el.style.display = "none";
                    }}
                  />
                </div>
                {photos.length > 1 && (
                  <div className="flex gap-2 p-4 overflow-x-auto bg-white/5">
                    {photos.map((url, i) => (
                      <button
                        key={i}
                        type="button"
                        onClick={() => setSelectedPhotoIndex(i)}
                        className={`flex-shrink-0 rounded-lg overflow-hidden transition-all ${
                          selectedPhotoIndex === i ? "ring-2 ring-[#00FFFF]" : "hover:ring-2 hover:ring-[#00FFFF]/50"
                        }`}
                      >
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img
                          src={getImageSrc(url)}
                          alt={`${room.types} ${i + 1}`}
                          className="w-20 h-16 object-cover"
                        />
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}

            <div className="flex items-center gap-4 mb-4 text-sm text-white/70">
              <span className="flex items-center gap-1.5">
                <Users className="w-4 h-4" />
                Capacity: {room.capacity}
              </span>
              <span className="flex items-center gap-1.5">
                <Bed className="w-4 h-4" />
                {room.totalCount} available
              </span>
            </div>

            {amenities.length > 0 && (
              <div className="mb-6">
                <h3 className="text-sm font-medium text-white/80 mb-2 flex items-center gap-2">
                  <Wifi className="w-4 h-4" />
                  Amenities
                </h3>
                <div className="flex flex-wrap gap-2">
                  {amenities.map((a, i) => (
                    <span
                      key={i}
                      className="px-3 py-1.5 rounded-lg bg-white/10 text-white/90 text-sm"
                    >
                      {a}
                    </span>
                  ))}
                </div>
              </div>
            )}

            <div className="flex items-center justify-between pt-4 border-t border-white/10">
              <div>
                <span className="text-2xl font-bold text-[#00FFFF]">
                  ₹{Math.round(price).toLocaleString()}
                </span>
                <span className="text-white/60 text-sm ml-1">/ night</span>
              </div>
              <button
                type="button"
                onClick={onBookNow}
                className="px-6 py-2.5 rounded-xl bg-[#00FFFF]/20 hover:bg-[#00FFFF]/30 border border-[#00FFFF]/50 text-[#00FFFF] font-medium"
              >
                Book Now
              </button>
            </div>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}

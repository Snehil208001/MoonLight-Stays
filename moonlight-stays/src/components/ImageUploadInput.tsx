"use client";

import { useState } from "react";
import { Link2, X } from "lucide-react";
import { getImageSrc, extractDirectImageUrl } from "@/lib/imageUtils";

interface ImageUploadInputProps {
  value: string[];
  onChange: (urls: string[]) => void;
  size?: "sm" | "md";
}

export function ImageUploadInput({ value, onChange, size = "md" }: ImageUploadInputProps) {
  const [urlInput, setUrlInput] = useState("");

  const photos = Array.isArray(value) ? value : [];
  const boxClass = size === "sm" ? "w-16 h-16" : "w-20 h-20";
  const imgClass = size === "sm" ? "w-16 h-16" : "w-20 h-20";

  const handleAddUrl = () => {
    const raw = urlInput.trim();
    if (!raw) return;
    const url = extractDirectImageUrl(raw);
    if (!url) return;
    onChange([...photos, url]);
    setUrlInput("");
  };

  return (
    <div className="flex flex-col gap-2 min-h-[88px]">
      <div className="flex flex-wrap gap-2 items-start">
        {photos.map((url, i) => (
          <div key={`${url}-${i}`} className={`relative ${boxClass}`}>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={getImageSrc(url)}
              alt=""
              className={`${imgClass} object-cover rounded-lg border border-white/10`}
              onError={(e) => {
                const el = e.target as HTMLImageElement;
                el.onerror = null;
                el.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='80' viewBox='0 0 80 80'%3E%3Crect width='80' height='80' fill='%23334'/%3E%3Cpath d='M25 55 L40 40 L55 55 L55 25 L25 25 Z' fill='none' stroke='%23555' stroke-width='1.5'/%3E%3Ccircle cx='35' cy='32' r='5' fill='%23555'/%3E%3C/svg%3E";
              }}
            />
            <button
              type="button"
              onClick={() => onChange(photos.filter((_, j) => j !== i))}
              className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center hover:bg-red-400"
            >
              <X className="w-3 h-3" />
            </button>
          </div>
        ))}
      </div>
      <div className="flex gap-2">
        <input
          type="text"
          value={urlInput}
          onChange={(e) => setUrlInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && (e.preventDefault(), handleAddUrl())}
          placeholder="Paste image URL (Google Images links work too)"
          className="flex-1 px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm placeholder:text-white/40"
        />
        <button
          type="button"
          onClick={handleAddUrl}
          disabled={!urlInput.trim()}
          className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Link2 className="w-4 h-4" />
          Add
        </button>
      </div>
    </div>
  );
}

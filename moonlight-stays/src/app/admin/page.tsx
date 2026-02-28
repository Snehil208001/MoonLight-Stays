"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { Navbar } from "@/components/Navbar";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { useAuth } from "@/hooks/useAuth";
import { api, type Hotel, type RoomDto } from "@/lib/api";
import { ROUTES } from "@/lib/constants";
import { showApiError, showSuccess } from "@/lib/toast";
import { Plus, Building2, Bed, ArrowLeft, ToggleLeft, ToggleRight, Tag, Pencil, Trash2, X } from "lucide-react";
import { ImageUploadInput } from "@/components/ImageUploadInput";
import { getImageSrc } from "@/lib/imageUtils";

export default function AdminPage() {
  const { isHotelManager } = useAuth();
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddHotel, setShowAddHotel] = useState(false);
  const [showAddRoom, setShowAddRoom] = useState<number | null>(null);
  const [editingRoom, setEditingRoom] = useState<{ hotelId: number; room: RoomDto } | null>(null);
  const [showPromo, setShowPromo] = useState(false);
  const [showSurge, setShowSurge] = useState<number | null>(null);
  const [editingHotel, setEditingHotel] = useState<Hotel | null>(null);
  const [roomsByHotel, setRoomsByHotel] = useState<Record<number, RoomDto[]>>({});
  const [deleteConfirm, setDeleteConfirm] = useState<{ type: "hotel" | "room" | "promo"; hotelId?: number; roomId?: number; promoId?: number } | null>(null);
  const [promoCodes, setPromoCodes] = useState<{ id: number; code: string; discountPercentage: number; active: boolean }[]>([]);
  const [newHotel, setNewHotel] = useState({
    name: "",
    city: "",
    amenities: "",
    photos: [] as string[],
    contactInfo: { address: "", phoneNumber: "", email: "" },
  });
  const [newRoom, setNewRoom] = useState({
    types: "",
    basePrice: 0,
    totalCount: 1,
    capacity: 2,
    amenities: "",
    photos: [] as string[],
  });
  const [newPromo, setNewPromo] = useState({ code: "", discountPercentage: 20, active: true });
  const [surgeData, setSurgeData] = useState({ surgeFactor: 1.2, startDate: "", endDate: "" });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isHotelManager) return;
    (async () => {
      try {
        const [hotelsData, promosData] = await Promise.all([
          api.getMyHotels(),
          api.getPromoCodes(),
        ]);
        const list = Array.isArray(hotelsData) ? hotelsData : [];
        setHotels(list);
        setPromoCodes(Array.isArray(promosData) ? promosData : []);
        list.forEach((h) =>
          api.getHotelRooms(h.id)
            .then((r) => setRoomsByHotel((prev) => ({ ...prev, [h.id]: r ?? [] })))
            .catch(() => {})
        );
      } catch (e) {
        setHotels([]);
        setPromoCodes([]);
        showApiError(e);
      } finally {
        setLoading(false);
      }
    })();
  }, [isHotelManager]);

  const loadRooms = useCallback(async (hotelId: number) => {
    try {
      const rooms = await api.getHotelRooms(hotelId);
      setRoomsByHotel((prev) => ({ ...prev, [hotelId]: rooms ?? [] }));
    } catch {
      setRoomsByHotel((prev) => ({ ...prev, [hotelId]: [] }));
    }
  }, []);

  const handleCreateHotel = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const amenities = newHotel.amenities.split(",").map((s) => s.trim()).filter(Boolean);
      const hotel = await api.createHotel({
        name: newHotel.name,
        city: newHotel.city,
        amenities: amenities.length ? amenities : undefined,
        photos: newHotel.photos.length ? newHotel.photos : undefined,
        contactInfo: newHotel.contactInfo,
      });
      setHotels((h) => [...h, hotel]);
      showSuccess("Hotel created successfully.");
      setShowAddHotel(false);
      setNewHotel({ name: "", city: "", amenities: "", photos: [], contactInfo: { address: "", phoneNumber: "", email: "" } });
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to create hotel";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleEditHotel = async (hotel: Hotel) => {
    setError("");
    setEditingHotel(hotel);
    setNewHotel({
      name: hotel.name,
      city: hotel.city,
      amenities: (hotel.amenities ?? []).join(", "),
      photos: Array.isArray(hotel.photos) ? hotel.photos : [],
      contactInfo: {
        address: hotel.contactInfo?.address ?? "",
        phoneNumber: hotel.contactInfo?.phoneNumber ?? "",
        email: hotel.contactInfo?.email ?? "",
      },
    });
    try {
      const fullHotel = await api.getHotelById(hotel.id);
      setNewHotel({
        name: fullHotel.name,
        city: fullHotel.city,
        amenities: (fullHotel.amenities ?? []).join(", "),
        photos: Array.isArray(fullHotel.photos) ? fullHotel.photos : [],
        contactInfo: {
          address: fullHotel.contactInfo?.address ?? "",
          phoneNumber: fullHotel.contactInfo?.phoneNumber ?? "",
          email: fullHotel.contactInfo?.email ?? "",
        },
      });
    } catch (e) {
      showApiError(e);
    }
  };

  const handleUpdateHotel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingHotel) return;
    setSubmitting(true);
    setError("");
    try {
      const amenities = newHotel.amenities.split(",").map((s) => s.trim()).filter(Boolean);
      const updated = await api.updateHotel(editingHotel.id, {
        name: newHotel.name,
        city: newHotel.city,
        amenities: amenities.length ? amenities : undefined,
        photos: newHotel.photos.length ? newHotel.photos : undefined,
        contactInfo: newHotel.contactInfo,
      });
      setHotels((h) => h.map((x) => (x.id === updated.id ? updated : x)));
      showSuccess("Hotel updated successfully.");
      setEditingHotel(null);
      setNewHotel({ name: "", city: "", amenities: "", photos: [], contactInfo: { address: "", phoneNumber: "", email: "" } });
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to update hotel";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteHotel = async () => {
    if (!deleteConfirm || deleteConfirm.type !== "hotel" || deleteConfirm.hotelId == null) return;
    const hotelId = deleteConfirm.hotelId;
    setSubmitting(true);
    setError("");
    try {
      await api.deleteHotel(hotelId);
      setHotels((h) => h.filter((x) => x.id !== hotelId));
      showSuccess("Hotel deleted.");
      setRoomsByHotel((prev) => {
        const next = { ...prev };
        delete next[hotelId];
        return next;
      });
      setDeleteConfirm(null);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to delete hotel";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteRoom = async () => {
    if (!deleteConfirm || deleteConfirm.type !== "room" || deleteConfirm.hotelId == null || deleteConfirm.roomId == null) return;
    const { hotelId, roomId } = deleteConfirm;
    setSubmitting(true);
    setError("");
    try {
      await api.deleteRoom(hotelId, roomId);
      setRoomsByHotel((prev) => ({
        ...prev,
        [hotelId]: (prev[hotelId] ?? []).filter((r) => r.id !== roomId),
      }));
      showSuccess("Room deleted.");
      setDeleteConfirm(null);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to delete room";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleEditRoom = async (hotel: Hotel, room: RoomDto) => {
    setError("");
    setEditingRoom({ hotelId: hotel.id, room });
    setNewRoom({
      types: room.types,
      basePrice: Number(room.basePrice) || 0,
      totalCount: room.totalCount ?? 1,
      capacity: room.capacity ?? 2,
      amenities: Array.isArray(room.amenities) ? room.amenities.join(", ") : "",
      photos: Array.isArray(room.photos) ? room.photos : [],
    });
    setShowAddRoom(null);
  };

  const handleUpdateRoom = async (e: React.FormEvent, hotelId: number, roomId: number) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const amenities = newRoom.amenities.split(",").map((s) => s.trim()).filter(Boolean);
      await api.updateRoom(hotelId, roomId, {
        types: newRoom.types,
        basePrice: newRoom.basePrice,
        totalCount: newRoom.totalCount,
        capacity: newRoom.capacity,
        amenities: amenities.length ? amenities : undefined,
        photos: newRoom.photos.length ? newRoom.photos : undefined,
      });
      await loadRooms(hotelId);
      showSuccess("Room updated successfully.");
      setEditingRoom(null);
      setNewRoom({ types: "", basePrice: 0, totalCount: 1, capacity: 2, amenities: "", photos: [] });
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to update room";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreateRoom = async (e: React.FormEvent, hotelId: number) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const amenities = newRoom.amenities.split(",").map((s) => s.trim()).filter(Boolean);
      await api.createRoom(hotelId, {
        types: newRoom.types,
        basePrice: newRoom.basePrice,
        totalCount: newRoom.totalCount,
        capacity: newRoom.capacity,
        amenities: amenities.length ? amenities : undefined,
        photos: newRoom.photos.length ? newRoom.photos : undefined,
      });
      const updated = await api.getMyHotels();
      setHotels(Array.isArray(updated) ? updated : []);
      setShowAddRoom(null);
      loadRooms(hotelId);
      showSuccess("Room created successfully.");
      setNewRoom({ types: "", basePrice: 0, totalCount: 1, capacity: 2, amenities: "", photos: [] });
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to create room";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleHotel = async (hotelId: number) => {
    try {
      await api.toggleHotelStatus(hotelId);
      const updated = await api.getMyHotels();
      setHotels(Array.isArray(updated) ? updated : []);
      showSuccess("Hotel status updated.");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to toggle");
      showApiError(e);
    }
  };

  const handleCreatePromo = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      await api.createPromoCode(newPromo);
      showSuccess("Promo code created.");
      setShowPromo(false);
      setNewPromo({ code: "", discountPercentage: 20, active: true });
      const updated = await api.getPromoCodes();
      setPromoCodes(Array.isArray(updated) ? updated : []);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to create promo";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeletePromo = async (id: number) => {
    setSubmitting(true);
    try {
      await api.deletePromoCode(id);
      showSuccess("Promo code deleted.");
      setPromoCodes((p) => p.filter((x) => x.id !== id));
      setDeleteConfirm(null);
    } catch (e) {
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdateSurge = async (e: React.FormEvent, hotelId: number) => {
    e.preventDefault();
    if (!surgeData.startDate || !surgeData.endDate) return;
    setSubmitting(true);
    setError("");
    try {
      await api.updateSurgeFactor(hotelId, surgeData.surgeFactor, surgeData.startDate, surgeData.endDate);
      showSuccess("Surge pricing updated.");
      setShowSurge(null);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to update surge";
      setError(msg);
      showApiError(e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <ProtectedRoute requireHotelManager>
    {loading ? (
      <main className="min-h-screen mesh-gradient-bg flex items-center justify-center">
        <div className="text-white/70">Loading...</div>
      </main>
    ) : (
    <main className="min-h-screen mesh-gradient-bg">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-8">
        <Link href={ROUTES.HOME} className="inline-flex items-center gap-2 text-white/70 hover:text-white mb-6">
          <ArrowLeft className="w-4 h-4" />
          Back to Search
        </Link>
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-2xl font-bold text-white">Admin Dashboard</h1>
          <div className="flex gap-2">
            <motion.button
              onClick={() => { setError(""); setShowAddHotel(true); setEditingHotel(null); }}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-medium"
            >
              <Plus className="w-5 h-5" />
              Add Hotel
            </motion.button>
            <motion.button
              onClick={() => setShowPromo(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl glass hover:bg-white/10 text-white"
            >
              <Tag className="w-5 h-5" />
              Add Promo
            </motion.button>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-4 rounded-xl bg-red-500/20 border border-red-500/50 text-red-400 text-sm">
            {error}
          </div>
        )}

        {(showAddHotel || editingHotel) && (
          <motion.div className="glass rounded-2xl p-6 mb-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
            <h2 className="text-lg font-bold text-white mb-4">{editingHotel ? "Edit Hotel" : "Add New Hotel"}</h2>
            <form onSubmit={editingHotel ? handleUpdateHotel : handleCreateHotel} className="space-y-4">
              <div>
                <label className="block text-sm text-white/70 mb-2">Hotel Name</label>
                <input
                  type="text"
                  value={newHotel.name}
                  onChange={(e) => setNewHotel((h) => ({ ...h, name: e.target.value }))}
                  required
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-2">City</label>
                <input
                  type="text"
                  value={newHotel.city}
                  onChange={(e) => setNewHotel((h) => ({ ...h, city: e.target.value }))}
                  required
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-2">Amenities (comma-separated)</label>
                <input
                  type="text"
                  value={newHotel.amenities}
                  onChange={(e) => setNewHotel((h) => ({ ...h, amenities: e.target.value }))}
                  placeholder="Free WiFi, Pool, Spa"
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div className="min-h-[100px]">
                <label className="block text-sm text-white/70 mb-2">Photos (add image URLs — Google Images links work too)</label>
                <ImageUploadInput
                  value={newHotel.photos}
                  onChange={(photos) => {
                    setNewHotel((h) => ({ ...h, photos }));
                    setError("");
                  }}
                  size="md"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-2">Address</label>
                <input
                  type="text"
                  value={newHotel.contactInfo.address}
                  onChange={(e) =>
                    setNewHotel((h) => ({ ...h, contactInfo: { ...h.contactInfo, address: e.target.value } }))
                  }
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-2">Phone</label>
                <input
                  type="text"
                  value={newHotel.contactInfo.phoneNumber}
                  onChange={(e) =>
                    setNewHotel((h) => ({ ...h, contactInfo: { ...h.contactInfo, phoneNumber: e.target.value } }))
                  }
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-2">Email</label>
                <input
                  type="email"
                  value={newHotel.contactInfo.email}
                  onChange={(e) =>
                    setNewHotel((h) => ({ ...h, contactInfo: { ...h.contactInfo, email: e.target.value } }))
                  }
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] disabled:opacity-50"
                >
                  {submitting ? (editingHotel ? "Saving..." : "Creating...") : editingHotel ? "Save" : "Create Hotel"}
                </button>
                <button
                  type="button"
                  onClick={() => { setShowAddHotel(false); setEditingHotel(null); }}
                  className="px-4 py-2 rounded-xl bg-white/10 text-white"
                >
                  Cancel
                </button>
              </div>
            </form>
          </motion.div>
        )}

        {showPromo && (
          <motion.div className="glass rounded-2xl p-6 mb-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
            <h2 className="text-lg font-bold text-white mb-4">Create Promo Code</h2>
            <form onSubmit={handleCreatePromo} className="space-y-4">
              <div>
                <label className="block text-sm text-white/70 mb-2">Code</label>
                <input
                  type="text"
                  value={newPromo.code}
                  onChange={(e) => setNewPromo((p) => ({ ...p, code: e.target.value.toUpperCase() }))}
                  required
                  placeholder="SAVE20"
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-2">Discount %</label>
                <input
                  type="number"
                  min={1}
                  max={100}
                  value={newPromo.discountPercentage}
                  onChange={(e) => setNewPromo((p) => ({ ...p, discountPercentage: parseInt(e.target.value) || 0 }))}
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white"
                />
              </div>
              <div className="flex gap-2">
                <button type="submit" disabled={submitting} className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] disabled:opacity-50">
                  {submitting ? "Creating..." : "Create"}
                </button>
                <button type="button" onClick={() => setShowPromo(false)} className="px-4 py-2 rounded-xl bg-white/10 text-white">
                  Cancel
                </button>
              </div>
            </form>
          </motion.div>
        )}

        {promoCodes.length > 0 && (
          <motion.div className="glass rounded-2xl p-6 mb-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
            <h2 className="text-lg font-bold text-white mb-4">Promo Codes</h2>
            <div className="space-y-2">
              {promoCodes.map((p) => (
                <div
                  key={p.id}
                  className="flex items-center justify-between py-2 px-3 rounded-xl bg-white/5 border border-white/10"
                >
                  <div className="flex items-center gap-3">
                    <Tag className="w-4 h-4 text-[#00FFFF]/70" />
                    <span className="font-mono text-white">{p.code}</span>
                    <span className="text-white/60">{p.discountPercentage}% off</span>
                    <span
                      className={`px-2 py-0.5 rounded text-xs ${p.active ? "bg-green-500/20 text-green-400" : "bg-amber-500/20 text-amber-400"}`}
                    >
                      {p.active ? "Active" : "Inactive"}
                    </span>
                  </div>
                  <button
                    onClick={() => setDeleteConfirm({ type: "promo", promoId: p.id })}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-500/20 hover:bg-red-500/30 text-red-400 text-sm"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete
                  </button>
                </div>
              ))}
            </div>
          </motion.div>
        )}

        <div className="space-y-4">
          {hotels.length === 0 && !showAddHotel ? (
            <div className="glass rounded-2xl p-12 text-center text-white/60">
              No hotels yet. Click &quot;Add Hotel&quot; to create your first property.
            </div>
          ) : (
            hotels.map((hotel) => (
              <motion.div key={hotel.id} className="glass rounded-2xl p-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                <div className="flex justify-between items-start">
                  <div className="flex items-center gap-3">
                    <Building2 className="w-8 h-8 text-[#00FFFF]/70" />
                    <div>
                      <h3 className="text-lg font-bold text-white">{hotel.name}</h3>
                      <p className="text-white/60">{hotel.city}</p>
                      <span
                        className={`inline-block mt-2 px-2 py-0.5 rounded text-xs ${
                          hotel.active ? "bg-green-500/20 text-green-400" : "bg-amber-500/20 text-amber-400"
                        }`}
                      >
                        {hotel.active ? "Active" : "Inactive"}
                      </span>
                    </div>
                  </div>
                  <div className="flex gap-2 flex-wrap">
                    <button
                      onClick={() => handleEditHotel(hotel)}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white text-sm"
                    >
                      <Pencil className="w-4 h-4" />
                      Edit
                    </button>
                    <button
                      onClick={() => setShowSurge(showSurge === hotel.id ? null : hotel.id)}
                      className="px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white text-sm"
                    >
                      Surge
                    </button>
                    <button
                      onClick={() => handleToggleHotel(hotel.id)}
                      className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white text-sm"
                    >
                      {hotel.active ? <ToggleRight className="w-5 h-5" /> : <ToggleLeft className="w-5 h-5" />}
                      {hotel.active ? "Active" : "Activate"}
                    </button>
                    <button
                      onClick={() => setDeleteConfirm({ type: "hotel", hotelId: hotel.id })}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-500/20 hover:bg-red-500/30 text-red-400 text-sm"
                    >
                      <Trash2 className="w-4 h-4" />
                      Delete
                    </button>
                  </div>
                </div>

                {showSurge === hotel.id && (
                  <form onSubmit={(e) => handleUpdateSurge(e, hotel.id)} className="mt-4 p-4 rounded-xl bg-white/5 space-y-3">
                    <h4 className="font-medium text-white">Update Surge Pricing</h4>
                    <div className="grid grid-cols-3 gap-3">
                      <div>
                        <label className="block text-xs text-white/60 mb-1">Factor</label>
                        <input
                          type="number"
                          step="0.1"
                          min={1}
                          value={surgeData.surgeFactor}
                          onChange={(e) => setSurgeData((s) => ({ ...s, surgeFactor: parseFloat(e.target.value) || 1 }))}
                          className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                        />
                      </div>
                      <div>
                        <label className="block text-xs text-white/60 mb-1">Start Date</label>
                        <input
                          type="date"
                          value={surgeData.startDate}
                          onChange={(e) => setSurgeData((s) => ({ ...s, startDate: e.target.value }))}
                          required
                          className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm [color-scheme:dark]"
                        />
                      </div>
                      <div>
                        <label className="block text-xs text-white/60 mb-1">End Date</label>
                        <input
                          type="date"
                          value={surgeData.endDate}
                          onChange={(e) => setSurgeData((s) => ({ ...s, endDate: e.target.value }))}
                          required
                          className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm [color-scheme:dark]"
                        />
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <button type="submit" disabled={submitting} className="px-3 py-1.5 rounded-lg bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-sm">
                        Update
                      </button>
                      <button type="button" onClick={() => setShowSurge(null)} className="px-3 py-1.5 rounded-lg bg-white/10 text-white text-sm">
                        Cancel
                      </button>
                    </div>
                  </form>
                )}

                <div className="mt-4 pt-4 border-t border-white/10">
                  <p className="text-sm text-white/50 mb-2">Hotel must have at least one room to be activated.</p>
                  {(roomsByHotel[hotel.id]?.length ?? 0) > 0 && (
                    <div className="mb-4 space-y-2">
                      <h4 className="font-medium text-white/80 text-sm">Rooms</h4>
                      {roomsByHotel[hotel.id].map((room) => (
                        <div
                          key={room.id}
                          className="flex justify-between items-center gap-3 px-3 py-2 rounded-lg bg-white/5 border border-white/10"
                        >
                          {(room.photos?.length ?? 0) > 0 ? (
                            /* eslint-disable-next-line @next/next/no-img-element */
                            <img
                              src={getImageSrc(room.photos![0])}
                              alt=""
                              className="w-12 h-12 object-cover rounded flex-shrink-0"
                            />
                          ) : (
                            <div className="w-12 h-12 rounded bg-white/10 flex items-center justify-center flex-shrink-0">
                              <Bed className="w-5 h-5 text-white/30" />
                            </div>
                          )}
                          <span className="text-white text-sm flex-1">
                            {room.types} — ₹{Number(room.basePrice).toLocaleString()}/night · {room.totalCount} available
                          </span>
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleEditRoom(hotel, room)}
                              className="flex items-center gap-1 px-2 py-1 rounded-lg bg-white/10 hover:bg-white/20 text-white text-sm"
                            >
                              <Pencil className="w-4 h-4" />
                              Edit
                            </button>
                            <button
                              onClick={() => setDeleteConfirm({ type: "room", hotelId: hotel.id, roomId: room.id })}
                              className="text-red-400 hover:text-red-300 text-sm flex items-center gap-1"
                            >
                              <Trash2 className="w-4 h-4" />
                              Delete
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                  {editingRoom?.hotelId === hotel.id ? (
                    <form onSubmit={(e) => handleUpdateRoom(e, hotel.id, editingRoom.room.id)} className="space-y-3 p-4 rounded-xl bg-white/5">
                      <h4 className="font-medium text-white">Edit Room — {editingRoom.room.types}</h4>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Room Type</label>
                          <input
                            type="text"
                            value={newRoom.types}
                            onChange={(e) => setNewRoom((r) => ({ ...r, types: e.target.value }))}
                            placeholder="e.g. Deluxe"
                            required
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Base Price (₹)</label>
                          <input
                            type="number"
                            min={0}
                            value={newRoom.basePrice || ""}
                            onChange={(e) => setNewRoom((r) => ({ ...r, basePrice: parseInt(e.target.value) || 0 }))}
                            required
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Total Count</label>
                          <input
                            type="number"
                            min={1}
                            value={newRoom.totalCount}
                            onChange={(e) => setNewRoom((r) => ({ ...r, totalCount: parseInt(e.target.value) || 1 }))}
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Capacity</label>
                          <input
                            type="number"
                            min={1}
                            value={newRoom.capacity}
                            onChange={(e) => setNewRoom((r) => ({ ...r, capacity: parseInt(e.target.value) || 1 }))}
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-white/60 mb-1">Amenities (comma-separated)</label>
                        <input
                          type="text"
                          value={newRoom.amenities}
                          onChange={(e) => setNewRoom((r) => ({ ...r, amenities: e.target.value }))}
                          placeholder="AC, TV, Mini Bar"
                          className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                        />
                      </div>
                      <div className="min-h-[100px]">
                        <label className="block text-sm text-white/70 mb-2">Photos (add image URLs — Google Images links work too)</label>
                        <ImageUploadInput
                          value={newRoom.photos}
                          onChange={(photos) => {
                            setNewRoom((r) => ({ ...r, photos }));
                            setError("");
                          }}
                          size="md"
                        />
                      </div>
                      <div className="flex gap-2">
                        <button type="submit" disabled={submitting} className="px-3 py-1.5 rounded-lg bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-sm">
                          {submitting ? "Saving..." : "Save"}
                        </button>
                        <button type="button" onClick={() => { setEditingRoom(null); setNewRoom({ types: "", basePrice: 0, totalCount: 1, capacity: 2, amenities: "", photos: [] }); }} className="px-3 py-1.5 rounded-lg bg-white/10 text-white text-sm">
                          Cancel
                        </button>
                      </div>
                    </form>
                  ) : showAddRoom === hotel.id ? (
                    <form onSubmit={(e) => handleCreateRoom(e, hotel.id)} className="space-y-3 p-4 rounded-xl bg-white/5">
                      <h4 className="font-medium text-white">Add Room</h4>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Room Type</label>
                          <input
                            type="text"
                            value={newRoom.types}
                            onChange={(e) => setNewRoom((r) => ({ ...r, types: e.target.value }))}
                            placeholder="e.g. Deluxe"
                            required
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Base Price (₹)</label>
                          <input
                            type="number"
                            min={0}
                            value={newRoom.basePrice || ""}
                            onChange={(e) => setNewRoom((r) => ({ ...r, basePrice: parseInt(e.target.value) || 0 }))}
                            required
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Total Count</label>
                          <input
                            type="number"
                            min={1}
                            value={newRoom.totalCount}
                            onChange={(e) => setNewRoom((r) => ({ ...r, totalCount: parseInt(e.target.value) || 1 }))}
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                        <div>
                          <label className="block text-xs text-white/60 mb-1">Capacity</label>
                          <input
                            type="number"
                            min={1}
                            value={newRoom.capacity}
                            onChange={(e) => setNewRoom((r) => ({ ...r, capacity: parseInt(e.target.value) || 1 }))}
                            className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                          />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-white/60 mb-1">Amenities (comma-separated)</label>
                        <input
                          type="text"
                          value={newRoom.amenities}
                          onChange={(e) => setNewRoom((r) => ({ ...r, amenities: e.target.value }))}
                          placeholder="AC, TV, Mini Bar"
                          className="w-full px-3 py-2 rounded-lg bg-white/5 border border-white/10 text-white text-sm"
                        />
                      </div>
                      <div className="min-h-[100px]">
                        <label className="block text-sm text-white/70 mb-2">Photos (add image URLs — Google Images links work too)</label>
                        <ImageUploadInput
                          value={newRoom.photos}
                          onChange={(photos) => {
                            setNewRoom((r) => ({ ...r, photos }));
                            setError("");
                          }}
                          size="md"
                        />
                      </div>
                      <div className="flex gap-2">
                        <button type="submit" disabled={submitting} className="px-3 py-1.5 rounded-lg bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] text-sm">
                          {submitting ? "Adding..." : "Add Room"}
                        </button>
                        <button type="button" onClick={() => setShowAddRoom(null)} className="px-3 py-1.5 rounded-lg bg-white/10 text-white text-sm">
                          Cancel
                        </button>
                      </div>
                    </form>
                  ) : (
                    <button
                      onClick={() => {
                        setEditingRoom(null);
                        setShowAddRoom(hotel.id);
                        loadRooms(hotel.id);
                      }}
                      className="flex items-center gap-2 px-3 py-2 rounded-lg bg-white/10 hover:bg-white/20 text-white/90 text-sm"
                    >
                      <Bed className="w-4 h-4" />
                      Add Room
                    </button>
                  )}
                </div>
              </motion.div>
            ))
          )}
        </div>
      </div>

      <AnimatePresence>
        {deleteConfirm && (
          <>
            <motion.div
              className="fixed inset-0 bg-black/60 z-50"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setDeleteConfirm(null)}
            />
            <motion.div
              className="fixed inset-0 z-50 flex items-center justify-center p-4"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
            >
              <motion.div
                className="glass rounded-2xl p-6 max-w-sm w-full"
                initial={{ scale: 0.9 }}
                animate={{ scale: 1 }}
                exit={{ scale: 0.9 }}
                onClick={(e) => e.stopPropagation()}
              >
                <h3 className="text-lg font-bold text-white mb-2">
                  {deleteConfirm.type === "hotel" ? "Delete Hotel?" : deleteConfirm.type === "room" ? "Delete Room?" : "Delete Promo Code?"}
                </h3>
                <p className="text-white/70 text-sm mb-4">
                  {deleteConfirm.type === "hotel"
                    ? "This will permanently delete the hotel and all its rooms. This cannot be undone."
                    : deleteConfirm.type === "room"
                      ? "This will permanently delete the room. This cannot be undone."
                      : "This will permanently delete the promo code. This cannot be undone."}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setDeleteConfirm(null)}
                    className="flex-1 py-2 rounded-xl bg-white/10 text-white text-sm font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={
                      deleteConfirm.type === "hotel"
                        ? handleDeleteHotel
                        : deleteConfirm.type === "room"
                          ? handleDeleteRoom
                          : () => deleteConfirm.promoId != null && handleDeletePromo(deleteConfirm.promoId)
                    }
                    disabled={submitting}
                    className="flex-1 py-2 rounded-xl bg-red-500/20 border border-red-500/50 text-red-400 text-sm font-medium disabled:opacity-50"
                  >
                    {submitting ? "Deleting..." : "Delete"}
                  </button>
                </div>
              </motion.div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </main>
    )}
    </ProtectedRoute>
  );
}

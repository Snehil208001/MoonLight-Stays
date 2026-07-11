"use client";

import { useState } from "react";
import Link from "next/link";
import { motion } from "framer-motion";
import { Navbar } from "@/components/Navbar";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { api, type TripPlanResponse } from "@/lib/api";
import { showApiError } from "@/lib/toast";
import { ArrowLeft, Sparkles, MapPin, Utensils, Lightbulb } from "lucide-react";
import { ROUTES } from "@/lib/constants";

const INTEREST_OPTIONS = [
  "Food & Dining",
  "History & Culture",
  "Nature & Outdoors",
  "Nightlife",
  "Shopping",
  "Art & Museums",
  "Adventure",
  "Relaxation",
];

const BUDGET_OPTIONS = ["BUDGET", "MODERATE", "LUXURY"];

export default function TripPlannerPage() {
  const [city, setCity] = useState("");
  const [checkInDate, setCheckInDate] = useState("");
  const [checkOutDate, setCheckOutDate] = useState("");
  const [numberOfGuests, setNumberOfGuests] = useState(2);
  const [interests, setInterests] = useState<string[]>([]);
  const [budgetLevel, setBudgetLevel] = useState("MODERATE");
  const [loading, setLoading] = useState(false);
  const [plan, setPlan] = useState<TripPlanResponse | null>(null);

  const toggleInterest = (value: string) => {
    setInterests((prev) =>
      prev.includes(value) ? prev.filter((i) => i !== value) : [...prev, value]
    );
  };

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!city.trim()) {
      showApiError(new Error("Please enter a destination city."));
      return;
    }
    setLoading(true);
    setPlan(null);
    try {
      const result = await api.generateTripPlan({
        city: city.trim(),
        checkInDate: checkInDate || undefined,
        checkOutDate: checkOutDate || undefined,
        numberOfGuests,
        interests: interests.length ? interests : undefined,
        budgetLevel,
      });
      setPlan(result);
    } catch (err) {
      showApiError(err);
    } finally {
      setLoading(false);
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

          <h1 className="text-2xl font-bold text-white mb-2 flex items-center gap-2">
            <Sparkles className="w-7 h-7 text-[#00FFFF]" />
            AI Trip Planner
          </h1>
          <p className="text-white/60 mb-6">
            Tell us where you&apos;re headed and we&apos;ll craft a day-by-day itinerary.
          </p>

          <form onSubmit={handleGenerate} className="glass rounded-2xl p-6 space-y-5">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="sm:col-span-2">
                <label className="block text-sm text-white/70 mb-1">Destination city</label>
                <input
                  type="text"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  placeholder="e.g. Jaipur"
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white placeholder-white/30 focus:outline-none focus:border-[#00FFFF]/50"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Check-in</label>
                <input
                  type="date"
                  value={checkInDate}
                  onChange={(e) => setCheckInDate(e.target.value)}
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white focus:outline-none focus:border-[#00FFFF]/50"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Check-out</label>
                <input
                  type="date"
                  value={checkOutDate}
                  onChange={(e) => setCheckOutDate(e.target.value)}
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white focus:outline-none focus:border-[#00FFFF]/50"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Travellers</label>
                <input
                  type="number"
                  min={1}
                  value={numberOfGuests}
                  onChange={(e) => setNumberOfGuests(Math.max(1, Number(e.target.value)))}
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white focus:outline-none focus:border-[#00FFFF]/50"
                />
              </div>
              <div>
                <label className="block text-sm text-white/70 mb-1">Budget</label>
                <select
                  value={budgetLevel}
                  onChange={(e) => setBudgetLevel(e.target.value)}
                  className="w-full px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white focus:outline-none focus:border-[#00FFFF]/50"
                >
                  {BUDGET_OPTIONS.map((b) => (
                    <option key={b} value={b} className="bg-[#0A0A1A]">
                      {b.charAt(0) + b.slice(1).toLowerCase()}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm text-white/70 mb-2">Interests</label>
              <div className="flex flex-wrap gap-2">
                {INTEREST_OPTIONS.map((opt) => {
                  const active = interests.includes(opt);
                  return (
                    <button
                      type="button"
                      key={opt}
                      onClick={() => toggleInterest(opt)}
                      className={`px-3 py-1.5 rounded-full text-sm border transition-all ${
                        active
                          ? "bg-[#00FFFF]/20 border-[#00FFFF]/50 text-[#00FFFF]"
                          : "bg-white/5 border-white/10 text-white/70 hover:bg-white/10"
                      }`}
                    >
                      {opt}
                    </button>
                  );
                })}
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-semibold hover:bg-[#00FFFF]/30 transition-all disabled:opacity-50"
            >
              <Sparkles className="w-5 h-5" />
              {loading ? "Planning your trip..." : "Generate Itinerary"}
            </button>
          </form>

          {loading && (
            <div className="mt-8 space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="glass rounded-2xl h-32 animate-pulse" />
              ))}
            </div>
          )}

          {plan && !loading && (
            <motion.div
              className="mt-8 space-y-6"
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
            >
              <div className="glass rounded-2xl p-6">
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                  <MapPin className="w-5 h-5 text-[#00FFFF]" />
                  {plan.destination}
                </h2>
                <p className="text-white/70 mt-2">{plan.summary}</p>
              </div>

              {plan.days?.map((day) => (
                <div key={day.day} className="glass rounded-2xl p-6">
                  <h3 className="text-lg font-bold text-[#00FFFF] mb-4">
                    Day {day.day}: {day.title}
                  </h3>
                  <div className="space-y-4">
                    {day.activities?.map((act, idx) => (
                      <div key={idx} className="flex gap-3">
                        <span className="shrink-0 px-2.5 py-1 h-fit rounded-lg bg-[#FF7F50]/20 border border-[#FF7F50]/40 text-[#FF7F50] text-xs font-medium">
                          {act.timeOfDay}
                        </span>
                        <div>
                          <p className="text-white font-medium">{act.title}</p>
                          <p className="text-white/60 text-sm">{act.description}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                  {day.mealSuggestion && (
                    <p className="mt-4 flex items-center gap-2 text-sm text-white/70">
                      <Utensils className="w-4 h-4 text-[#00FFFF]" />
                      {day.mealSuggestion}
                    </p>
                  )}
                </div>
              ))}

              {plan.tips && plan.tips.length > 0 && (
                <div className="glass rounded-2xl p-6">
                  <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
                    <Lightbulb className="w-5 h-5 text-[#00FFFF]" />
                    Travel Tips
                  </h3>
                  <ul className="space-y-2">
                    {plan.tips.map((tip, idx) => (
                      <li key={idx} className="text-white/70 text-sm flex gap-2">
                        <span className="text-[#00FFFF]">•</span>
                        {tip}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </motion.div>
          )}
        </div>
      </main>
    </ProtectedRoute>
  );
}

// Always use relative path so Next.js rewrites proxy to backend.
// This avoids CORS, mixed-content (HTTPS→HTTP), and cookie issues.
// next.config.js uses NEXT_PUBLIC_API_URL to set the rewrite destination.
const API_BASE = "/api/v1";

export interface HotelSearchRequest {
  city: string;
  checkInDate: string;
  endDate: string;
  roomsCount: number;
  minPrice?: number;
  maxPrice?: number;
  roomType?: string;
  amenity?: string;
  page?: number;
  size?: number;
}

export interface HotelContactInfo {
  address?: string;
  phoneNumber?: string;
  email?: string;
  location?: string;
}

export interface Hotel {
  id: number;
  name: string;
  city: string;
  photos?: string[];
  amenities?: string[];
  contactInfo?: HotelContactInfo;
  active?: boolean;
  basePrice?: number;
  dynamicPricingActive?: boolean;
}

export interface RoomDto {
  id: number;
  types: string;
  basePrice: number;
  photos?: string[];
  amenities?: string[];
  totalCount: number;
  capacity: number;
}

export interface HotelPriceDto {
  hotel: Hotel;
  price: number;
  roomPrices?: RoomPriceDto[];
}

export interface RoomPriceDto {
  roomId: number;
  pricePerNight: number;
  totalForStay: number;
}

export interface HotelInfoDto {
  hotelDto: Hotel;
  rooms: RoomDto[];
}

export interface TripPlanRequest {
  city: string;
  checkInDate?: string;
  checkOutDate?: string;
  numberOfGuests?: number;
  interests?: string[];
  budgetLevel?: string;
  hotelId?: number;
}

export interface TripActivity {
  timeOfDay: string;
  title: string;
  description: string;
}

export interface TripDayPlan {
  day: number;
  title: string;
  activities: TripActivity[];
  mealSuggestion?: string;
}

export interface TripPlanResponse {
  destination: string;
  summary: string;
  days: TripDayPlan[];
  tips?: string[];
}

export interface BookingRequest {
  hotelId: number;
  roomId: number;
  checkInDate: string;
  checkOutDate: string;
  roomsCount: number;
  promoCode?: string;
}

export interface ApiResponse<T> {
  data?: T;
  error?: { message: string; status: number };
  timeStamp?: string;
}

export type Role = "GUEST" | "HOTEL_MANAGER";
export type GuestGender = "MALE" | "FEMALE" | "OTHER";

export interface GuestDto {
  name: string;
  gender: GuestGender;
  age: number;
}

export interface UserDto {
  id: number;
  name: string;
  email: string;
  roles: Role[];
}

async function refreshAccessToken(): Promise<string | null> {
  try {
    const res = await fetch(`${API_BASE}/auth/refresh`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    });
    const json = await res.json().catch(() => ({}));
    if (!res.ok) return null;
    const token = json?.data?.accessToken ?? json?.accessToken ?? json?.access_token;
    if (token && typeof window !== "undefined") {
      localStorage.setItem("accessToken", token);
      return token;
    }
    return null;
  } catch {
    return null;
  }
}

async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {},
  isRetry = false
): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  if (token) {
    (headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers, credentials: "include" });
  const json = await res.json().catch(() => ({}));

  if (res.status === 401 && !isRetry) {
    const newToken = await refreshAccessToken();
    if (newToken) return fetchApi<T>(endpoint, options, true);
  }

  if (!res.ok) {
    const msg = json?.error?.message ?? json?.message ?? res.statusText;
    throw new Error(msg || `Request failed: ${res.status}`);
  }

  return json.data ?? json;
}

export const api = {
  async searchHotels(params: HotelSearchRequest): Promise<HotelPriceDto[]> {
    const data = await fetchApi<HotelPriceDto[] | { content?: HotelPriceDto[] }>(
      "/hotels/search",
      { method: "POST", body: JSON.stringify(params) }
    );
    return Array.isArray(data) ? data : (data?.content ?? []);
  },

  async getHotelInfo(hotelId: number): Promise<HotelInfoDto> {
    return fetchApi<HotelInfoDto>(`/hotels/${hotelId}/info`);
  },

  async getRoomPrices(
    hotelId: number,
    checkIn: string,
    checkOut: string,
    roomsCount = 1
  ): Promise<RoomPriceDto[]> {
    const params = new URLSearchParams({
      checkIn,
      checkOut,
      roomsCount: String(roomsCount),
    });
    return fetchApi<RoomPriceDto[]>(`/hotels/${hotelId}/room-prices?${params}`);
  },

  async initBooking(data: BookingRequest): Promise<{ id: number }> {
    let token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
    if (!token) {
      token = await refreshAccessToken();
      if (!token) throw new Error("Please sign in to book");
    }
    const body: Record<string, unknown> = {
      hotelId: data.hotelId,
      roomId: data.roomId,
      checkInDate: data.checkInDate,
      checkOutDate: data.checkOutDate,
      roomsCount: data.roomsCount,
    };
    if (data.promoCode?.trim()) body.promoCode = data.promoCode.trim();
    let res = await fetch(`${API_BASE}/bookings/init`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
      credentials: "include",
    });
    if (res.status === 401) {
      const newToken = await refreshAccessToken();
      if (newToken) {
        res = await fetch(`${API_BASE}/bookings/init`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${newToken}`,
          },
          body: JSON.stringify(body),
          credentials: "include",
        });
      }
    }
    const json = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(json?.error?.message ?? json?.message ?? "Booking failed");
    const dto = json.data ?? json;
    const id = dto?.id ?? dto?.bookingId;
    if (id == null) throw new Error("Invalid booking response");
    return { id: Number(id) };
  },

  async addGuests(bookingId: number, guests: GuestDto[]): Promise<void> {
    if (!guests.length) return;
    await fetchApi(`/bookings/${bookingId}/addGuests`, {
      method: "POST",
      body: JSON.stringify(guests),
    });
  },

  async getBookingById(bookingId: number): Promise<BookingDto> {
    return fetchApi<BookingDto>(`/bookings/${bookingId}`);
  },

  async initiatePayment(bookingId: number): Promise<{ sessionUrl: string }> {
    let token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
    if (!token) {
      token = await refreshAccessToken();
      if (!token) throw new Error("Please sign in to complete payment");
    }
    let res = await fetch(`${API_BASE}/bookings/${bookingId}/payments`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      credentials: "include",
    });
    if (res.status === 401) {
      const newToken = await refreshAccessToken();
      if (newToken) {
        res = await fetch(`${API_BASE}/bookings/${bookingId}/payments`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${newToken}`,
          },
          credentials: "include",
        });
      }
    }
    const json = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(json?.error?.message ?? json?.message ?? "Payment failed");
    const data = json.data ?? json;
    const sessionUrl = data?.sessionUrl;
    if (!sessionUrl) throw new Error("No payment URL received");
    return { sessionUrl };
  },

  async login(email: string, password: string): Promise<{ accessToken: string }> {
    let res: Response;
    try {
      res = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
        credentials: "include",
      });
    } catch (e) {
      throw new Error("Network error. Please check your connection and try again.");
    }
    const json = await res.json().catch(() => ({}));
    if (!res.ok) {
      const msg = json?.error?.message ?? json?.message ?? `Login failed (${res.status})`;
      throw new Error(msg);
    }
    const dto = json.data ?? json;
    const token = dto?.accessToken ?? dto?.access_token;
    if (token) localStorage.setItem("accessToken", token);
    return { accessToken: token ?? "" };
  },

  async signup(email: string, password: string, name: string): Promise<unknown> {
    const res = await fetch(`${API_BASE}/auth/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, name }),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json?.error?.message ?? json?.message ?? "Signup failed");
    return json.data ?? json;
  },

  async logout(): Promise<void> {
    try {
      await fetch(`${API_BASE}/auth/logout`, {
        method: "POST",
        credentials: "include",
      });
    } finally {
      localStorage.removeItem("accessToken");
    }
  },

  async adminSignup(email: string, password: string, name: string): Promise<unknown> {
    const res = await fetch(`${API_BASE}/auth/admin/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, name }),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json?.error?.message ?? json?.message ?? "Admin signup failed");
    return json.data ?? json;
  },

  async refreshToken(): Promise<string | null> {
    return refreshAccessToken();
  },

  async getProfile(): Promise<UserDto> {
    return fetchApi<UserDto>("/users/profile");
  },

  async createHotel(data: Partial<Hotel>): Promise<Hotel> {
    return fetchApi<Hotel>("/admin/hotels", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  async getMyHotels(): Promise<Hotel[]> {
    return fetchApi<Hotel[]>("/admin/hotels", { method: "GET" });
  },

  async getHotelById(hotelId: number): Promise<Hotel> {
    return fetchApi<Hotel>(`/admin/hotels/${hotelId}`);
  },

  async updateHotel(hotelId: number, data: Partial<Hotel>): Promise<Hotel> {
    return fetchApi<Hotel>(`/admin/hotels/${hotelId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  async deleteHotel(hotelId: number): Promise<void> {
    return fetchApi(`/admin/hotels/${hotelId}`, { method: "DELETE" });
  },

  async createRoom(hotelId: number, data: Partial<RoomDto>): Promise<RoomDto> {
    return fetchApi<RoomDto>(`/admin/hotels/${hotelId}/rooms`, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  async getHotelRooms(hotelId: number): Promise<RoomDto[]> {
    return fetchApi<RoomDto[]>(`/admin/hotels/${hotelId}/rooms`);
  },

  async getRoomById(hotelId: number, roomId: number): Promise<RoomDto> {
    return fetchApi<RoomDto>(`/admin/hotels/${hotelId}/rooms/${roomId}`);
  },

  async updateRoom(hotelId: number, roomId: number, data: Partial<RoomDto>): Promise<RoomDto> {
    return fetchApi<RoomDto>(`/admin/hotels/${hotelId}/rooms/${roomId}/update`, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  async deleteRoom(hotelId: number, roomId: number): Promise<void> {
    return fetchApi(`/admin/hotels/${hotelId}/rooms/${roomId}`, { method: "DELETE" });
  },

  async toggleHotelStatus(hotelId: number): Promise<void> {
    return fetchApi(`/admin/hotels/${hotelId}`, { method: "PATCH" });
  },

  async updateSurgeFactor(
    hotelId: number,
    surgeFactor: number,
    startDate: string,
    endDate: string
  ): Promise<void> {
    return fetchApi(`/admin/hotels/${hotelId}/surge`, {
      method: "PATCH",
      body: JSON.stringify({ surgeFactor, startDate, endDate }),
    });
  },

  async getPromoCodes(): Promise<{ id: number; code: string; discountPercentage: number; active: boolean }[]> {
    const res = await fetchApi<{ id: number; code: string; discountPercentage: number; active: boolean }[]>(
      "/admin/promocodes",
      { method: "GET" }
    );
    return Array.isArray(res) ? res : [];
  },

  async createPromoCode(data: { code: string; discountPercentage: number; active: boolean }): Promise<unknown> {
    return fetchApi("/admin/promocodes", { method: "POST", body: JSON.stringify(data) });
  },

  async deletePromoCode(id: number): Promise<void> {
    return fetchApi(`/admin/promocodes/${id}`, { method: "DELETE" });
  },

  async validatePromoCode(code: string): Promise<{ valid: boolean; discountPercentage?: number }> {
    const res = await fetchApi<{ valid: boolean; discountPercentage?: number }>(
      `/promocodes/validate?code=${encodeURIComponent(code.trim())}`
    );
    return res ?? { valid: false };
  },

  async getActivePromoCodes(): Promise<{ code: string; discountPercentage: number }[]> {
    const res = await fetchApi<{ code: string; discountPercentage: number }[]>("/promocodes");
    return Array.isArray(res) ? res : [];
  },

  async updateProfile(data: { name?: string }): Promise<UserDto> {
    return fetchApi<UserDto>("/users/profile", { method: "PATCH", body: JSON.stringify(data) });
  },

  async getMyBookings(
    page = 0,
    size = 10,
    status?: string
  ): Promise<{ content: BookingDto[]; totalPages: number }> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (status && status !== "ALL") params.set("status", status);
    const data = await fetchApi<{ content?: BookingDto[]; totalPages?: number }>(
      `/bookings/myBookings?${params}`
    );
    const content = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
    return { content, totalPages: data?.totalPages ?? 0 };
  },

  async cancelBooking(bookingId: number): Promise<void> {
    return fetchApi(`/bookings/${bookingId}/cancel`, { method: "POST" });
  },

  async addReview(hotelId: number, rating: number, content: string): Promise<ReviewDto> {
    return fetchApi<ReviewDto>(`/hotels/${hotelId}/reviews`, {
      method: "POST",
      body: JSON.stringify({ rating, content, hotelId }),
    });
  },

  async getHotelReviews(
    hotelId: number,
    page = 0,
    size = 10
  ): Promise<{ content: ReviewDto[]; totalPages: number }> {
    const data = await fetchApi<{ content?: ReviewDto[]; totalPages?: number }>(
      `/hotels/${hotelId}/reviews?page=${page}&size=${size}`
    );
    const content = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
    return { content, totalPages: data?.totalPages ?? 0 };
  },

  async getHotelAverageRating(hotelId: number): Promise<number> {
    const val = await fetchApi<number>(`/hotels/${hotelId}/reviews/average`);
    return typeof val === "number" ? val : 0;
  },

  async generateTripPlan(req: TripPlanRequest): Promise<TripPlanResponse> {
    return fetchApi<TripPlanResponse>("/ai/trip-plan", {
      method: "POST",
      body: JSON.stringify(req),
    });
  },

  async getFavoriteHotels(): Promise<Hotel[]> {
    return fetchApi<Hotel[]>("/users/favorites");
  },

  async addToFavorites(hotelId: number): Promise<void> {
    return fetchApi(`/users/favorites/${hotelId}`, { method: "POST" });
  },

  async removeFromFavorites(hotelId: number): Promise<void> {
    return fetchApi(`/users/favorites/${hotelId}`, { method: "DELETE" });
  },

  async uploadImage(file: File): Promise<{ url: string }> {
    const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
    if (!token) throw new Error("Please sign in to upload images");
    const formData = new FormData();
    formData.append("file", file);
    const headers: HeadersInit = {
      Authorization: `Bearer ${token}`,
    };
    let res = await fetch(`${API_BASE}/upload/image`, {
      method: "POST",
      headers,
      body: formData,
      credentials: "include",
    });
    if (res.status === 401) {
      const newToken = await refreshAccessToken();
      if (newToken) {
        res = await fetch(`${API_BASE}/upload/image`, {
          method: "POST",
          headers: { Authorization: `Bearer ${newToken}` },
          body: formData,
          credentials: "include",
        });
      }
    }
    const json = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(json?.error?.message ?? json?.message ?? "Upload failed");
    const data = json.data ?? json;
    const url = data?.url;
    if (!url) throw new Error("No URL returned from upload");
    return { url };
  },
};

export interface BookingDto {
  id: number;
  hotel: Hotel;
  room: RoomDto;
  checkInDate: string;
  checkOutDate: string;
  roomsCount: number;
  amount: number;
  bookingStatus: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewDto {
  id: number;
  rating: number;
  content: string;
  hotelId: number;
  userId: number;
}

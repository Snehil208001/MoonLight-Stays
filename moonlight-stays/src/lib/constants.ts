/** Centralized route paths for consistent navigation */
export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  PROFILE: "/profile",
  BOOKINGS: "/bookings",
  FAVORITES: "/favorites",
  TRIP_PLANNER: "/trip-planner",
  ADMIN: "/admin",
  PAYMENT_SUCCESS: "/payments/success",
  PAYMENT_FAILURE: "/payments/failure",
  hotel: (id: number) => `/hotels/${id}`,
} as const;

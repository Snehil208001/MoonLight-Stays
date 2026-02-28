"use client";

import { useCallback } from "react";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { refreshAuth, logoutUser } from "@/store/authSlice";

/** Redux-based auth hook - drop-in replacement for AuthContext */
export function useAuth() {
  const dispatch = useAppDispatch();
  const user = useAppSelector((s) => s.auth.user);
  const loading = useAppSelector((s) => s.auth.loading);

  const isLoggedIn = !!user;
  const isHotelManager = user?.roles?.includes("HOTEL_MANAGER") ?? false;

  const refreshAuthState = useCallback(async (): Promise<boolean> => {
    const result = await dispatch(refreshAuth());
    return result.meta.requestStatus === "fulfilled";
  }, [dispatch]);

  const logout = useCallback(async () => {
    await dispatch(logoutUser());
  }, [dispatch]);

  return {
    user,
    isLoggedIn,
    isHotelManager,
    loading,
    refreshAuth: refreshAuthState,
    logout,
  };
}

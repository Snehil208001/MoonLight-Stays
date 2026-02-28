"use client";

import { useEffect } from "react";
import { useAppDispatch } from "@/store/hooks";
import { refreshAuth } from "@/store/authSlice";

/** Runs on mount and window focus to refresh auth state */
export function AuthInitializer({ children }: { children: React.ReactNode }) {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(refreshAuth());
  }, [dispatch]);

  useEffect(() => {
    const onFocus = () => dispatch(refreshAuth());
    window.addEventListener("focus", onFocus);
    return () => window.removeEventListener("focus", onFocus);
  }, [dispatch]);

  return <>{children}</>;
}

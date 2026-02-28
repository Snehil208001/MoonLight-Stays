import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { api, type UserDto } from "@/lib/api";

interface AuthState {
  user: UserDto | null;
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  user: null,
  loading: true,
  error: null,
};

const timeout = (ms: number) =>
  new Promise<never>((_, reject) => setTimeout(() => reject(new Error("Timeout")), ms));

export const refreshAuth = createAsyncThunk(
  "auth/refresh",
  async (_, { rejectWithValue }) => {
    try {
      const profile = await Promise.race([api.getProfile(), timeout(8000)]);
      return profile;
    } catch {
      try {
        const refreshed = await Promise.race([api.refreshToken(), timeout(5000)]);
        if (refreshed) {
          const profile = await Promise.race([api.getProfile(), timeout(8000)]);
          return profile;
        }
      } catch {
        if (typeof window !== "undefined") localStorage.removeItem("accessToken");
      }
      return rejectWithValue(null);
    }
  }
);

export const logoutUser = createAsyncThunk("auth/logout", async () => {
  await api.logout();
});

export const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setUser: (state, action: { payload: UserDto | null }) => {
      state.user = action.payload;
      state.error = null;
    },
    clearAuth: (state) => {
      state.user = null;
      state.error = null;
      if (typeof window !== "undefined") localStorage.removeItem("accessToken");
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(refreshAuth.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(refreshAuth.fulfilled, (state, action) => {
        state.user = action.payload;
        state.loading = false;
        state.error = null;
      })
      .addCase(refreshAuth.rejected, (state) => {
        state.user = null;
        state.loading = false;
        state.error = null;
        if (typeof window !== "undefined") localStorage.removeItem("accessToken");
      })
      .addCase(logoutUser.fulfilled, (state) => {
        state.user = null;
        state.error = null;
      });
  },
});

export const { setUser, clearAuth } = authSlice.actions;
export const selectUser = (state: { auth: AuthState }) => state.auth.user;
export const selectAuthLoading = (state: { auth: AuthState }) => state.auth.loading;
export const selectIsLoggedIn = (state: { auth: AuthState }) => !!state.auth.user;
export const selectIsHotelManager = (state: { auth: AuthState }) =>
  state.auth.user?.roles?.includes("HOTEL_MANAGER") ?? false;
export default authSlice.reducer;

import {
  createSlice,
  createAsyncThunk,
  type PayloadAction,
} from "@reduxjs/toolkit";
import { SpotifyOAuthControllerApi } from "../../runnify-api-v1";
import type { LoggedInMemberDto } from "../../runnify-api-v1";
import { createPublicApiClient } from "../../utils/apiClient";

export interface AuthState {
  user: LoggedInMemberDto | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  spotifyAuthUrl: string | null;
  spotifyState: string | null;
}

const initialState: AuthState = {
  user: null,
  token: null,
  loading: false,
  error: null,
  spotifyAuthUrl: null,
  spotifyState: null,
};

const spotifyOAuthApi = new SpotifyOAuthControllerApi(createPublicApiClient());

// Step 1: Get Spotify authorization URL
export const initiateSpotifyLogin = createAsyncThunk(
  "auth/initiateSpotifyLogin",
  async () => {
    const response = await spotifyOAuthApi.generateSpotifyAuthorisationUrl();
    return response;
  }
);

// Step 2: Handle the callback from Spotify (complete login)
export const completeSpotifyLogin = createAsyncThunk(
  "auth/completeSpotifyLogin",
  async ({ code, state }: { code: string; state: string }) => {
    const response = await spotifyOAuthApi.handleSpotifyCallback({
      code,
      state,
    });
    return response;
  }
);

export const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.error = null;
      state.spotifyAuthUrl = null;
      state.spotifyState = null;
      // Clear token from localStorage if you're storing it there
      localStorage.removeItem("authToken");
    },
    clearError: (state) => {
      state.error = null;
    },
    // Action to restore auth state from localStorage on app init
    restoreAuthFromStorage: (
      state,
      action: PayloadAction<{ user: LoggedInMemberDto; token: string }>
    ) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
    },
  },
  extraReducers: (builder) => {
    builder
      // Initiate Spotify Login
      .addCase(initiateSpotifyLogin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(initiateSpotifyLogin.fulfilled, (state, action) => {
        state.loading = false;
        state.spotifyAuthUrl = action.payload.authUrl;
        state.spotifyState = action.payload.state;
      })
      .addCase(initiateSpotifyLogin.rejected, (state, action) => {
        state.loading = false;
        state.error =
          action.error.message || "Failed to initiate Spotify login";
      })

      // Complete Spotify Login
      .addCase(completeSpotifyLogin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(completeSpotifyLogin.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.spotifyAuthUrl = null;
        state.spotifyState = null;

        // Store token in localStorage for persistence
        localStorage.setItem("authToken", action.payload.token);
        localStorage.setItem("user", JSON.stringify(action.payload.user));
      })
      .addCase(completeSpotifyLogin.rejected, (state, action) => {
        state.loading = false;
        state.error =
          action.error.message || "Failed to complete Spotify login";
        state.spotifyAuthUrl = null;
        state.spotifyState = null;
      });
  },
});

export const { logout, clearError, restoreAuthFromStorage } = authSlice.actions;
export default authSlice.reducer;

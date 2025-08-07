import { configureStore } from "@reduxjs/toolkit";
import {
  playlistSlice,
  type PlaylistState,
} from "../../features/playlists/playlistSlice";
import { authSlice, type AuthState } from "../../features/auth/authSlice";

export interface RunnifyAppState {
  playlists: PlaylistState;
  auth: AuthState;
}

export const store = configureStore({
  reducer: {
    playlists: playlistSlice.reducer,
    auth: authSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// Re-export types for convenience
export type { NormalisedStoreState } from "./createNormalisedSlice";

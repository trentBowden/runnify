import type { RootState } from "../../app/store/store";

// Basic auth selectors
export const selectAuthUser = (state: RootState) => state.auth.user;
export const selectAuthToken = (state: RootState) => state.auth.token;
export const selectAuthLoading = (state: RootState) => state.auth.loading;
export const selectAuthError = (state: RootState) => state.auth.error;
export const selectSpotifyAuthUrl = (state: RootState) =>
  state.auth.spotifyAuthUrl;
export const selectSpotifyState = (state: RootState) => state.auth.spotifyState;

// Computed selectors
export const selectIsAuthenticated = (state: RootState) =>
  state.auth.user !== null && state.auth.token !== null;

export const selectUserName = (state: RootState) =>
  state.auth.user?.name || null;

export const selectUserAvatarUrl = (state: RootState) =>
  state.auth.user?.avatarUrl || null;

export const selectUserId = (state: RootState) => state.auth.user?.id || null;

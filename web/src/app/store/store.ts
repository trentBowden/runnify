import { configureStore } from "@reduxjs/toolkit";
import {
  playlistSlice,
  type PlaylistState,
} from "../../features/playlists/playlistSlice";

export interface RunnifyAppState {
  playlists: PlaylistState;
}

export const store = configureStore({
  reducer: {
    playlists: playlistSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

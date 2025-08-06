import { configureStore } from "@reduxjs/toolkit";
import {
  playlistSlice,
  type PlaylistState,
} from "../../features/playlists/playlistSlice";
import { gpxSlice, type GpxState } from "../../features/gpx/gpxSlice";

export interface RunnifyAppState {
  playlists: PlaylistState;
  gpx: GpxState;
}

export const store = configureStore({
  reducer: {
    playlists: playlistSlice.reducer,
    gpx: gpxSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

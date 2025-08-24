import { createAsyncThunk } from "@reduxjs/toolkit";
import type { Playlist } from "../../runnify-api-v1";
import { UserPlaylistsControllerApi } from "../../runnify-api-v1";
import {
  createnormalisedSlice,
  type BaseEntity,
  type NormalisedStoreState,
} from "../../app/store/createNormalisedSlice";
import { createConfiguredApiClient, getAuthToken } from "../../utils/apiClient";

type PlaylistEntity = Playlist & BaseEntity;
export type PlaylistState = NormalisedStoreState<PlaylistEntity>;

// Create API client with automatic JWT token inclusion
const getPlaylistApi = () =>
  new UserPlaylistsControllerApi(createConfiguredApiClient(getAuthToken));

// Api calls:
export const fetchPlaylistById = createAsyncThunk(
  "playlists/fetchById",
  async (id: string) => {
    const token = getAuthToken();
    if (!token) {
      throw new Error("No authentication token available");
    }
    const playlistApi = getPlaylistApi();
    const playlist = await playlistApi.getPlaylistById({
      id,
      authorization: `Bearer ${token}`,
    });
    if (!playlist) {
      throw new Error(`Playlist with id ${id} not found`);
    }
    return playlist as PlaylistEntity;
  }
);

export const fetchAllPlaylists = createAsyncThunk(
  "playlists/fetchAll",
  async () => {
    const token = getAuthToken();
    if (!token) {
      throw new Error("No authentication token available");
    }
    const playlistApi = getPlaylistApi();
    const playlists = await playlistApi.getPlaylists({
      authorization: `Bearer ${token}`,
    });
    return playlists as PlaylistEntity[];
  }
);

// Use the generic slice factory to save boilerplate.
export const playlistSlice = createnormalisedSlice<PlaylistEntity>(
  "playlists",
  {},
  (builder) => {
    builder
      /**
       * Fetch a single playlist
       */
      .addCase(fetchPlaylistById.pending, (state) => {
        state.loading = true;
        state.error = undefined;
      })
      .addCase(fetchPlaylistById.fulfilled, (state, action) => {
        state.loading = false;
        const playlist = action.payload;
        state.entities[playlist.id] = playlist;
        if (!state.ids.includes(playlist.id)) {
          state.ids.push(playlist.id);
        }
      })
      .addCase(fetchPlaylistById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || "Failed to fetch playlist";
      })
      /**
       * Fetch all playlists
       */
      .addCase(fetchAllPlaylists.pending, (state) => {
        state.loading = true;
        state.error = undefined;
      })
      .addCase(fetchAllPlaylists.fulfilled, (state, action) => {
        state.loading = false;
        state.entities = {};
        state.ids = [];

        action.payload.forEach((playlist) => {
          state.entities[playlist.id] = playlist;
          state.ids.push(playlist.id);
        });
      })
      .addCase(fetchAllPlaylists.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || "Failed to fetch playlists";
      });
  }
);

// Export the CRUD actions
export const {
  addEntity: addPlaylist,
  updateEntity: updatePlaylist,
  removeEntity: removePlaylist,
  clearAll: clearAllPlaylists,
} = playlistSlice.actions;

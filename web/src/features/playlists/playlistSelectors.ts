import { createnormalisedSelectors } from "../../app/store/normalisedSelectors";
import type { RootState } from "../../app/store/store";

// Create the selectors using the generic factory
export const playlistSelectors = createnormalisedSelectors<any>(
  (state: RootState) => state.playlists
);

// Export individual selectors (convenience thing)
export const {
  selectAll: selectAllPlaylists,
  selectById: selectPlaylistById,
  selectByIds: selectPlaylistsByIds,
  selectLoading: selectPlaylistsLoading,
  selectError: selectPlaylistsError,
  selectIds: selectPlaylistIds,
  selectCount: selectPlaylistsCount,
  selectHasEntities: selectHasPlaylists,
  selectEntities: selectPlaylistEntities,
} = playlistSelectors;

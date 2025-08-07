import React, { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../app/store/hooks';
import { fetchAllPlaylists, fetchPlaylistById } from './playlistSlice';
import {
  selectAllPlaylists,
  selectPlaylistsLoading,
  selectPlaylistsError,
} from './playlistSelectors';

export const PlaylistExample: React.FC = () => {
  const dispatch = useAppDispatch();
  
  // Select data from the store
  const playlists = useAppSelector(selectAllPlaylists);
  const loading = useAppSelector(selectPlaylistsLoading);
  const error = useAppSelector(selectPlaylistsError);

  // Fetch all playlists when component mounts
  useEffect(() => {
    dispatch(fetchAllPlaylists());
  }, [dispatch]);

  // Handler to fetch individual playlist
  const handleFetchPlaylist = (id: string) => {
    dispatch(fetchPlaylistById(id));
  };

  if (loading) {
    return <div>Loading playlists...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div>
      <h2>Playlists</h2>
      <button onClick={() => dispatch(fetchAllPlaylists())}>
        Refresh All Playlists
      </button>
      
      <ul>
        {playlists.map((playlist) => (
          <li key={playlist.id}>
            <span>{playlist.name}</span>
            <button onClick={() => handleFetchPlaylist(playlist.id)}>
              Refresh This Playlist
            </button>
          </li>
        ))}
      </ul>
      
      {playlists.length === 0 && !loading && (
        <p>No playlists found. Click "Refresh All Playlists" to load data.</p>
      )}
    </div>
  );
};
// The main container for the home page. 
// This will house three columns, 1/4, 2/4 and 1/4 width respectively.
// The first column will have two components: The Playlist Picker and the GpxFileList.
// The second column will have the Playlist Detail View
// The third column will have the GpxRenderer component.

import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../app/store/hooks';
import { selectAllPlaylists, selectPlaylistsError, selectPlaylistsLoading } from '../playlists/playlistSelectors';
import { fetchAllPlaylists } from '../playlists/playlistSlice';
import styles from './Home.module.css';

const HomePage = () => {
  // Use typed useAppSelector with selectors to get specific data from the store
  const dispatch = useAppDispatch();

  // Select data from the store
  const playlists = useAppSelector(selectAllPlaylists);
  const loading = useAppSelector(selectPlaylistsLoading);
  const error = useAppSelector(selectPlaylistsError);

  useEffect(() => {
    dispatch(fetchAllPlaylists());
  }, [dispatch]);

  if (loading) {
    return <div>Loading playlists...</div>;
  }
  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div className={styles.homeContainer}>
      {/* Column 1: The Playlist Picker and the GpxFileList */}
      <div className={styles.column}>
        Playlist picker ({playlists.length} playlists)
        <ul>
          {playlists.map((playlist) => (
            <li key={playlist.id}>{playlist.name}</li>
          ))}
        </ul>
      </div>

      {/* Column 2: The Playlist Detail View */}
      <div className={styles.column}>
        Playlist detail view
      </div>

      {/* Column 3: The GpxRenderer */}
      <div className={styles.column}>
        GPX renderer
      </div>
    </div>
  );
};

export default HomePage;

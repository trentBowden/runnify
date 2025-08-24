// The main container for the home page. 
// This will house three columns, 1/4, 2/4 and 1/4 width respectively.
// The first column will have two components: The Playlist Picker and the GpxFileList.
// The second column will have the Playlist Detail View
// The third column will have the GpxRenderer component.

import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../app/store/hooks';
import { selectAllPlaylists, selectPlaylistsError, selectPlaylistsLoading } from '../playlists/playlistSelectors';
import { fetchAllPlaylists } from '../playlists/playlistSlice';
import { 
  selectIsAuthenticated, 
  selectAuthUser 
} from '../auth/authSelectors';
import { logout, restoreAuthFromStorage } from '../auth/authSlice';
import Login from '../auth/Login';
import styles from './Home.module.css';

const HomePage = () => {
  const dispatch = useAppDispatch();

  // Auth state
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const user = useAppSelector(selectAuthUser);

  // Playlist state
  const playlists = useAppSelector(selectAllPlaylists);
  const playlistsLoading = useAppSelector(selectPlaylistsLoading);
  const playlistsError = useAppSelector(selectPlaylistsError);

  // Check for stored auth on component mount
  useEffect(() => {
    const storedToken = localStorage.getItem('authToken');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      try {
        const user = JSON.parse(storedUser);
        dispatch(restoreAuthFromStorage({ user, token: storedToken }));
      } catch {
        // Invalid stored data, clear it
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
      }
    }
  }, [dispatch]);

  // Fetch playlists only if authenticated
  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchAllPlaylists());
    }
  }, [dispatch, isAuthenticated]);



  const handleLogout = () => {
    dispatch(logout());
  };

  // If not authenticated, show login screen
  if (!isAuthenticated) {
    return (
      <div className={styles.homeContainer}>
        <Login />
      </div>
    );
  }

  // Show playlists loading state
  if (playlistsLoading) {
    return <div>Loading playlists...</div>;
  }

  // Show playlist error
  if (playlistsError) {
    return (
      <div>
        <div>Error loading playlists: {playlistsError}</div>
        <button onClick={handleLogout}>Logout</button>
      </div>
    );
  }

  // Main authenticated view
  return (
    <div className={styles.homeContainer}>
      {/* User info and logout */}
      <div style={{ 
        position: 'absolute', 
        top: '10px', 
        right: '10px',
        display: 'flex',
        alignItems: 'center',
        gap: '10px'
      }}>
        <span>Welcome, {user?.name || 'User'}!</span>
        {user?.avatarUrl && (
          <img 
            src={user.avatarUrl} 
            alt="User avatar" 
            style={{ width: '32px', height: '32px', borderRadius: '50%' }} 
          />
        )}
        <button onClick={handleLogout}>Logout</button>
      </div>

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

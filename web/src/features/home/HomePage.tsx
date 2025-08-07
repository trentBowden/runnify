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
  selectAuthUser, 
  selectAuthLoading, 
  selectAuthError 
} from '../auth/authSelectors';
import { initiateSpotifyLogin, logout, restoreAuthFromStorage } from '../auth/authSlice';
import styles from './Home.module.css';

const HomePage = () => {
  const dispatch = useAppDispatch();

  // Auth state
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const user = useAppSelector(selectAuthUser);
  const authLoading = useAppSelector(selectAuthLoading);
  const authError = useAppSelector(selectAuthError);

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

  const handleLogin = async () => {
    try {
      const result = await dispatch(initiateSpotifyLogin()).unwrap();
      // Redirect to Spotify OAuth URL
      window.location.href = result.authUrl;
    } catch (error) {
      console.error('Failed to initiate login:', error);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
  };

  // Show loading state
  if (authLoading) {
    return <div>Authenticating...</div>;
  }

  // Show auth error
  if (authError) {
    return (
      <div>
        <div>Authentication Error: {authError}</div>
        <button onClick={handleLogin}>Try Login Again</button>
      </div>
    );
  }

  // If not authenticated, show login screen
  if (!isAuthenticated) {
    return (
      <div className={styles.homeContainer}>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh',
          flexDirection: 'column'
        }}>
          <h1>Welcome to Runnify</h1>
          <p>Connect your Spotify account to get started</p>
          <button 
            onClick={handleLogin}
            style={{
              padding: '12px 24px',
              fontSize: '16px',
              backgroundColor: '#1db954',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer'
            }}
          >
            Login with Spotify
          </button>
        </div>
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

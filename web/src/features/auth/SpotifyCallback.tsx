import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../app/store/hooks';
import { completeSpotifyLogin } from './authSlice';
import { selectAuthLoading, selectAuthError } from './authSelectors';

/**
 * Component to handle the Spotify OAuth callback
 * This should be rendered at a route like /auth/spotify/callback
 */
const SpotifyCallback = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const loading = useAppSelector(selectAuthLoading);
  const error = useAppSelector(selectAuthError);
  const hasProcessedCallback = useRef(false);

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const errorParam = searchParams.get('error');

    if (errorParam) {
      console.error('Spotify OAuth error:', errorParam);
      navigate('/', { replace: true });
      return;
    }

    if (code && state) {
      // Prevent duplicate calls using ref (robust against StrictMode)
      if (hasProcessedCallback.current) {
        return;
      }
      hasProcessedCallback.current = true;
      
      dispatch(completeSpotifyLogin({ code, state }))
        .unwrap()
        .then(() => {
          // Successfully logged in, redirect to home
          navigate('/', { replace: true });
        })
        .catch((err) => {
          console.error('Login failed:', err);
          // Reset flag on error so user can retry if needed
          hasProcessedCallback.current = false;
          // Stay on this page to show error, or redirect to home
          navigate('/', { replace: true });
        });
    } else {
      // Missing required parameters
      console.error('Missing code or state parameter');
      navigate('/', { replace: true });
    }
  }, [dispatch, navigate, searchParams]);

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column'
      }}>
        <h2>Completing Spotify Login...</h2>
        <p>Please wait while we finish setting up your account.</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column'
      }}>
        <h2>Login Failed</h2>
        <p>Error: {error}</p>
        <button onClick={() => navigate('/')}>Return to Home</button>
      </div>
    );
  }

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh'
    }}>
      <h2>Processing...</h2>
    </div>
  );
};

export default SpotifyCallback;


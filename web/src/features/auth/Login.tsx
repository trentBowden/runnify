import { useAppDispatch, useAppSelector } from '../../app/store/hooks';
import { selectAuthError, selectAuthLoading } from './authSelectors';
import { initiateSpotifyLogin } from './authSlice';
import styles from './Login.module.css';
import logo from '../../assets/Runnify_467x467.png';
import spotifyLogo from '../../assets/spotify_icon.svg';

interface LoginProps {
  onRetry?: () => void;
}

const Login = ({ onRetry }: LoginProps) => {
  const dispatch = useAppDispatch();
  const authLoading = useAppSelector(selectAuthLoading);
  const authError = useAppSelector(selectAuthError);

  const handleLogin = async () => {
    try {
      const result = await dispatch(initiateSpotifyLogin()).unwrap();
      // Redirect to Spotify OAuth URL
      window.location.href = result.authUrl;
    } catch (error) {
      console.error('Failed to initiate login:', error);
    }
  };

  // Show loading state
  if (authLoading) {
    return <div className={styles.loadingContainer}>Authenticating...</div>;
  }

  // Show auth error
  if (authError) {
    return (
      <div className={styles.errorContainer}>
        <div className={styles.errorMessage}>Authentication Error: {authError}</div>
        <button className={styles.retryButton} onClick={onRetry || handleLogin}>Try Login Again</button>
      </div>
    );
  }

  // Default login screen
  return (
    <div className={styles.loginContainer}>
        <div className={styles.productTitleContainer}>

            <img src={logo} alt="Runnify" className={styles.logo} />
            <h1 className={styles.title}>Runnify</h1>
        </div>


      <button 
        className={styles.loginButton}
        onClick={handleLogin}
      >
        <img src={spotifyLogo} alt="Spotify" className={styles.spotifyLogo} />
        Login with Spotify
      </button>
    </div>
  );
};

export default Login;

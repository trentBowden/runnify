// The main container for the home page. 
// This will house three columns, 1/4, 2/4 and 1/4 width respectively.
// The first column will have two components: The Playlist Picker and the GpxFileList.
// The second column will have the Playlist Detail View
// The third column will have the GpxRenderer component.

import { useSelector } from 'react-redux';
import { selectAllPlaylists } from '../playlists/playlistSelectors';
import styles from './Home.module.css';

const HomePage = () => {
  // Use useSelector with selectors to get specific data from the store
  const allPlaylists = useSelector(selectAllPlaylists);

  return (
    <div className={styles.homeContainer}>
      {/* Column 1: The Playlist Picker and the GpxFileList */}
      <div className={styles.column}>
        Playlist picker ({allPlaylists.length} playlists)
        Gpx File list
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
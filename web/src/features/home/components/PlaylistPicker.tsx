import { GenericListItem } from "../../../components";
import type { Playlist } from "../../../runnify-api-v1";
import styles from "./PlaylistPicker.module.css";

const PlaylistPicker = (props: { playlists: Playlist[] }) => {
  return <div className={styles.container}>
    <h4>Your Playlists</h4>
    <hr/>

    <div className={styles.playlistsContainer}>

      {props.playlists.map((playlist) => (
          <GenericListItem 
          key={playlist.id}
          title={playlist.name}
          subtitle={`${playlist.tracks.length} tracks`}
          imageUrl={playlist.coverUrl}
          onClick={() => {
              console.log("Playlist clicked", playlist);
            }}
            >
        </GenericListItem>
      ))}

      </div>
  </div>;
};

export default PlaylistPicker;
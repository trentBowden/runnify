import { GenericListItem } from "../../../components";
import type { GpxParsed, Playlist } from "../../../runnify-api-v1";
import styles from "./PlaylistPicker.module.css";

const GpxFilePicker = (props: { gpxFiles: GpxParsed[] }) => {
  return <div className={styles.container}>
    <h4>Your GPX Files</h4>
    <hr/>

    <div className={styles.playlistsContainer}>

      {props.gpxFiles.map((gpxFile) => (
          <GenericListItem 
          key={gpxFile.name}
          title={gpxFile.name}
          subtitle={`${gpxFile.distance} km`}
          onClick={() => {
              console.log("GpxFile clicked", gpxFile);
            }}
            >
        </GenericListItem>
      ))}

      </div>
  </div>;
};

export default GpxFilePicker;
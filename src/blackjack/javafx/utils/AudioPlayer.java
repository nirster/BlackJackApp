package blackjack.javafx.utils;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class AudioPlayer {
    private MediaPlayer mediaPlayer = null;
    private final String url = new File("background.mp3").toURI().toString();
    
    public void enable() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer(new Media(url));
        }
        if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    mediaPlayer.play();
                }
            };
            new Thread(r).start();
        }
    }
    
    public void disable() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}

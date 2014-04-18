
package blackjack.javafx.utils;

import blackjack.engine.PlayerType;
import java.io.File;
import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;


public class Utils {
    
    public static final String RESOURCES_FOLDER = "/blackjack/resources/";
    public static final String IMAGE_FOLDER = RESOURCES_FOLDER + "images/";
    public static final String SOUND_FOLDER = RESOURCES_FOLDER + "sounds/";
    
    public static Image getImage (String imageName){
        InputStream imageInputStream = Utils.class.getResourceAsStream(IMAGE_FOLDER + imageName);
        return new Image(imageInputStream);
    }
    
    public static ImageView getImageView (String imageName){
        return new ImageView(getImage(imageName));
    }
    
    public static ImageView getCardImageView(String imageName) {
        return new ImageView(getImage(imageName.toLowerCase() + ".gif"));
    }
    
    public static ImageView getIconImageView(PlayerType playerType) {
        switch (playerType) {
            case HUMAN:
                return new ImageView(getImage("human.jpg"));
            case COMPUTER:
                return new ImageView(getImage("robot.png"));
            default:
                return new ImageView(getImage("human.jpg"));
        }
    }
    
    public static void playAww() {
            String url = new File("aww.mp3").toURI().toString();
            AudioClip ac = new AudioClip(url);
            ac.play();
    }
}

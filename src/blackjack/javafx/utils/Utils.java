
package blackjack.javafx.utils;

import blackjack.engine.PlayerType;
import java.io.File;
import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;


public class Utils {
    
    public final String RESOURCES_FOLDER = "/blackjack/resources/";
    public final String IMAGE_FOLDER = RESOURCES_FOLDER + "images/";
    public final String SOUND_FOLDER = RESOURCES_FOLDER + "sounds/";
    
    public Image getImage (String imageName){
        InputStream imageInputStream = Utils.class.getResourceAsStream(IMAGE_FOLDER + imageName);
        return new Image(imageInputStream);
    }
    
    public ImageView getImageView (String imageName){
        return new ImageView(getImage(imageName));
    }
    
    public ImageView getCardImageView(String imageName) {
        return new ImageView(getImage(imageName.toLowerCase() + ".gif"));
    }
    
    public ImageView getHiddenCardImageView() {
        return new ImageView(getImage("flippedcard.gif"));
    }
    
    public ImageView getIconImageView(PlayerType playerType) {
        switch (playerType) {
            case HUMAN:
                return new ImageView(getImage("human.jpg"));
            case COMPUTER:
                return new ImageView(getImage("robot.png"));
            default:
                return new ImageView(getImage("human.jpg"));
        }
    }
    
    public void playAww() {
            String url = new File("aww.mp3").toURI().toString();
            AudioClip ac = new AudioClip(url);
            ac.play();
    }
    
    public void playShuffle() {
        String url = new File("shuffle.wav").toURI().toString();
        AudioClip ac = new AudioClip(url);
        ac.play();
    }
    
    public void playDraw() {
        String url = new File("draw.wav").toURI().toString();
        AudioClip ac = new AudioClip(url);
        ac.play();
    }
    
    public void playBet() {
        String url = new File("bet.wav").toURI().toString();
        AudioClip ac = new AudioClip(url);
        ac.play();
    }
}

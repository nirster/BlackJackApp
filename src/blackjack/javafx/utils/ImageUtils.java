
package blackjack.javafx.utils;

import blackjack.engine.PlayerType;
import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class ImageUtils {
    
    public static final String RESOURCES_FOLDER = "/blackjack/resources/";
    public static final String IMAGE_FOLDER = RESOURCES_FOLDER + "images/";
    
    public static Image getImage (String imageName){
        InputStream imageInputStream = ImageUtils.class.getResourceAsStream(IMAGE_FOLDER + imageName);
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
    
}

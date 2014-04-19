
package blackjack.javafx.utils;

import blackjack.engine.Card;
import blackjack.engine.Hand;
import blackjack.engine.Player;
import blackjack.engine.PlayerType;
import java.io.File;
import java.io.InputStream;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.util.Duration;


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
    
    public HBox getHandHBox(Player p, Hand h, Player activePlayer, Hand activeHand) {
        // default config for all players
        HBox playerBox = new HBox(8);
        playerBox.setPadding(new Insets(5, 5, 5, 5));
        playerBox.setMaxHeight(110);
        playerBox.setPrefWidth(322);
        ImageView icon = getIconImageView(p.getType());
        icon.setFitHeight(42);
        icon.setPreserveRatio(true);
        Label nameLbl = new Label(p.getName());
        nameLbl.setAlignment(Pos.CENTER_LEFT);
        nameLbl.setPrefHeight(playerBox.getHeight());
        nameLbl.setStyle("-fx-font: regular 14px \"Arial\"; -fx-text-fill: white;");
        Label valueLabel = new Label("Hand: " + String.valueOf(h.cardsValue()));
        valueLabel.setStyle("-fx-font: regular 14px \"Arial\";");
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        valueLabel.setPrefHeight(playerBox.getHeight());
        playerBox.getChildren().addAll(icon, nameLbl, valueLabel);
        
        // set color according to hand value
        Color color = Color.RED;
        if (p.getCurrentHand().cardsValue() > 21) {
            color = Color.RED;
        }
        if (p.getCurrentHand().cardsValue() == 21) {
            color = Color.GREEN;
        }
        if (p.getCurrentHand().cardsValue() < 21) {
            color = Color.YELLOW;
        }
        valueLabel.setTextFill(color);
        
        // add cards images
        for (Card card : h.getCards()) {
            ImageView smallCard;
            smallCard = getCardImageView(card.toString().toLowerCase());
            smallCard.setFitHeight(42);
            smallCard.setPreserveRatio(true);
            playerBox.getChildren().add(smallCard);
        }
        
        
        // install tooltips
        Tooltip tt = new Tooltip("Money: " + p.getFunds() + "$" + "\nBet: " + h.getBetAmount() + "$");
        Tooltip.install(playerBox, tt);
        
        
        // add blinking if this is the active hand in the main view
        if (activePlayer.getName().equals(p.getName()) && h == activeHand) {
                nameLbl.setStyle("-fx-font: regular 14px \"Arial\"; -fx-text-fill: black;");
                final FadeTransition animation = FadeTransitionBuilder.create()
                        .node(playerBox)
                        .duration(Duration.millis(1400))
                        .fromValue(0.0)
                        .toValue(1.0)
                        .cycleCount(Animation.INDEFINITE)
                        .autoReverse(true)
                        .build();
                        animation.play();
            }

        return playerBox;
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

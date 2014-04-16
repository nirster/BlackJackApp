package blackjack.javafx.main;

import blackjack.engine.BlackJackTable;
import blackjack.engine.BlackJackTable.GameMode;
import blackjack.engine.Card;
import blackjack.engine.PlayerType;
import blackjack.javafx.utils.ImageUtils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author shai
 */
public class TableController implements Initializable {
    private BlackJackTable table;
    private float currentPlayerBet = 5f;
    
    @FXML
    private Button placeBetBtn;
    @FXML
    private Button skipRoundButton;
    @FXML
    private Label currentNameLabel;
    @FXML
    private Label currentMoneyLabel;
    @FXML
    private Label currentBetLabel;
    @FXML
    private Label currentBetHeader;
    @FXML
    private HBox cardsHBox;
    @FXML
    private HBox actionsBox;
    @FXML
    private VBox secondaryPlayersVBox;
    @FXML
    private VBox activePlayerInfoBox;
    @FXML
    private HBox handValueBox;
    @FXML
    private HBox chipsBox;
    @FXML
    private HBox placingBetsBox;
    @FXML
    private Label handValueLabel;
    @FXML
    private Label handValueLeft;
    
    public void setTable(BlackJackTable table) {
        this.table = table;
        updateView();
    }
    
    private void updateView() {
        if (table.getMode() == GameMode.PLACING_BETS) {
            placingBetsBox.setVisible(true);
            actionsBox.setVisible(false);
            handValueBox.setVisible(false);
        }
        
        if (table.getMode() == GameMode.ROUND) {
            placingBetsBox.setVisible(false);
            handValueBox.setVisible(true);
            actionsBox.setVisible(true);
            updateCurrentPlayerView();
            updateLocalValues();
            // updateRestOfPlayersView();
        }
        
        if (table.getMode() == GameMode.READING_PLAYERS) {
            // reading players mode
        }
    }
    
    private void updateLocalValues() {
        currentPlayerBet = 5f;
    }
    
    private void changeMode(GameMode gameMode) {
        updateView();
    }

    private void updateCurrentPlayerView() {
        updatePlayerInfoBoxView();
        updateCardsView();
    }
    
    private void updatePlayerInfoBoxView() {
        currentNameLabel.setText(table.getCurrentPlayer().getName());
        currentMoneyLabel.setText(String.valueOf(table.getCurrentPlayer().getFunds()));
        currentBetLabel.setText(String.valueOf(currentPlayerBet));
    }
    
    private void updateCardsView() {
        cardsHBox.getChildren().clear();
        for (Card c : table.getCurrentPlayer().getCurrentHand().getCards()) {
            ImageView cardView = ImageUtils.getCardImageView(c.toString());
            cardsHBox.getChildren().add(cardView);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //updateView();
    }
    
    @FXML
    private void onFiveClick(ActionEvent event) {
        this.currentPlayerBet = 5f;
        updateView();
    }
    
    @FXML
    private void onTenClick(ActionEvent event) {
        this.currentPlayerBet = 10f;
        updateView();
    }
    
    @FXML
    private void onTwentyFiveClick(ActionEvent event) {
        this.currentPlayerBet = 25f;
        updateView();
    }
    
    @FXML
    private void onHundredClick(ActionEvent event) {
        this.currentPlayerBet = 100f;
        updateView();
    }
    
    @FXML
    private void onPlaceBetClick(ActionEvent event) {
        table.getCurrentPlayer().placeInitialBet(currentPlayerBet);
        table.getCurrentPlayer().getCurrentHand().drawCardFrom(table.getDeck());
        table.getCurrentPlayer().getCurrentHand().drawCardFrom(table.getDeck());
        if (table.hasNextPlayer()) {
            table.moveToNextPlayer();
            updateView();
        }
        else {
            table.setMode(BlackJackTable.GameMode.ROUND);
            updateView();
        }
    }
    
    @FXML
    private void onSkipRoundButtonClick(ActionEvent event) {
        table.getCurrentPlayer().setOutOfRound(true);
    }

}

package blackjack.javafx.main;

import blackjack.engine.BlackJackTable;
import blackjack.engine.BlackJackTable.GameMode;
import blackjack.engine.Card;
import blackjack.engine.HandAction;
import blackjack.engine.Player;
import blackjack.engine.PlayerType;
import blackjack.javafx.utils.ImageUtils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
// change

/**
 *
 * @author shai
 */
public class TableController implements Initializable {
    private BlackJackTable table;
    private float currentPlayerBet = 5f;
    private boolean isHuman;
    private HandAction lastActionDone;
    
    @FXML
    private Button placeBetBtn;
    @FXML
    private Button hitButton;
    @FXML
    private Button standButton;
    @FXML
    private Button doubleButton;
    @FXML
    private Button splitButton;
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
    private Label handValueLabel;
    @FXML
    private HBox chipsBox;
    @FXML
    private HBox placingBetsBox;
    
    @FXML
    private TextField nameTextBox;
    @FXML
    private Button addPlayerButton;
    @FXML
    private RadioButton isHumanRadio;
    @FXML
    private Pane enteringPlayersPane;
    @FXML
    private Pane dealerPane;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Button startRoundButton;
    @FXML
    private HBox dealerCardsHBox;
    @FXML
    private Label dealerValueLabel;
    @FXML
    private MenuItem newGameMenuItem;
    
    public void setTable(BlackJackTable table) {
        this.table = table;
        updateView();
    }
    
    
    @FXML
    private void onAddPlayerClick(ActionEvent event) {
        hideNameError();
        PlayerType playerType = isHuman ? PlayerType.HUMAN : PlayerType.COMPUTER;
        if (table.getPlayer(nameTextBox.getText()) != null) {
            showNameError();
        }
        else {
        table.addPlayer(nameTextBox.getText(), playerType);
        nameErrorLabel.setText("Player " + nameTextBox.getText() + " added");
        nameErrorLabel.setVisible(true);
        }
    }
    
    @FXML
    private void createNewGame(ActionEvent event) {
        table = BlackJackTable.createDefaultTable();
        updateView();
    }
    
    @FXML
    private void onStartGameClick(ActionEvent event) {
        table.setMode(GameMode.PLACING_BETS);
        updateView();
    }
    
    @FXML
    private void onHumanClick(ActionEvent event) {
        isHuman = isHumanRadio.isSelected();
    }
    
    @FXML
    private void onHitClick(ActionEvent event) {
        table.getCurrentPlayer().doHit();
        lastActionDone = HandAction.HIT;
        updateView();
    }
    
    @FXML
    private void onStandClick(ActionEvent event) {
        table.getCurrentPlayer().doStand();
        lastActionDone = HandAction.STAND;
        // update Player data, check busted/blackjack
        if (table.getCurrentPlayer().getCurrentHand().isBusted()) {
            // remove funds
        }
        updateView();
        if (table.hasNextPlayer())
            table.moveToNextPlayer();
        
        updateView();
    }
    
    @FXML
    private void onDoubleClick(ActionEvent event) {
        table.getCurrentPlayer().doDouble();
        lastActionDone = HandAction.DOUBLE;
        updateView();
    }
    
    @FXML
    private void onSplitClick(ActionEvent event) {
        table.getCurrentPlayer().doSplit();
        lastActionDone = HandAction.SPLIT;
        updateView();
    }
    
    private void updateView() {
        
        if (table.getMode() == GameMode.PLACING_BETS) {
            // show
            chipsBox.setVisible(true);
            placingBetsBox.setVisible(true);
            activePlayerInfoBox.setVisible(true);
            updatePlayerInfoBoxView();
            updateLocalValues();
            
            // hide
            secondaryPlayersVBox.setVisible(false);
            enteringPlayersPane.setVisible(false);
            actionsBox.setVisible(false);
            handValueBox.setVisible(false);
        }
        
        if (table.getMode() == GameMode.ROUND) {
            placingBetsBox.setVisible(false);
            enteringPlayersPane.setVisible(false);
            chipsBox.setVisible(false);
            
            secondaryPlayersVBox.setVisible(true);
            cardsHBox.setVisible(true);
            dealerPane.setVisible(true);
            handValueBox.setVisible(true);
            actionsBox.setVisible(true);
            updateNonActivePlayersView();
            updateActionsBoxView();
            updatePlayerInfoBoxView();
            updateCardsView();
            updateLocalValues();
        }
        
        if (table.getMode() == GameMode.READING_PLAYERS) {
            secondaryPlayersVBox.setVisible(false);
            cardsHBox.setVisible(false);
            placingBetsBox.setVisible(false);
            handValueBox.setVisible(false);
            actionsBox.setVisible(false);
            dealerPane.setVisible(false);
            chipsBox.setVisible(false);
            activePlayerInfoBox.setVisible(false);
            enteringPlayersPane.setVisible(true);
        }
    }
    
    private void updateLocalValues() {
        currentPlayerBet = 5f;
    }
    
    private void changeMode(GameMode gameMode) {
        updateView();
    }
    
    private void updatePlayerInfoBoxView() {
        currentNameLabel.setText(table.getCurrentPlayer().getName());
        currentMoneyLabel.setText(String.valueOf(table.getCurrentPlayer().getFunds()));
        
        if (table.getMode() == GameMode.ROUND) {
            currentBetLabel.setText(String.valueOf(table.getCurrentPlayer().getCurrentHand().getBetAmount()));
        }
    }
    
    
    private void updateCardsView() {
        cardsHBox.getChildren().clear();
        for (Card c : table.getCurrentPlayer().getCurrentHand().getCards()) {
            ImageView cardView = ImageUtils.getCardImageView(c.toString());
            cardsHBox.getChildren().add(cardView);
        }
        handValueLabel.setText("Hand Value: " + table.getCurrentPlayer().getCurrentHand().cardsValue());
        
        dealerCardsHBox.getChildren().clear();
        for (Card c : table.getDealer().getCards()) {
            ImageView cardView = ImageUtils.getCardImageView(c.toString());
            dealerCardsHBox.getChildren().add(cardView);
        }
        dealerValueLabel.setText("Dealer: " + table.getDealer().cardsValue());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        if (table.hasNextPlayer()) {
            table.moveToNextPlayer();
            updateView();
        }
        else {
            table.setMode(GameMode.ROUND);
            updateView();
        }
    }
    
    @FXML
    private void onSkipRoundButtonClick(ActionEvent event) {
        table.getCurrentPlayer().setOutOfRound(true);
    }

    private void showNameError() {
        nameErrorLabel.setText("Name " + nameTextBox.getText() + " already exists");
        nameErrorLabel.setVisible(true);
    }

    private void hideNameError() {
        if (nameErrorLabel != null) {
            nameErrorLabel.setVisible(false);
        }
    }

    private void updateActionsBoxView() {
        if (!table.getCurrentPlayer().getCurrentHand().isSplittable()) {
            splitButton.setDisable(true);
        }
        
        if (lastActionDone == HandAction.DOUBLE) {
            hitButton.setDisable(true);
            splitButton.setDisable(true);
            doubleButton.setDisable(true);
        }
    }

    private void updateNonActivePlayersView() {
        
        secondaryPlayersVBox.getChildren().clear();
        for (Player p : table.getPlayers()) {
            // don't show active player
            if (!table.getCurrentPlayer().getName().equals(p.getName())) {
                HBox playerBox = new HBox();
                playerBox.setPadding(new Insets(5, 5, 5, 5));
                playerBox.setMaxHeight(secondaryPlayersVBox.getHeight() / 5.0);
                playerBox.setPrefWidth(secondaryPlayersVBox.getWidth());
                Label nameLbl = new Label(p.getName());
                nameLbl.setAlignment(Pos.CENTER_LEFT);
                nameLbl.setPrefHeight(playerBox.getHeight());
                Label valueLabel = new Label(String.valueOf(p.getCurrentHand().cardsValue()));
                valueLabel.setAlignment(Pos.CENTER_RIGHT);
                valueLabel.setPrefHeight(playerBox.getHeight());
                playerBox.getChildren().addAll(nameLbl, valueLabel);
                
                Color color = Color.RED;
                if (p.getCurrentHand().cardsValue() > 21)
                    color = Color.RED;
                if (p.getCurrentHand().cardsValue() == 21)
                    color = Color.GREEN;
                if (p.getCurrentHand().cardsValue() < 21)
                    color = Color.YELLOW;
                
                valueLabel.setTextFill(color);
                secondaryPlayersVBox.getChildren().add(playerBox);
                }
                
            }
        }
    }

package blackjack.javafx.main;

import blackjack.engine.BlackJackTable;
import blackjack.engine.BlackJackTable.GameMode;
import blackjack.engine.Card;
import blackjack.engine.Hand;
import blackjack.engine.HandAction;
import blackjack.engine.Player;
import blackjack.engine.PlayerType;
import blackjack.javafx.utils.ImageUtils;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
// change

/**
 *
 * @author shai
 */
public class TableController implements Initializable {
    private BlackJackTable table;
    private float currentPlayerBet = 5f;
    private boolean isHuman;
    private ObservableList<Label> msgLabelsList;
    
    @FXML
    private Button switchHandButton;
    @FXML
    private Button hitButton;
    @FXML
    private Button standButton;
    @FXML
    private Button doubleButton;
    @FXML
    private Button splitButton;
    @FXML
    private Label currentPlayerNameLabel;
    @FXML
    private Label currentPlayerMoneyLabel;
    @FXML
    private Label currentPlayerBetAmountLabel;
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
    private VBox messagesVBox;
    @FXML
    private TextField nameTextBox;
    @FXML
    private RadioButton isHumanRadio;
    @FXML
    private Pane enteringPlayersPane;
    @FXML
    private Pane dealerPane;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private HBox dealerCardsHBox;
    @FXML
    private Label dealerValueLabel;
    
    
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
        else if (table.getPlayers().size() >= 6) {
            showMaxPlayersError();
        }
        else if (nameTextBox.getText() == null) {
            showNameError();
        }
        else {
        table.addPlayer(nameTextBox.getText(), playerType);
        nameErrorLabel.setText("Player " + nameTextBox.getText() + " added");
        nameErrorLabel.setTextFill(Color.GREEN);
        nameErrorLabel.setVisible(true);
        FadeTransition animation = FadeTransitionBuilder.create()
                    .node(nameErrorLabel)
                    .duration(Duration.seconds(0.3))
                    .fromValue(0.0)
                    .toValue(1.0)
                    .build();
            animation.play();
        }
    }
    
    @FXML
    private void createNewGame(ActionEvent event) {
        table = BlackJackTable.createDefaultTable();
        msgLabelsList.clear();
        updateView();
    }
    
    @FXML
    private void onSwitchHandClick(ActionEvent event) {
        table.getCurrentPlayer().switchHands();
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
        addMessage(activePlayer().getName() + " is hitting");
        updateView();
        
        if (activeHand().isBlackJack()) {
            addMessage(activePlayer().getName() + " has BlackJack! :)");
            addMessage(activePlayer().getName() + " gets " + activeHand().getBetAmount() * 2.5 + "$");
            onDoneTurn();
        }
        
        if (activeHand().isBusted()) {
            addMessage(activePlayer().getName() + " is busted! :(");
            onDoneTurn();
        }
        
        updateView();
    }
    
    private void onDoneTurn() {
        if (activePlayer().hasMoreHands()) {
            activePlayer().switchHands();
            updateView();
        }
        
        else if (table.hasNextPlayer()) {
                table.moveToNextPlayer();
                updateView();
        }
        
        else {
            doDealerMove();
        }
    }
    
    private void doDealerMove() {
        while (dealer().cardsValue() < 18) {
            dealer().drawCardFrom(table.getDeck());
            addMessage("dealer is hitting");
            updateView();
        }
    }
    
    @FXML
    private void onStandClick(ActionEvent event) {
        activePlayer().doStand();
        addMessage(activePlayer().getName() + " is standing");

        if (activeHand().isBusted()) {
            addMessage(activePlayer().getName() + " is busted! :(");
        }

        onDoneTurn();
    }
    
    @FXML
    private void onDoubleClick(ActionEvent event) {
        activePlayer().doDouble();
        updateView();
        if (activeHand().isBusted()) {
            addMessage(activePlayer().getName() + " is busted! :(");
            onDoneTurn();
        }
    }
    
    @FXML
    private void onSplitClick(ActionEvent event) {
        activePlayer().doSplit();
        addMessage(activePlayer().getName() + " is splitting his hand");
        updateView();
    }
    
   
    
    private void updateView() {
        hideNameError();
        
        if (table.getMode() == GameMode.PLACING_BETS) {
            // show
            chipsBox.setVisible(true);
            placingBetsBox.setVisible(true);
            activePlayerInfoBox.setVisible(true);
            updatePlayerInfoBoxView();
            
            // hide
            secondaryPlayersVBox.setVisible(false);
            enteringPlayersPane.setVisible(false);
            actionsBox.setVisible(false);
            handValueBox.setVisible(false);
            switchHandButton.setVisible(false);
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
            updatePlayersListView();
            updateActionsBoxView();
            updatePlayerInfoBoxView();
            updateCardsView();
        }
        
        if (table.getMode() == GameMode.READING_PLAYERS) {
            secondaryPlayersVBox.setVisible(false);
            cardsHBox.setVisible(false);
            placingBetsBox.setVisible(false);
            handValueBox.setVisible(false);
            actionsBox.setVisible(false);
            dealerPane.setVisible(false);
            chipsBox.setVisible(false);
            switchHandButton.setVisible(false);
            activePlayerInfoBox.setVisible(false);
            
            enteringPlayersPane.setVisible(true);
        }
    }
    
    
    private void changeMode(GameMode gameMode) {
        updateView();
    }
    
    private void addMessage(String message) {
        String cssString = "-fx-font: regular 14px \"Arial\"; -fx-text-fill: white;";
        Label msgLabel = new Label(message);
        msgLabel.setStyle(cssString);
        msgLabelsList.add(msgLabel);
        
    }
    
    private void updatePlayerInfoBoxView() {
        currentPlayerNameLabel.setText(table.getCurrentPlayer().getName());
        currentPlayerMoneyLabel.setText(String.valueOf(table.getCurrentPlayer().getFunds()) + "$");
    }
    
    
    private void updateCardsView() {
        cardsHBox.getChildren().clear();
        for (Card c : activeHand().getCards()) {
            ImageView cardView = ImageUtils.getCardImageView(c.toString());
            cardsHBox.getChildren().add(cardView);
            FadeTransition animation = FadeTransitionBuilder.create()
                    .node(cardView)
                    .duration(Duration.seconds(0.7))
                    .fromValue(0.0)
                    .toValue(1.0)
                    .build();
            animation.play();
        }
        handValueLabel.setText("Hand Value: " + activeHand().cardsValue());
        currentPlayerBetAmountLabel.setText("Bet Amount: " + String.valueOf(activeHand().getBetAmount()) + "$");
        
        dealerCardsHBox.getChildren().clear();
        for (Card c : dealer().getCards()) {
            ImageView cardView = ImageUtils.getCardImageView(c.toString());
            dealerCardsHBox.getChildren().add(cardView);
        }
        dealerValueLabel.setText("Dealer: " + dealer().cardsValue());
        
        if (activePlayer().hasMoreHands()) {
            switchHandButton.setVisible(true);
        }
        else {
            switchHandButton.setVisible(false);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messagesVBox.setAlignment(Pos.TOP_RIGHT);
        
        msgLabelsList = FXCollections.observableArrayList();
        msgLabelsList.addListener(new ListChangeListener<Label>() {

            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (!msgLabelsList.isEmpty()) {
                    Label msgLabel = msgLabelsList.get(msgLabelsList.size() - 1);
                    messagesVBox.getChildren().add(msgLabel);
                    FadeTransition animation = FadeTransitionBuilder.create()
                    .node(msgLabel)
                    .duration(Duration.seconds(0.3))
                    .fromValue(0.0)
                    .toValue(1.0)
                    .build();
            animation.play();
                }
                updateView();
            }
        });
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
        activePlayer().placeInitialBet(currentPlayerBet);
        addMessage(activePlayer().getName() + " placed a bet of " + currentPlayerBet + "$");
        if (table.hasNextPlayer()) {
            table.moveToNextPlayer();
            updateView();
        }
        else {
            table.setMode(GameMode.ROUND);
            addMessage("Starting round...");
            updateView();
        }
    }
    
    @FXML
    private void onSkipRoundButtonClick(ActionEvent event) {
        activePlayer().setOutOfRound(true);
        addMessage(activePlayer().getName() + " is not playing this round");
    }

    private void showNameError() {
        nameErrorLabel.setText("Name " + nameTextBox.getText() + " already exists");
        nameErrorLabel.setTextFill(Color.BLACK);
        FadeTransition animation = FadeTransitionBuilder.create()
                    .node(nameErrorLabel)
                    .duration(Duration.seconds(0.3))
                    .fromValue(0.0)
                    .toValue(1.0)
                    .build();
            animation.play();
    }

    private void hideNameError() {
        if (nameErrorLabel != null) {
            nameErrorLabel.setVisible(false);
            FadeTransition animation = FadeTransitionBuilder.create()
                    .node(nameErrorLabel)
                    .duration(Duration.seconds(0.3))
                    .fromValue(1.0)
                    .toValue(0.0)
                    .build();
            animation.play();
        }
    }

    private void updateActionsBoxView() {
        for (Node node : actionsBox.getChildren()) {
            node.setDisable(true);
        }
        
        
        standButton.setDisable(false);
        List<HandAction> possibleActions = activeHand().getLegalActions(table.getCurrentPlayer().getFunds());
        for (HandAction handAction : possibleActions) {
            if (handAction == HandAction.DOUBLE)
                doubleButton.setDisable(false);
            if (handAction == HandAction.HIT)
                hitButton.setDisable(false);
            if (handAction == HandAction.SPLIT)
                splitButton.setDisable(false);
        }
        
        switchHandButton.setVisible(false);
        if (activePlayer().hasMoreHands())
            switchHandButton.setVisible(true);
    }

    private void updatePlayersListView() {

        secondaryPlayersVBox.getChildren().clear();
        for (Player p : table.getPlayers()) {
            HBox playerBox = new HBox(8);
            playerBox.setPadding(new Insets(5, 5, 5, 5));
            playerBox.setMaxHeight(secondaryPlayersVBox.getHeight() / 5.0);
            playerBox.setPrefWidth(secondaryPlayersVBox.getWidth());
            ImageView icon = ImageUtils.getIconImageView(p.getType());
            icon.setFitHeight(32);
            icon.setFitWidth(32);
            Label nameLbl = new Label(p.getName());
            nameLbl.setAlignment(Pos.CENTER_LEFT);
            nameLbl.setPrefHeight(playerBox.getHeight());
            nameLbl.setStyle("-fx-font: regular 14px \"Arial\"; -fx-text-fill: white;");
            Label valueLabel = new Label("Hand: " + String.valueOf(p.getCurrentHand().cardsValue()));
            valueLabel.setStyle("-fx-font: regular 14px \"Arial\";");
            valueLabel.setAlignment(Pos.CENTER_RIGHT);
            valueLabel.setPrefHeight(playerBox.getHeight());
            playerBox.getChildren().addAll(icon, nameLbl, valueLabel);

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
            
            if (activePlayer().getName().equals(p.getName())) {
                nameLbl.setStyle("-fx-font: bold 16px \"Arial\"; -fx-text-fill: green;");
                final FadeTransition animation = FadeTransitionBuilder.create()
                        .node(playerBox)
                        .duration(Duration.millis(1400))
                        .fromValue(0.0)
                        .toValue(1.0)
                        .cycleCount(Animation.INDEFINITE)
                        .autoReverse(true)
                        .build();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        animation.play();
                    }
                };
                
                new Thread(task).start();
            }

            valueLabel.setTextFill(color);
            secondaryPlayersVBox.getChildren().add(playerBox);

        }
    }

    private void showMaxPlayersError() {
        nameErrorLabel.setText("You already have six players");
        nameErrorLabel.setTextFill(Color.BLACK);
        nameErrorLabel.setVisible(true);
    }
    
    private Player activePlayer() {
        return table.getCurrentPlayer();
    }
    
    private Hand activeHand() {
        return activePlayer().getCurrentHand();
    }
    
    private Hand dealer() {
        return table.getDealer();
    }
    
}

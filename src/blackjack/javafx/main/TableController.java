package blackjack.javafx.main;

import blackjack.engine.BlackJackTable;
import blackjack.engine.BlackJackTable.GameMode;
import blackjack.engine.Card;
import blackjack.engine.Hand;
import blackjack.engine.HandAction;
import blackjack.engine.Player;
import blackjack.engine.PlayerType;
import blackjack.javafx.utils.AudioPlayer;
import blackjack.javafx.utils.Utils;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
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
    private boolean enableSound = true;
    private AudioPlayer audioPlayer;
    
    @FXML
    private Button fiveDollarButton;
    @FXML
    private Button tenDollarButton;
    @FXML
    private Button twentyFiveDollarButton;
    @FXML
    private Button hundredDollarButton;
    @FXML
    private Button placeBetBtn;
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
    @FXML
    private CheckMenuItem soundToggleItem;
    
    
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
    

    
    private void onDoneTurn() {
        // move to next hand if the player has more than one hand
        if (activePlayer().hasMoreHands()) {
            activePlayer().switchHands();
            updateView();
        }
        // move to next player
        else if (table.hasNextPlayer()) {
                table.moveToNextPlayer();
                updateView();
        }
        // everyone done playing
        else {
            doDealerMove();
            computeResults();
        }
    }
    
    private void computeResults() {
        for (Player p : table.getPlayers()) {
            for (Hand hand : p.getHands()) {
                if (!hand.isBusted()) {
                    if (hand.cardsValue() < dealer().cardsValue() && !dealer().isBusted()) {
                        addMessage(p.getName() + " lost the round");
                        addMessage(p.getName() + " now has " + p.getFunds() + "$");
                    }
                    if (hand.cardsValue() == dealer().cardsValue() && !dealer().isBusted()) {
                        p.setFunds(p.getFunds() + hand.getBetAmount());
                        addMessage(p.getName() + " gets his money back: " + hand.getBetAmount() + "$");
                        addMessage(p.getName() + " now has " + p.getFunds() + "$");
                    }
                    if (hand.cardsValue() > dealer().cardsValue() || dealer().isBusted()) {
                        final float normalWinFactor = 2.0f;
                        final float winAmount = hand.getBetAmount() * normalWinFactor;
                        p.setFunds(p.getFunds() + winAmount);
                        addMessage(p.getName() + " wins " + winAmount + "$");
                        addMessage(p.getName() + " now has " + p.getFunds() + "$");
                    }

                }
            }
        }
        addMessage("You can start a new round from");
        addMessage("\"File -> New Round\"");
        
        for (Node node : actionsBox.getChildren())
            node.setDisable(true);
        
        updateView();
    }
    
    private void doDealerMove() {
        while (dealer().cardsValue() < 18) {
            dealer().drawCardFrom(table.getDeck());
            addMessage("dealer is hitting");
            updateView();
        }
        if (dealer().isBusted()) {
            addMessage("Dealer is busted!");
        }
    }
    
        @FXML
    private void onHitClick(ActionEvent event) {
        table.getCurrentPlayer().doHit();
        addMessage(activePlayer().getName() + " is hitting");
        
        if (activeHand().isBlackJack()) {
            final float blackJackWinFactor = 2.5f;
            final float bjWinValue = activeHand().getBetAmount() * blackJackWinFactor;
            addMessage(activePlayer().getName() + " has BlackJack! :)");
            addMessage(activePlayer().getName() + " gets " + bjWinValue + "$");
            onDoneTurn();
        }
        
        if (activeHand().isBusted()) {
            Utils.playAww();
            addMessage(activePlayer().getName() + " is busted! :(");
            activePlayer().doStand();
            onDoneTurn();
        }
        
        updateView();
    }
    
    @FXML
    private void onStandClick(ActionEvent event) {
        activePlayer().doStand();
        addMessage(activePlayer().getName() + " is standing");

        if (activeHand().isBusted()) {
            Utils.playAww();
            addMessage(activePlayer().getName() + " is busted! :(");
        }

        onDoneTurn();
    }
    
    @FXML
    private void onDoubleClick(ActionEvent event) {
        activePlayer().doDouble();
        updateView();
        if (activeHand().isBusted()) {
            Utils.playAww();
            addMessage(activePlayer().getName() + " is busted! :(");
            activePlayer().doStand();
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
        if (table.getMode() == GameMode.PLACING_BETS) {
            // show
            chipsBox.setVisible(true);
            updateChipsBoxButtons();
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
    
    private void updateChipsBoxButtons() {
        float playerFunds = activePlayer().getFunds();
        
        fiveDollarButton.setDisable(playerFunds < 5);
        tenDollarButton.setDisable(playerFunds < 10);
        twentyFiveDollarButton.setDisable(playerFunds < 25);
        hundredDollarButton.setDisable(playerFunds < 100);
        placeBetBtn.setDisable(playerFunds < 5);
    }
    
    
    private void changeMode(GameMode gameMode) {
        updateView();
    }
    
    private void addMessage(String message) {
        String cssString = "-fx-font: regular 14px \"Arial\"; -fx-text-fill: white;";
        Label msgLabel = new Label(message);
        msgLabel.setStyle(cssString);
        if (messagesVBox.getChildren().size() >= 29)
            messagesVBox.getChildren().remove(0);
        
        msgLabelsList.add(msgLabel);
        
    }
    
    private void updatePlayerInfoBoxView() {
        currentPlayerNameLabel.setText(table.getCurrentPlayer().getName());
        currentPlayerMoneyLabel.setText(String.valueOf(table.getCurrentPlayer().getFunds()) + "$");
    }
    
    @FXML
    private void createNewRound(ActionEvent event) {
        table.setMode(GameMode.PLACING_BETS);
        msgLabelsList.clear();
        messagesVBox.getChildren().clear();
        dealerPane.setVisible(false);
        dealerCardsHBox.getChildren().clear();
        cardsHBox.getChildren().clear();
        updateView();
    }
    
    
    private void updateCardsView() {
        cardsHBox.getChildren().clear();
        for (Card c : activeHand().getCards()) {
            ImageView cardView = Utils.getCardImageView(c.toString());
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
            ImageView cardView = Utils.getCardImageView(c.toString());
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
        audioPlayer = new AudioPlayer();
        audioPlayer.enable();
        initTooltips();
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
            }
        });
    }
    
    private void initTooltips() {
        Tooltip hitTooltip = new Tooltip("Draw a card");
        Tooltip.install(hitButton, hitTooltip);
        
        Tooltip standTooltip = new Tooltip("Finish your turn");
        Tooltip.install(standButton, standTooltip);
        
        Tooltip doubleTooltip = new Tooltip("Double the bet amount");
        Tooltip.install(doubleButton, doubleTooltip);
        
        Tooltip splitTooltip = new Tooltip("Split your hand to two different bets");
        Tooltip.install(splitButton, splitTooltip);
        
    }
    
    @FXML
    private void toggleSound(ActionEvent event) {
        this.enableSound = soundToggleItem.isSelected();
        if (enableSound == true) {
            audioPlayer.enable();
        } else {
            audioPlayer.disable();
        }
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
        
        List<HandAction> possibleActions = activeHand().getLegalActions(table.getCurrentPlayer().getFunds());
        for (HandAction handAction : possibleActions) {
            if (handAction == HandAction.DOUBLE)
                doubleButton.setDisable(false);
            if (handAction == HandAction.STAND)
                standButton.setDisable(false);
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
            ImageView icon = Utils.getIconImageView(p.getType());
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
            
            // install tooltip to show player money and number of hands
            Tooltip tt = new Tooltip("Player money: " + p.getFunds() + "\nPlayer hands: " + p.getHands().size());
            Tooltip.install(playerBox, tt);
            
            // set colors: red - busted, yellow - ok, green - blackjack
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
            
            // infinite fade transition animation for current player (on different thread)
            if (activePlayer().getName().equals(p.getName())) {
                nameLbl.setStyle("-fx-font: regular 14px \"Arial\"; -fx-text-fill: black;");
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

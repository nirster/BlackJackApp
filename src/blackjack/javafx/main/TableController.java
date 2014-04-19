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
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

/**
 *
 * @author shai
 */
public class TableController implements Initializable {
    private BlackJackTable table;
    private Stage stage;
    private float currentPlayerBet = 5f;
    private boolean isHuman;
    private ObservableList<Label> msgLabelsList;
    private boolean enableSound = true;
    private AudioPlayer audioPlayer;
    private boolean isDealerHidden = true;
    private Utils utils;
    private File xmlSaveFile = null;
    
    
    @FXML
    private CheckMenuItem saveNextRound;
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
    

    
    @FXML
    private void loadXMLGame(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load from XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML File(*.xml)", "*.xml"));
        File xmlFile = fileChooser.showOpenDialog(stage);
        if (xmlFile != null) {
            try {
                this.table.loadXMLGame(xmlFile);
                addMessage("Game was loaded from " + xmlFile.toString());
            } catch (JAXBException | SAXException | NullPointerException ex) {
                addMessage("Error reading XML file, starting new game");
                addMessage(ex.getMessage());
                createNewGame(event);
            }
        }
        updateView();
    }
    
    @FXML
    private void saveXMLGame(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Saving to XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML File(*.xml)", "*.xml"));
        xmlSaveFile = fileChooser.showSaveDialog(stage);

        if (!xmlSaveFile.getName().contains(".")) {
            xmlSaveFile = new File(xmlSaveFile.getAbsolutePath() + ".xml");
        }
    }
    
    @FXML
    private void onAddPlayerClick(ActionEvent event) {
        PlayerType playerType = isHuman ? PlayerType.HUMAN : PlayerType.COMPUTER;
        if (table.getPlayer(nameTextBox.getText()) != null) {
            showNameError();
        }
        else if (table.getPlayers().size() >= 6) {
            showMaxPlayersError();
        }
        else if (nameTextBox.getText() == null || nameTextBox.getText().trim().isEmpty()) {
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
        nameTextBox.setText("");
        }
    }
    
    @FXML
    private void createNewGame(ActionEvent event) {
        table = BlackJackTable.createDefaultTable();
        msgLabelsList.clear();
        messagesVBox.getChildren().clear();
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
        doHit();
    }
    
    private void doHit() {
        utils.playDraw();
        activePlayer().doHit();
        addMessage(activePlayer().getName() + " is hitting");
        
        if (activeHand().isBusted()) {
            utils.playAww();
            addMessage(activePlayer().getName() + " is busted! :(");
            activePlayer().doStand();
            onDoneTurn();
        }
        
        else {
            updateView();
        }
        
    }
    
    @FXML
    private void onStandClick(ActionEvent event) {
        doStand();
    }
    
    private void doStand() {
        activePlayer().doStand();
        addMessage(activePlayer().getName() + " is standing");

        if (activeHand().isBusted()) {
            utils.playAww();
            addMessage(activePlayer().getName() + " is busted! :(");
        }

        onDoneTurn();
    }
    
    @FXML
    private void onDoubleClick(ActionEvent event) {
       doDouble();
    }
    
    private void doDouble() {
        utils.playDraw();
        activePlayer().doDouble();
        addMessage(activePlayer().getName() + " doubles his bet");
        onDoneTurn();
        updateView();
        if (activeHand().isBusted()) {
            utils.playAww();
            addMessage(activePlayer().getName() + " is busted! :(");
            activePlayer().doStand();
            onDoneTurn();
        }
    }
    
    @FXML
    private void onSplitClick(ActionEvent event) {
        doSplit();
    }
    
    private void doSplit() {
        utils.playDraw();
        activePlayer().doSplit();
        addMessage(activePlayer().getName() + " is splitting his hand");
        updateView();
    }
    
    @FXML
    private void createNewRound(ActionEvent event) {
        isDealerHidden = true;
        utils.playShuffle();
        table.setMode(GameMode.PLACING_BETS);
        msgLabelsList.clear();
        messagesVBox.getChildren().clear();
        dealerPane.setVisible(false);
        dealerCardsHBox.getChildren().clear();
        cardsHBox.getChildren().clear();
        updateView();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utils = new Utils();
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
                    final FadeTransition animation = FadeTransitionBuilder.create()
                            .node(msgLabel)
                            .duration(Duration.seconds(0.3))
                            .fromValue(0.0)
                            .toValue(1.0)
                            .build();
                    animation.play();
                }
            }
        });
        
        saveNextRound.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if (saveNextRound.isSelected()) {
                    if (xmlSaveFile != null) {
                        addMessage("Next round will be saved to");
                        addMessage(xmlSaveFile.toString());
                    } else {
                        addMessage("You must first set save location from");
                        addMessage("File->Set Save Location");
                    }
                }
            }
        });
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
        doPlaceBet();
    }
    
    private void doPlaceBet() {
        utils.playBet();
        activePlayer().placeInitialBet(currentPlayerBet);
        addMessage(activePlayer().getName() + " placed a bet of " + currentPlayerBet + "$");

        if (table.hasNextPlayer()) {
            table.moveToNextPlayer();
            updateView();
        } else {
            table.setMode(GameMode.ROUND);
            saveGameIfSelected();
            addMessage("Starting round...");
            updateView();
        }
    }
    
    @FXML
    private void onSkipRoundButtonClick(ActionEvent event) {
       doSkipRound();
    }
    
    private void doSkipRound() {
         activePlayer().setOutOfRound(true);
        addMessage(activePlayer().getName() + " is not playing this round");
        
        if (table.hasNextPlayer()) {
            table.moveToNextPlayer();
            updateView();
        }
        else {
            table.setMode(GameMode.ROUND);
            saveGameIfSelected();
            addMessage("Starting round...");
            updateView();
        }
    }
    
    @FXML
    private void quitApp() {
        Platform.exit();
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
    
    private void saveGameIfSelected() {
        if (saveNextRound.isSelected() && xmlSaveFile != null) {
            try {
                table.saveToXML(xmlSaveFile);
                addMessage("Game was saved to " + xmlSaveFile.toString());
                saveNextRound.selectedProperty().setValue(false);
            } catch (JAXBException | SAXException | NullPointerException ex) {
                addMessage("Failed saving game to XML file");
                addMessage(ex.getMessage());
            }
        }
    }
    
    private void updateCardsView() {
        cardsHBox.getChildren().clear();
        for (Card c : activeHand().getCards()) {
            ImageView cardView = utils.getCardImageView(c.toString());
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
            if (isDealerHidden == true && dealer().getCards().get(0) == c) {
                ImageView cardView = utils.getHiddenCardImageView();
                dealerValueLabel.setText("Dealer: Unknown");
                dealerCardsHBox.getChildren().add(cardView);
            }
            else if (isDealerHidden == true) {
                ImageView cardView = utils.getCardImageView(c.toString());
                dealerValueLabel.setText("Dealer: Unknown");
                dealerCardsHBox.getChildren().add(cardView);
            }
            else {
                ImageView cardView = utils.getCardImageView(c.toString());
                dealerCardsHBox.getChildren().add(cardView);
                dealerValueLabel.setText("Dealer: " + dealer().cardsValue());
            }
        }
        
    }
    
    public void setTable(BlackJackTable table) {
        this.table = table;
        updateView();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
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
        }
        
        if (table.getMode() == GameMode.ROUND) {
            placingBetsBox.setVisible(false);
            enteringPlayersPane.setVisible(false);
            chipsBox.setVisible(false);
            
            activePlayerInfoBox.setVisible(true);
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
            activePlayerInfoBox.setVisible(false);
            
            enteringPlayersPane.setVisible(true);
        }
        
        if (activePlayer() != null) {
            if (activePlayer().getType() == PlayerType.COMPUTER) {
                handleComputerPlayer();
            }
        }
    }
    
    private void checkForBlackJack() {
        if (activeHand().isBlackJack()) {
                final float blackJackWinFactor = 2.5f;
                final float bjWinValue = activeHand().getBetAmount() * blackJackWinFactor;
                addMessage(activePlayer().getName() + " has BlackJack! :)");
                addMessage(activePlayer().getName() + " gets " + bjWinValue + "$");
                onDoneTurn();
            }
    }
    
    private void handleComputerPlayer() {
        if (table.getMode() == GameMode.ROUND) {
            handleComputerAction();
        }

        if (table.getMode() == GameMode.PLACING_BETS) {
            handleComputerBet();
        }
    }
    
    private void handleComputerBet() {
        float computerFunds = activePlayer().getFunds();
            if (computerFunds >= 100) {
                currentPlayerBet = 100;
                doPlaceBet();
            } else if (computerFunds >= 25) {
                currentPlayerBet = 25;
                doPlaceBet();
            } else if (computerFunds >= 10) {
                currentPlayerBet = 10;
                doPlaceBet();
            } else if (computerFunds >= 5) {
                currentPlayerBet = 5;
                doPlaceBet();
            } else {
                onSkipRoundButtonClick(null);
            }
    }
    
  
    private void handleComputerAction() {
        final List<HandAction> possibleActions = activeHand().getLegalActions(currentPlayerBet);
        HandAction action;
        
        if (activeHand().cardsValue() == 11 && possibleActions.contains(HandAction.DOUBLE))
            action = HandAction.DOUBLE;
        else if (activeHand().cardsValue() < 15 && possibleActions.contains(HandAction.HIT))
            action = HandAction.HIT;
        else
            action = HandAction.STAND;
        
        doAction(action);
        updateActionsBoxView();
    }
    
    private void doAction(HandAction action) {
        switch (action) {
            case HIT:
                doHit();
                break;
            case STAND:
                doStand();
                break;
            case DOUBLE:
                doDouble();
                break;
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
        if (messagesVBox.getChildren().size() >= 35)
            messagesVBox.getChildren().remove(0);
        
        msgLabelsList.add(msgLabel);
        
    }
    
    private void updatePlayerInfoBoxView() {
        currentPlayerNameLabel.setText(table.getCurrentPlayer().getName());
        currentPlayerMoneyLabel.setText(String.valueOf(table.getCurrentPlayer().getFunds()) + "$");
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
            updateCardsView();
            updatePlayersListView();
        }
    }
    
    private void computeResults() {
        for (Player p : table.getPlayers()) {
            for (Hand hand : p.getHands()) {
                if (!hand.isBusted()) {
                    if (hand.isBlackJack()) {
                        final float blackjackFactor = 2.5f;
                        p.setFunds(p.getFunds() + hand.getBetAmount() * blackjackFactor);
                        addMessage(p.getName() + " has BlackJack!");
                        addMessage(p.getName() + " now has " + p.getFunds() + "$");
                    }
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
        
    }
    
    private void doDealerMove() {
        isDealerHidden = false;
        final int dealerMax = 17;
        while (dealer().cardsValue() < dealerMax) {
            dealer().drawCardFrom(table.getDeck());
            addMessage("dealer is hitting");
        }
        if (dealer().isBusted()) {
            addMessage("Dealer is busted!");
        }
        updateCardsView();
    }

    private void showNameError() {
        if (nameTextBox.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Name can't be empty");
        } else {
            nameErrorLabel.setText("Name " + nameTextBox.getText() + " already exists");
        }
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

    }

    private void updatePlayersListView() {

        secondaryPlayersVBox.getChildren().clear();
        for (Player p : table.getPlayers()) {
            for (Hand h : p.getHands()) {
                HBox playerBox = utils.getHandHBox(p, h, activePlayer(), activeHand());
                secondaryPlayersVBox.getChildren().add(playerBox);
            }
        }

    }

    private void showMaxPlayersError() {
        nameErrorLabel.setText("You already have six players");
        nameErrorLabel.setTextFill(Color.BLACK);
        nameErrorLabel.setTextFill(Color.BLACK);
        FadeTransition animation = FadeTransitionBuilder.create()
                    .node(nameErrorLabel)
                    .duration(Duration.seconds(0.3))
                    .fromValue(0.0)
                    .toValue(1.0)
                    .build();
            animation.play();
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

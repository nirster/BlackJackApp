package blackjack.engine;

//import blackjack.engine.xml.Bet;
//import blackjack.engine.xml.Blackjack;
//import blackjack.engine.xml.Cards;
//import blackjack.engine.xml.Players;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class BlackJackTable {

    private final int maxPlayers;
    private final float defaultFunds;
    private final String tableName;
    private final Deck deck;
    private List<Player> players;
    private List<Player> currentRoundPlayers;
    private List<Player> currentPlayersList;
    private Hand dealer;
    private GameMode gameMode;
    private Player currentPlayer;

    public BlackJackTable(int maxPlayers, float defaultFunds, String name) {
        this.maxPlayers = maxPlayers;
        this.defaultFunds = defaultFunds;
        this.tableName = name;
        this.deck = new Deck();
        this.dealer = null;
        this.players = new LinkedList<>();
        this.currentRoundPlayers = null;
        this.gameMode = GameMode.READING_PLAYERS;
        setMode(gameMode);
        this.currentPlayer = null;
    }

    public static BlackJackTable createDefaultTable() {
        BlackJackTable retTable = new BlackJackTable(6, 500, "game01");
        return retTable;
    }
    
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }
    
    public void moveToNextPlayer() {
        int idx = currentPlayersList.indexOf(currentPlayer);
        currentPlayer = currentPlayersList.get(++idx);
    }
    
    public boolean hasNextPlayer() {
        int idx = currentPlayersList.indexOf(currentPlayer);
        if (idx < currentPlayersList.size() - 1)
            return true;
        else
            return false;
    }
    
    public void createNewDealer() {
        this.dealer = Hand.newEmptyHand(0);
    }

    public final void setMode(GameMode gameMode) {
        this.gameMode = gameMode;
        initMode(gameMode);
    }
    
    public enum GameMode {
        READING_PLAYERS,
        PLACING_BETS,
        ROUND;
    }
    
    public Hand getDealer() {
        return this.dealer;
    }

    public GameMode getMode() {
        return this.gameMode;
    }

    public Deck getDeck() {
        return this.deck;
    }
    
    public void addPlayer(String playerName, PlayerType playerType) {
        this.players.add(new Player(playerName, playerType, this));
    }
    
    public void removePlayer(String playerName) {
        for (Player p : this.players) {
            if (p.getName().equals(playerName)) {
                int idx = this.players.indexOf(p);
                this.players.remove(idx);
            }
        }
    }
    
    public Player getPlayer(String name) {
        for (Player p : this.players) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    private void initMode(GameMode gameMode) {
        switch (gameMode) {
            case PLACING_BETS:
                initPlacingBetsMode();
                break;
            case ROUND:
                initRoundMode();
                break;
            case READING_PLAYERS:
                initReadingPlayersMode();
                break;
        }

    }
    
    private void updateCurrentRoundPlayers() {
        currentRoundPlayers = new LinkedList<>();
        
        for (Player p : players) {
            if (!p.isOutOfRound())
                currentRoundPlayers.add(p);
        }
    }
    
    private void initPlacingBetsMode() {
        if (currentRoundPlayers != null) {
            currentRoundPlayers.clear();
        }
        else {
            currentRoundPlayers = new ArrayList<>();
        }
        for (Player p : players) {
            p.dropHands();
        }
        this.dealer = Hand.newEmptyHand(0);
        this.dealer.drawCardFrom(deck);
        this.currentPlayersList = this.players;
        this.currentPlayer = currentPlayersList.get(0);
    }

    private void initRoundMode() {
        updateCurrentRoundPlayers();
        for (int i = 0; i < 2; ++i) {
            for (Player p : currentRoundPlayers) {
                p.getCurrentHand().drawCardFrom(deck);
                dealer.drawCardFrom(deck);
            }
        }
        currentPlayersList = currentRoundPlayers;
        currentPlayer = currentPlayersList.get(0);
    }
    
    private void initReadingPlayersMode() {
        currentPlayersList = null;
        players = new LinkedList<>();
        currentRoundPlayers = null;
        dealer = null;
        currentPlayer = null;
        deck.shuffle();
    }

    
}
    
    
//    public void loadXMLGame(File xmlFile) {
//        try {
//            clearPlayersBets();
//            clearActivePlayersList();
//            JAXBContext jc = JAXBContext.newInstance(Blackjack.class);
//            Unmarshaller u = jc.createUnmarshaller();
//            Blackjack bj = (Blackjack) u.unmarshal(xmlFile);
//            loadXMLDealer(bj.getDealer());
//            loadXMLPlayers(bj.getPlayers().getPlayer());
//        } catch (JAXBException ex) {
//            Logger.getLogger(BlackJackTable.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
//    private void loadXMLDealer(Bet dealer) {
//        createNewDealer();
//        for (Cards.Card card : dealer.getCards().getCard()) {
//            Card c = xmlCardToGameCard(card);
//            this.dealer.addCard(c);
//        }
//    }
//    
//    public static Card xmlCardToGameCard(Cards.Card xmlCard) {
//        String rank = xmlCard.getRank().toString();
//        String suit = xmlCard.getSuit().toString();
//        Card.Rank r = Card.Rank.valueOf(rank);
//        Card.Suit s = Card.Suit.valueOf(suit);
//        return new Card(s, r);
//    }
//
//    private void loadXMLPlayers(List<com.mta.blackjack.engine.xml.Player> player) {
//        for (com.mta.blackjack.engine.xml.Player xmlPlayer : player) {
//            String name = xmlPlayer.getName();
//            String type = xmlPlayer.getType().toString();
//            PlayerType t = PlayerType.valueOf(type);
//            addPlayer(name, t);
//            Player p = findPlayerByName(name);
//            p.loadXMLData(xmlPlayer);
//        }
//    }
//    public void saveToXML(File xmlFile) {
//        try {
//            JAXBContext jc = JAXBContext.newInstance(Blackjack.class);
//            Marshaller m = jc.createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            Blackjack bj = new Blackjack();
//            bj.setName(tableName);
//            Bet dealerBet = this.dealer.toXMLBet();
//            bj.setDealer(dealerBet);
//            bj.setPlayers(new Players());
//            bj.getPlayers().getPlayer().clear();
//            for (Player p : this.players) {
//                p.createXMLPlayerAndBets(bj.getPlayers().getPlayer());
//            }
//            m.marshal(bj, xmlFile);
//        } catch (JAXBException ex) {
//            Logger.getLogger(BlackJackTable.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }


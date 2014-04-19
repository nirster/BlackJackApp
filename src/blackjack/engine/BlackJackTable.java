package blackjack.engine;

import blackjack.xml.Bet;
import blackjack.xml.Blackjack;
import blackjack.xml.Cards;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

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
    
    public void setModeRoundXML() {
        this.gameMode = GameMode.ROUND;
        initXMLRound();
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
    
    public List<Player> getPlayers() {
        if (this.gameMode == GameMode.READING_PLAYERS)
            return this.players;
        else
            return this.currentPlayersList;
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
        this.currentPlayersList = this.players;
        this.currentPlayer = currentPlayersList.get(0);
    }

    private void initRoundMode() {
        updateCurrentRoundPlayers();
        for (Player p : currentRoundPlayers) {
            for (int i = 0; i < 2; ++i) 
                p.getCurrentHand().drawCardFrom(deck);
        }
        for (int i = 0; i < 2; ++i)
                dealer.drawCardFrom(deck);
        
        currentPlayersList = currentRoundPlayers;
        currentPlayer = currentPlayersList.get(0);
    }
    
    private void initXMLRound() {
        updateCurrentRoundPlayers();
        currentPlayersList = players;
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
    
    private void clearTable() {
        this.dealer = null;
        this.players = new LinkedList<>();
        this.currentRoundPlayers = null;
        this.gameMode = GameMode.READING_PLAYERS;
        setMode(gameMode);
        this.currentPlayer = null;
    }
    
    public static Card xmlCardToGameCard(Cards.Card xmlCard) {
        String rank = xmlCard.getRank().toString();
        String suit = xmlCard.getSuit().toString();
        Card.Rank r = Card.Rank.valueOf(rank);
        Card.Suit s = Card.Suit.valueOf(suit);
        return new Card(s, r);
    }
    
    private void loadXMLDealer(Bet dealer) {
        this.dealer = Hand.newEmptyHand(0);
        for (Cards.Card card : dealer.getCards().getCard()) {
            Card c = xmlCardToGameCard(card);
            this.dealer.addCard(c);
        }
    }
    
    private void loadXMLPlayers(List<blackjack.xml.Player> player) {
        for (blackjack.xml.Player xmlPlayer : player) {
            String name = xmlPlayer.getName();
            String type = xmlPlayer.getType().toString();
            PlayerType t = PlayerType.valueOf(type);
            addPlayer(name, t);
            Player p = getPlayer(name);
            p.loadXMLData(xmlPlayer);
        }
    }
    
    public void loadXMLGame(File xmlFile) throws JAXBException, SAXException {
        clearTable();
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(getClass().getResource("/blackjack/resources/xml/blackjack.xsd"));
        JAXBContext jc = JAXBContext.newInstance(Blackjack.class);
        Unmarshaller u = jc.createUnmarshaller();
        u.setSchema(schema);
        Blackjack bj = (Blackjack) u.unmarshal(xmlFile);
        loadXMLDealer(bj.getDealer());
        loadXMLPlayers(bj.getPlayers().getPlayer());
        setModeRoundXML();
    }
}
    
    



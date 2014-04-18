package blackjack.engine;

//import blackjack.engine.xml.Bet;
//import blackjack.engine.xml.Bets;
//import blackjack.engine.xml.Cards;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class Player {
    private static final int NORMAL_WIN_FACTOR = 2;
    private final PlayerType playerType;
    private final String name;
    private final List<Hand> hands;
    private final BlackJackTable table;
    private Hand currentHand;
    private float funds;
    private boolean isOutOfRound;
    
    
    public Player(String name, PlayerType type, BlackJackTable table) {
        this.funds = 500f;
        this.name = name;
        this.table = table;
        this.hands = new ArrayList<>();
        this.playerType = type;
        this.currentHand = null;
        this.isOutOfRound = true;
    }
    
    public float getFunds() {
        return funds;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
    public void setFunds(float newValue) {
        this.funds = newValue;
    }
    
    public List<Hand> getHands() {
        return this.hands;
    }
    
    public void dropHands() {
        if (!this.hands.isEmpty()) {
            hands.clear();
        }
        currentHand = null;
    }

    public void placeInitialBet(float betValue) {
            this.funds -= betValue;
            Hand newHand = Hand.newEmptyHand(betValue);
            this.hands.add(newHand);
            this.currentHand = newHand;
            this.isOutOfRound = false;
        }
    
    public boolean isOutOfRound() {
        return this.isOutOfRound;
    }
    
    public void setOutOfRound(boolean value) {
        this.isOutOfRound = value;
    }
    
    public void doStand() {
        currentHand.setStanding(true);
    }
    
    public void doHit() {
        currentHand.drawCardFrom(table.getDeck());
    }
    
    public void doDouble() {
        currentHand.drawCardFrom(table.getDeck());
        this.funds -= currentHand.getBetAmount();
        currentHand.setBetAmount(currentHand.getBetAmount() * 2);
    }
    
    public void doSplit() {
        this.hands.add(Hand.splitFrom(currentHand));
        this.funds -= currentHand.getBetAmount();
    }
    
    public boolean isActive() {
        if (this.hands.size() <= 0)
            return false;
        
        for (Hand hand : this.hands) {
            if (hand.isPlayable())
                return true;
        }
        return false;
    }
    
    public boolean hasMoreHands() {
        if (this.hands.size() <= 1)
            return false;
        else
            for (Hand h : this.hands) {
                if (h.getUID() != currentHand.getUID())
                    if (h.isPlayable() && !h.isStanding())
                        return true;
            }
        return false;
    }
    
    public void switchHands() {
        for (Hand h : this.hands) {
            if (h.getUID() != currentHand.getUID())
                if (h.isPlayable() && !h.isStanding())
                    currentHand = h;
        }
    }
    
    public Hand getCurrentHand() {
        return this.currentHand;
    }
    
    public String getName() {
        return this.name;
    }
    
    public PlayerType getType() {
        return this.playerType;
    }

//    void loadXMLData(com.mta.blackjack.engine.xml.Player xmlPlayer) {
//        this.funds = xmlPlayer.getMoney();
//        for (Bet bet : xmlPlayer.getBets().getBet()) {
//            Hand hand = Hand.newEmptyHand(bet.getSum());
//            this.bets.add(hand);
//            this.isActive = true;
//            for (Cards.Card card : bet.getCards().getCard()) {
//                Card c = TableManager.xmlCardToGameCard(card);
//                hand.addCard(c);
//            }
//        }
//    }

//    void createXMLPlayerAndBets(List<com.mta.blackjack.engine.xml.Player> players) {
//        com.mta.blackjack.engine.xml.Player xmlPlayer = new com.mta.blackjack.engine.xml.Player();
//        xmlPlayer.setMoney(funds);
//        xmlPlayer.setName(name);
//        xmlPlayer.setType(com.mta.blackjack.engine.xml.PlayerType.fromValue(this.playerType.toString()));
//        xmlPlayer.setBets(new Bets());
//        players.add(xmlPlayer);
//        for (Hand hand : this.bets) {
//            Bet bet = hand.toXMLBet();
//            xmlPlayer.getBets().getBet().add(bet);
//        }
//    }
    
}

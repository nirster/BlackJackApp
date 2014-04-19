package blackjack.engine;

import blackjack.xml.Bet;
import blackjack.xml.Cards;
import blackjack.xml.Rank;
import blackjack.xml.Suit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class Hand {
    private final String uid;
    private final List<Card> cards;
    private float betAmount;
    private boolean beenSplitted;
    private boolean isStanding;
    
    
    private Hand(float betAmount, boolean beenSplitted) {
        this.cards = new ArrayList<>();
        this.isStanding = false;
        this.beenSplitted = beenSplitted;
        this.betAmount = betAmount;
        this.uid = UUID.randomUUID().toString();
        
    }
    
    public String getUID() {
        return this.uid;
    }
    
    public boolean isStanding() {
        return this.isStanding;
    }
    
    public void setStanding(boolean value) {
        this.isStanding = value;
    }
    
    public boolean equalsUID(Hand otherHand) {
        return (this.uid == null ? otherHand.getUID() == null : this.uid.equals(otherHand.getUID()));
    }
    
    public static Hand newEmptyHand(float betAmount) {
        Hand retHand = new Hand(betAmount, false);
        return retHand;
    }
    
    public static Hand splitFrom(Hand otherHand) {
        Hand retHand = new Hand(otherHand.betAmount, true);
        otherHand.beenSplitted = true;
        retHand.cards.add(otherHand.cards.get(1));
        otherHand.cards.remove(1);
        return retHand;
    }
    
    public List<Card> getCards() {
        return this.cards;
    }
    
    public float getBetAmount() {
        return this.betAmount;
    }
    
    public void setBetAmount(float newAmount) {
        this.betAmount = newAmount;
    }
    
    public int cardsValue() {
        int sum = 0;
        int acesCount = 0;

        for (Card c : cards) {
            if (c.getRank() == Card.Rank.ACE) {
                acesCount++;
            }
            sum += c.getRank().numericValue();
        }
        // Count aces as one if busted.
        while (sum > 21 && acesCount > 0) {
            acesCount--;
            sum -= 10;
        }
        return sum;
    }
    
    public boolean isBusted() {
        return cardsValue() > 21;
    }
    
    public boolean isBlackJack() {
        return  cardsValue() == 21 && 
                beenSplitted == false &&
                cards.size() == 2;
    }
    
    public boolean isSplittable() {
        if (cards.size() != 2)
            return false;
        return cards.get(0).getRank().numericValue() == cards.get(1).getRank().numericValue();
    }
    
    public List<HandAction> getLegalActions(float playerFunds) {
        List<HandAction> retList = new ArrayList<>();
        if (!isBusted() && !isStanding()) {
            retList.add(HandAction.HIT);
            retList.add(HandAction.STAND);
            
            if (playerFunds >= this.betAmount)
                retList.add(HandAction.DOUBLE);

            if (isSplittable() && playerFunds >= this.betAmount) {
                retList.add(HandAction.SPLIT);
            }
        }
        return retList;
    }
    
    public void drawCardFrom(Deck deck) {
        Card card = deck.drawCard();
        cards.add(card);
    }
    
    public boolean isPlayable() {
        return !isBusted();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb
            .append(c.toString())
            .append("\n");
        }
        sb.append("Value: ").append(cardsValue());
        return sb.toString();
    }
    
    public void addCard(Card card) {
        cards.add(card);
    }

    Bet toXMLBet() {
        Bet retBet = new Bet();
        retBet.setSum(betAmount);
        retBet.setCards(new Cards());
        for (Card card : this.cards) {
            Cards.Card xmlCard = new Cards.Card();
            xmlCard.setRank(Rank.fromValue(card.getRank().toString()));
            xmlCard.setSuit(Suit.fromValue(card.getSuit().toString()));
            retBet.getCards().getCard().add(xmlCard);
        }
        return retBet;
    }
}

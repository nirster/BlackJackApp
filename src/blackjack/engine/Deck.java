package blackjack.engine;

import blackjack.engine.Card.Rank;
import blackjack.engine.Card.Suit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public final class Deck {
    private final List<Card> cardsList;
    private static final int CARDS_IN_DECK = 52;
    private static final int TOTAL_RANKS = 13;
    private static final int TOTAL_SUITS = 4;
    private final Random randomizer;
    
    public Deck() {
        this.cardsList = new ArrayList<>(CARDS_IN_DECK);
        randomizer = new Random();
        initDeck();
        shuffle();
    }
    
    public void shuffle() {
        randomizer.setSeed(System.currentTimeMillis());
    }

    private void initDeck() {
        Rank[] ranks = Rank.values();
        Suit[] suits = Suit.values();
        
        for (int i = 0; i < TOTAL_SUITS; ++i) {
            for (int j = 0; j < TOTAL_RANKS; ++j) {
                this.cardsList.add(new Card(suits[i], ranks[j]));
            } 
        }
    }
    
    public Card drawCard() {
        int randomIndex = randomizer.nextInt(CARDS_IN_DECK);
        return new Card(cardsList.get(randomIndex));
    }
}

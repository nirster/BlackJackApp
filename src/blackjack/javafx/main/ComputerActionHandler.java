package blackjack.javafx.main;

import blackjack.engine.Hand;
import blackjack.engine.HandAction;
import blackjack.engine.Player;
import java.util.List;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class ComputerActionHandler implements Runnable {

    private HandAction result;
    private Hand activeHand;
    private Player activePlayer;

    public void setHand(Hand hand) {
        activeHand = hand;
    }

    public void setPlayer(Player player) {
        activePlayer = player;
    }
    
    public HandAction getResult() {
        return result;
    }
    
    public ComputerActionHandler(Player p, Hand h) {
        activePlayer = p;
        activeHand = h;
    }

    @Override
    public synchronized void run() {
        final List<HandAction> possibleActions = activeHand.getLegalActions(activePlayer.getFunds());
        if (activeHand.cardsValue() == 11 && possibleActions.contains(HandAction.DOUBLE)) {
            result = HandAction.DOUBLE;
        } else if (activeHand.cardsValue() < 17 && possibleActions.contains(HandAction.HIT)) {
            result = HandAction.HIT;
        } else {
            result = HandAction.STAND;
        }
    }
}

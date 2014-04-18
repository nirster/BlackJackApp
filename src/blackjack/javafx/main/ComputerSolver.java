package blackjack.javafx.main;

import blackjack.engine.Hand;
import blackjack.engine.HandAction;
import blackjack.engine.Player;
import java.util.concurrent.Callable;

/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class ComputerSolver implements Callable<HandAction> {
    private final Player activePlayer;
    private final Hand activeHand;
    
    public ComputerSolver(Player p, Hand h) {
        activePlayer = p;
        activeHand = h;
    }

    @Override
    public synchronized HandAction call() throws Exception {
        //final List<HandAction> possibleActions = activeHand.getLegalActions(activePlayer.getFunds());
        if (activeHand.cardsValue() == 11 /*&& possibleActions.contains(HandAction.DOUBLE)*/) {
            return HandAction.DOUBLE;
        } else if (activeHand.cardsValue() < 17 /*&& possibleActions.contains(HandAction.HIT)*/) {
            return HandAction.HIT;
        } else {
            return HandAction.STAND;
        }
    }
}

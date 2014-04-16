package blackjack.engine;
/**
 *
 * @author Nir Zarko <nirster@gmail.com>
 */
public class IDGenerator {
    
    public static long getUID() {
        return System.currentTimeMillis();
    }
}
